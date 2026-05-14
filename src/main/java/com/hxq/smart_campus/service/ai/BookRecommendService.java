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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookRecommendService {

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public List<BookRecommendVO> recommendByStudent(Long studentId) {
        List<BorrowRecordListVO> history = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        List<BookListVO> allBooks = bookMapper.getBookList(null, null, null, null, null);

        if (allBooks == null || allBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // 找出学生已借过的书名（带null保护）
        Set<String> borrowedTitles = history != null ? history.stream()
                .map(BorrowRecordListVO::getBookTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()) : Collections.emptySet();

        // 找出学生已借过书的分类，优先推荐同分类书籍
        Set<String> historyCategories = history != null ? history.stream()
                .filter(h -> h.getBookTitle() != null)
                .map(h -> {
                    for (BookListVO b : allBooks) {
                        if (b.getTitle() != null && b.getTitle().equals(h.getBookTitle()) && b.getCategoryName() != null) {
                            return b.getCategoryName();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()) : Collections.emptySet();

        // 筛选未借过的书，优先同分类
        List<BookListVO> filtered = allBooks.stream()
                .filter(b -> b.getTitle() != null && !borrowedTitles.contains(b.getTitle()))
                .sorted((a, b) -> {
                    boolean aInCategory = historyCategories.contains(a.getCategoryName());
                    boolean bInCategory = historyCategories.contains(b.getCategoryName());
                    if (aInCategory && !bInCategory) return -1;
                    if (!aInCategory && bInCategory) return 1;
                    // 同分类下按可借数排序（可借少=热门）
                    int aAvail = a.getAvailableCopies() != null ? a.getAvailableCopies() : 999;
                    int bAvail = b.getAvailableCopies() != null ? b.getAvailableCopies() : 999;
                    return Integer.compare(aAvail, bAvail);
                })
                .limit(5)
                .collect(Collectors.toList());

        List<BookRecommendVO> recommendations = new ArrayList<>();
        for (int i = 0; i < filtered.size(); i++) {
            BookListVO book = filtered.get(i);
            boolean sameCategory = historyCategories.contains(book.getCategoryName());
            String reason = sameCategory ? "与您读过的书籍分类相同，值得一读"
                    : i == 0 ? "热门推荐，广受好评"
                    : i == 1 ? "经典之作，不容错过"
                    : "根据您的阅读偏好推荐";
            // 计算推荐评分：同分类加分 + 基于可借数的热度
            int ratingBase = sameCategory ? 4 : 3;
            int rating = Math.min(5, ratingBase + (book.getAvailableCopies() != null && book.getAvailableCopies() < 5 ? 1 : 0));
            recommendations.add(BookRecommendVO.builder()
                    .id(book.getId())
                    .bookName(book.getTitle())
                    .author(book.getAuthor())
                    .reason(reason)
                    .rating(rating)
                    .build());
        }

        return recommendations;
    }

    public List<HotBookVO> recommendHotBooks() {
        List<BookListVO> allBooks = bookMapper.getBookList(null, null, null, null, null);
        if (allBooks == null || allBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // 统计每本书的借阅次数
        Map<Long, String> bookIdToTitle = allBooks.stream()
                .filter(b -> b.getId() != null)
                .collect(Collectors.toMap(BookListVO::getId, b -> b.getTitle() != null ? b.getTitle() : "", (a, b) -> a));

        // 获取所有借阅记录统计借阅次数
        Map<String, Integer> borrowCountByTitle = new HashMap<>();
        try {
            // 通过逐一查询各本书的借阅情况统计（限制查询范围避免性能问题）
            List<BorrowRecordListVO> allRecords = borrowRecordMapper.getBorrowRecordList(null, null, null);
            if (allRecords != null) {
                for (BorrowRecordListVO record : allRecords) {
                    if (record.getBookTitle() != null) {
                        borrowCountByTitle.merge(record.getBookTitle(), 1, Integer::sum);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询借阅记录统计失败，使用默认排序", e);
        }

        // 按借阅次数排序取前10
        int maxBorrowCount = borrowCountByTitle.values().stream().max(Integer::compareTo).orElse(1);

        List<HotBookVO> hotBooks = allBooks.stream()
                .limit(10)
                .map(book -> {
                    int borrowCount = borrowCountByTitle.getOrDefault(book.getTitle() != null ? book.getTitle() : "", 0);
                    // hotScore 根据借阅次数归一化到 60-100 范围
                    int hotScore = maxBorrowCount > 0
                            ? 60 + (borrowCount * 40 / maxBorrowCount)
                            : 60;
                    return HotBookVO.builder()
                            .id(book.getId())
                            .bookName(book.getTitle())
                            .author(book.getAuthor())
                            .categoryName(book.getCategoryName())
                            .borrowCount(borrowCount)
                            .hotScore(hotScore)
                            .build();
                })
                .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                .collect(Collectors.toList());

        return hotBooks;
    }
}
