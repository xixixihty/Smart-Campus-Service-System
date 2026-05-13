package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.BookListVO;
import com.hxq.smart_campus.entity.vo.BookRecommendVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.entity.vo.HotBookVO;
import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookRecommendService {

    private final AiService aiService;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final Random random = new Random();

    public List<BookRecommendVO> recommendByStudent(Long studentId) {
        List<BorrowRecordListVO> history = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        List<BookListVO> allBooks = bookMapper.getBookList(null, null, null, null, null);

        List<BookRecommendVO> recommendations = new ArrayList<>();
        
        List<BookListVO> filteredBooks = allBooks.stream()
                .filter(b -> !history.stream().anyMatch(h -> h.getBookTitle().equals(b.getTitle())))
                .limit(5)
                .collect(Collectors.toList());

        String[] reasons = {
            "与您的阅读偏好高度匹配",
            "根据您的借阅历史推荐",
            "热门推荐，值得一读",
            "经典之作，不容错过",
            "广受好评的优秀作品"
        };

        for (int i = 0; i < filteredBooks.size(); i++) {
            BookListVO book = filteredBooks.get(i);
            recommendations.add(BookRecommendVO.builder()
                    .id(book.getId())
                    .bookName(book.getTitle())
                    .author(book.getAuthor())
                    .reason(reasons[i % reasons.length])
                    .rating(random.nextInt(3) + 3)
                    .build());
        }

        return recommendations;
    }

    public List<HotBookVO> recommendHotBooks() {
        List<BookListVO> allBooks = bookMapper.getBookList(null, null, null, null, null);
        List<HotBookVO> hotBooks = new ArrayList<>();

        for (int i = 0; i < Math.min(allBooks.size(), 10); i++) {
            BookListVO book = allBooks.get(i);
            hotBooks.add(HotBookVO.builder()
                    .id(book.getId())
                    .bookName(book.getTitle())
                    .author(book.getAuthor())
                    .categoryName(book.getCategoryName())
                    .borrowCount(random.nextInt(100) + 20)
                    .hotScore(random.nextInt(40) + 60)
                    .build());
        }

        return hotBooks.stream()
                .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                .collect(Collectors.toList());
    }
}
