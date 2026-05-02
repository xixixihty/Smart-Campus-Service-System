package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import com.hxq.smart_campus.service.ScoreEntryService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

/**
 * 成绩录入 Service 实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreEntryServiceImpl implements ScoreEntryService {

    private final ScoreEntryMapper scoreEntryMapper;

    /**
     * 录入成绩
     *
     * @param scoreEntryCreateDTO 成绩录入参数
     * @return 成绩响应DTO
     * @throws IllegalArgumentException 参数为空时抛出
     * @throws RuntimeException 录入失败时抛出
     */
    @Override
    public ScoreEntryResponseDTO insertScore(ScoreEntryCreateDTO scoreEntryCreateDTO) {
        if (scoreEntryCreateDTO == null) {
            throw new IllegalArgumentException("成绩录入参数不能为空");
        }
        int result = scoreEntryMapper.insertScore(scoreEntryCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("成绩录入失败");
        }
        Long id = scoreEntryMapper.getLastInsertId();
        ScoreEntryDetailVO scoreEntryDetailVO = scoreEntryMapper.getScoreDetail(id);
        if (scoreEntryDetailVO == null) {
            throw new RuntimeException("获取录入成功的成绩信息失败");
        }
        return convertToScoreEntryResponseDTO(scoreEntryDetailVO);
    }

    /**
     * 更新成绩
     *
     * @param scoreEntryUpdateDTO 成绩更新参数
     * @return 成绩响应DTO
     * @throws IllegalArgumentException 参数为空时抛出
     * @throws RuntimeException 更新失败时抛出
     */
    @Override
    public ScoreEntryResponseDTO updateScore(ScoreEntryUpdateDTO scoreEntryUpdateDTO) {
        if (scoreEntryUpdateDTO == null) {
            throw new IllegalArgumentException("成绩更新参数不能为空");
        }
        int result = scoreEntryMapper.updateScore(scoreEntryUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("成绩更新失败");
        }
        ScoreEntryDetailVO scoreEntryDetailVO = scoreEntryMapper.getScoreDetail(scoreEntryUpdateDTO.getId());
        if (scoreEntryDetailVO == null) {
            throw new RuntimeException("获取更新后的成绩信息失败");
        }
        return convertToScoreEntryResponseDTO(scoreEntryDetailVO);
    }

    /**
     * 批量录入成绩
     *
     * @param batchScoreEntryDTO 批量成绩录入参数
     * @return 是否成功
     * @throws IllegalArgumentException 参数为空或列表为空时抛出
     * @throws RuntimeException 录入失败时抛出
     */
    @Override
    public boolean BatchScore(BatchScoreEntryDTO batchScoreEntryDTO) {
        if (batchScoreEntryDTO == null || batchScoreEntryDTO.getScoreEntries() == null || batchScoreEntryDTO.getScoreEntries().isEmpty()) {
            throw new IllegalArgumentException("成绩录入参数不能为空");
        }
        for (ScoreEntryCreateDTO scoreEntry : batchScoreEntryDTO.getScoreEntries()) {
            int result = scoreEntryMapper.insertScore(scoreEntry);
            if (result <= 0) {
                throw new RuntimeException("成绩录入失败");
            }
        }
        return true;
    }

    /**
     * 获取成绩列表
     *
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @return 分页成绩列表
     */
    @Override
    public PageInfo<ScoreEntryListVO> getScoreList(Integer pageNum, Integer pageSize, Long courseId, Long studentId, Long semesterId) {
        PageHelper.startPage(pageNum, pageSize);
        List<ScoreEntryListVO> scoreEntryListVOList = scoreEntryMapper.getScoreList(courseId, studentId, semesterId);
        return new PageInfo<>(scoreEntryListVOList);
    }

    /**
     * 获取成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情VO
     */
    @Override
    public ScoreEntryDetailVO getScoreDetail(Long id) {
        return scoreEntryMapper.getScoreDetail(id);
    }

    /**
     * 获取我的成绩列表
     *
     * @param semesterId 学期ID
     * @param courseId   课程ID
     * @return 成绩列表
     */
    @Override
    public List<ScoreEntryListVO> getMyScoreList(Long semesterId, Long courseId) {
        Long studentId = SecurityUtils.getCurrentUserId();
        return scoreEntryMapper.getScoreList(courseId, studentId, semesterId);
    }

    /**
     * 获取成绩统计信息
     *
     * @param queryDTO 查询参数（包含学期ID、统计维度及对应维度ID）
     * @return 成绩统计VO
     * @throws IllegalArgumentException 参数校验失败时抛出
     */
    @Override
    public ScoreStatisticsVO getScoreStatistics(ScoreStatisticsQueryDTO queryDTO) {
        // ========== 步骤1：参数校验 ==========
        // 校验查询参数合法性，确保必填参数已填写
        validateQuery(queryDTO);

        // ========== 步骤2：Mapper单次查询 ==========
        // 调用Mapper只执行一次SQL查询，获取成绩列表及相关信息
        // Service层负责在内存中对数据进行统计计算
        List<ScoreEntryForStatisticsVO> scoreList = scoreEntryMapper.getScoreListForStatistics(queryDTO);

        // ========== 步骤3：构建统计结果对象 ==========
        ScoreStatisticsVO statistics = new ScoreStatisticsVO();

        // ========== 步骤4：设置基础维度信息 ==========
        // 维度类型和学期ID直接来源于查询参数
        statistics.setDimension(queryDTO.getDimension());
        statistics.setSemesterId(queryDTO.getSemesterId());

        // ========== 步骤5：Service层内存统计计算 ==========
        // 通过遍历成绩列表，在内存中计算各项统计指标
        // 包括：总人数、平均分、最高分、最低分、各分段人数等
        statistics.setTotalCount(countTotal(scoreList));
        statistics.setAverageScore(calcAverage(scoreList));
        statistics.setHighestScore(calcHighest(scoreList));
        statistics.setLowestScore(calcLowest(scoreList));
        statistics.setExcellentCount(countExcellent(scoreList));
        statistics.setGoodCount(countGood(scoreList));
        statistics.setPassCount(countPass(scoreList));
        statistics.setFailCount(countFail(scoreList));

        // ========== 步骤6：设置维度名称 ==========
        // 根据不同维度类型查询对应的名称信息
        statistics.setDimensionName(getDimensionName(queryDTO));

        // ========== 步骤7：设置学期名称 ==========
        // 通过Mapper查询学期名称
        statistics.setSemesterName(scoreEntryMapper.getSemesterNameById(queryDTO.getSemesterId()));

        // ========== 步骤8：计算率统计 ==========
        // 根据人数计算优秀率、良好率、及格率
        calculateRates(statistics);

        return statistics;
    }

    /**
     * 获取平均绩点
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return GPA VO
     * @throws IllegalArgumentException 参数为空时抛出
     */
    @Override
    public GpaVO getGpa(Long studentId, Long semesterId) {
        if (studentId == null || semesterId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        List<ScoreEntryListVO> scoreEntryListVOList = getMyScoreList(studentId, null);
        Map<String, List<ScoreEntryListVO>> scoreEntryListVOMap = scoreEntryListVOList.stream()
                .collect(Collectors.groupingBy(ScoreEntryListVO::getSemesterName));
        GpaVO gpaVO = new GpaVO();
        gpaVO.setGpa(BigDecimal.ZERO);
        gpaVO.setCourseCount(0);
        gpaVO.setTotalCredits(BigDecimal.ZERO);
        for (Map.Entry<String, List<ScoreEntryListVO>> entry : scoreEntryListVOMap.entrySet()) {
            List<ScoreEntryListVO> scoreEntryListVOs = entry.getValue();
            BigDecimal gpa = BigDecimal.ZERO;
            Integer courseCount = 0;
            BigDecimal totalCredits = BigDecimal.ZERO;
            for (ScoreEntryListVO scoreEntryListVO : scoreEntryListVOs) {
                gpa = gpa.add(scoreEntryListVO.getScorePoint());
                courseCount++;
                totalCredits = totalCredits.add(scoreEntryListVO.getTotalScore());
            }
            gpa = gpa.divide(new BigDecimal(courseCount), 2, RoundingMode.HALF_UP);
            gpaVO.setGpa(gpa);
            gpaVO.setCourseCount(courseCount);
            gpaVO.setTotalCredits(totalCredits);
        }
        return gpaVO;
    }

    // ==================== Service层统计方法 ====================

    /**
     * 统计总人数
     * 思路：直接获取成绩列表的size即为总人数
     *
     * @param scoreList 成绩列表
     * @return 总人数
     */
    private Integer countTotal(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return 0;
        }
        return scoreList.size();
    }

    /**
     * 计算平均分
     * 思路：遍历列表，将所有总评成绩求和，然后除以有效成绩数量
     *
     * @param scoreList 成绩列表
     * @return 平均分
     */
    private BigDecimal calcAverage(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null) {
                sum = sum.add(score.getTotalScore());
                count++;
            }
        }
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算最高分
     * 思路：遍历列表，比较每个成绩，获取最大值
     *
     * @param scoreList 成绩列表
     * @return 最高分
     */
    private BigDecimal calcHighest(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal highest = null;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null) {
                if (highest == null || score.getTotalScore().compareTo(highest) > 0) {
                    highest = score.getTotalScore();
                }
            }
        }
        return highest != null ? highest : BigDecimal.ZERO;
    }

    /**
     * 计算最低分
     * 思路：遍历列表，比较每个成绩，获取最小值
     *
     * @param scoreList 成绩列表
     * @return 最低分
     */
    private BigDecimal calcLowest(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal lowest = null;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null) {
                if (lowest == null || score.getTotalScore().compareTo(lowest) < 0) {
                    lowest = score.getTotalScore();
                }
            }
        }
        return lowest != null ? lowest : BigDecimal.ZERO;
    }

    /**
     * 统计优秀人数（>=90分）
     * 思路：遍历列表，统计总评成绩>=90的数量
     *
     * @param scoreList 成绩列表
     * @return 优秀人数
     */
    private Integer countExcellent(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null && score.getTotalScore().compareTo(new BigDecimal("90")) >= 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计良好人数（80-89分）
     * 思路：遍历列表，统计80<=成绩<90的数量
     *
     * @param scoreList 成绩列表
     * @return 良好人数
     */
    private Integer countGood(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null) {
                BigDecimal total = score.getTotalScore();
                if (total.compareTo(new BigDecimal("80")) >= 0 && total.compareTo(new BigDecimal("90")) < 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 统计及格人数（60-79分）
     * 思路：遍历列表，统计60<=成绩<80的数量
     *
     * @param scoreList 成绩列表
     * @return 及格人数
     */
    private Integer countPass(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null) {
                BigDecimal total = score.getTotalScore();
                if (total.compareTo(new BigDecimal("60")) >= 0 && total.compareTo(new BigDecimal("80")) < 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 统计不及格人数（<60分）
     * 思路：遍历列表，统计成绩<60的数量
     *
     * @param scoreList 成绩列表
     * @return 不及格人数
     */
    private Integer countFail(List<ScoreEntryForStatisticsVO> scoreList) {
        if (scoreList == null || scoreList.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ScoreEntryForStatisticsVO score : scoreList) {
            if (score.getTotalScore() != null && score.getTotalScore().compareTo(new BigDecimal("60")) < 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 计算率统计（优秀率、良好率、及格率）
     * 思路：根据各分段人数和总人数计算百分比
     *
     * @param statistics 统计结果对象
     */
    private void calculateRates(ScoreStatisticsVO statistics) {
        int total = statistics.getTotalCount() != null ? statistics.getTotalCount() : 0;
        if (total == 0) {
            total = 1;
        }
        // 计算优秀率 = 优秀人数 / 总人数 * 100
        if (statistics.getExcellentCount() != null) {
            statistics.setExcellentRate(
                    BigDecimal.valueOf(statistics.getExcellentCount())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
            );
        }
        // 计算良好率 = 良好人数 / 总人数 * 100
        if (statistics.getGoodCount() != null) {
            statistics.setGoodRate(
                    BigDecimal.valueOf(statistics.getGoodCount())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
            );
        }
        // 计算及格率 = (优秀+良好+及格人数) / 总人数 * 100
        int passTotal = Optional.ofNullable(statistics.getExcellentCount()).orElse(0)
                + Optional.ofNullable(statistics.getGoodCount()).orElse(0)
                + Optional.ofNullable(statistics.getPassCount()).orElse(0);
        statistics.setPassRate(
                BigDecimal.valueOf(passTotal)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
        );
    }

    /**
     * 参数校验
     * 思路：校验必填参数和维度对应关系
     *
     * @param queryDTO 查询参数
     * @throws IllegalArgumentException 校验失败时抛出
     */
    private void validateQuery(ScoreStatisticsQueryDTO queryDTO) {
        if (queryDTO.getSemesterId() == null) {
            throw new IllegalArgumentException("学期ID不能为空");
        }
        if (queryDTO.getDimension() == null) {
            throw new IllegalArgumentException("统计维度不能为空");
        }
        switch (queryDTO.getDimension()) {
            case STUDENT:
                if (queryDTO.getStudentId() == null) {
                    throw new IllegalArgumentException("个人维度必须指定学生ID");
                }
                break;
            case COURSE:
                if (queryDTO.getCourseId() == null) {
                    throw new IllegalArgumentException("课程维度必须指定课程ID");
                }
                break;
            case CLASS:
                if (queryDTO.getClassId() == null) {
                    throw new IllegalArgumentException("班级维度必须指定班级ID");
                }
                break;
            case MAJOR:
                if (queryDTO.getMajorId() == null) {
                    throw new IllegalArgumentException("专业维度必须指定专业ID");
                }
                break;
            case COLLEGE:
                if (queryDTO.getCollegeId() == null) {
                    throw new IllegalArgumentException("学院维度必须指定学院ID");
                }
                break;
        }
    }

    /**
     * 获取维度名称
     * 思路：根据不同维度类型查询对应的名称，拼接完整标识
     *
     * @param queryDTO 查询参数
     * @return 维度名称
     */
    private String getDimensionName(ScoreStatisticsQueryDTO queryDTO) {
        switch (queryDTO.getDimension()) {
            case STUDENT:
                String name = scoreEntryMapper.getStudentNameById(queryDTO.getStudentId());
                String no = scoreEntryMapper.getStudentNoById(queryDTO.getStudentId());
                return name + "(" + no + ")";
            case COURSE:
                return scoreEntryMapper.getCourseNameById(queryDTO.getCourseId());
            case CLASS:
                return scoreEntryMapper.getClassNameById(queryDTO.getClassId());
            case MAJOR:
                return scoreEntryMapper.getMajorNameById(queryDTO.getMajorId());
            case COLLEGE:
                return scoreEntryMapper.getCollegeNameById(queryDTO.getCollegeId());
            default:
                return "未知";
        }
    }

    /**
     * 转换成绩详情为成绩响应DTO
     *
     * @param scoreEntryDetailVO 成绩详情VO
     * @return 成绩响应DTO
     */
    private ScoreEntryResponseDTO convertToScoreEntryResponseDTO(ScoreEntryDetailVO scoreEntryDetailVO) {
        if (scoreEntryDetailVO == null) {
            throw new IllegalArgumentException("成绩详情参数不能为空");
        }
        ScoreEntryResponseDTO scoreEntryResponseDTO = new ScoreEntryResponseDTO();
        scoreEntryResponseDTO.setId(scoreEntryDetailVO.getId());
        scoreEntryResponseDTO.setCourseId(scoreEntryDetailVO.getCourseId());
        scoreEntryResponseDTO.setCourseName(scoreEntryDetailVO.getCourseName());
        scoreEntryResponseDTO.setStudentId(scoreEntryDetailVO.getStudentId());
        scoreEntryResponseDTO.setStudentName(scoreEntryDetailVO.getStudentName());
        scoreEntryResponseDTO.setStudentNo(scoreEntryDetailVO.getStudentNo());
        scoreEntryResponseDTO.setSemesterId(scoreEntryDetailVO.getSemesterId());
        scoreEntryResponseDTO.setSemesterName(scoreEntryDetailVO.getSemesterName());
        scoreEntryResponseDTO.setUsualScore(scoreEntryDetailVO.getUsualScore());
        scoreEntryResponseDTO.setFinalScore(scoreEntryDetailVO.getFinalScore());
        scoreEntryResponseDTO.setTotalScore(scoreEntryDetailVO.getTotalScore());
        scoreEntryResponseDTO.setMakeupScore(scoreEntryDetailVO.getMakeupScore());
        scoreEntryResponseDTO.setScorePoint(scoreEntryDetailVO.getScorePoint());
        scoreEntryResponseDTO.setExamStatus(scoreEntryDetailVO.getExamStatus());
        scoreEntryResponseDTO.setMakeupExamId(scoreEntryDetailVO.getMakeupExamId());
        try {
            if (scoreEntryDetailVO.getCreateTime() != null && !scoreEntryDetailVO.getCreateTime().isEmpty()) {
                scoreEntryResponseDTO.setCreateTime(LocalDateTime.parse(scoreEntryDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (scoreEntryDetailVO.getUpdateTime() != null && !scoreEntryDetailVO.getUpdateTime().isEmpty()) {
                scoreEntryResponseDTO.setUpdateTime(LocalDateTime.parse(scoreEntryDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换成绩详情为响应DTO时出错", e);
            throw new RuntimeException("转换成绩详情为响应DTO时出错", e);
        }
        return scoreEntryResponseDTO;
    }
}
