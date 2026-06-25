package com.hxq.smart_campus.service.ai;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Markdown 格式化工具
 * 将 Tool 方法返回的数据统一为 Markdown 结构，便于前端 marked 渲染
 */
public class MarkdownFormatter {

    private MarkdownFormatter() {}

    /**
     * 将 List 转换为 Markdown 表格
     *
     * @param headers 表头
     * @param rows    数据行
     * @return Markdown 表格字符串
     */
    public static String toTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0 || rows == null || rows.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 表头
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        // 分隔行
        sb.append("| ").append(Stream.generate(() -> "---")
                .limit(headers.length).collect(Collectors.joining(" | ")))
          .append(" |\n");
        // 数据行
        for (String[] row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }
        return sb.toString();
    }

    /**
     * 将 Map 数据转为 Markdown 列表
     *
     * @param data 键值对数据
     * @return Markdown 无序列表字符串
     */
    public static String toKeyValueList(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        return data.entrySet().stream()
                .map(e -> String.format("- **%s**：%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 将多个键值对转为 Markdown 列表（无 Map 封装，直接输出）
     *
     * @param items 键值对数组，如 "key1", "val1", "key2", "val2"
     * @return Markdown 无序列表字符串
     */
    public static String toKeyValueList(String... items) {
        if (items == null || items.length == 0 || items.length % 2 != 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i += 2) {
            sb.append("- **").append(items[i]).append("**：").append(items[i + 1]).append("\n");
        }
        return sb.toString();
    }

    /**
     * 将 List<Map> 数据转为 Markdown 列表
     *
     * @param dataList 数据列表
     * @param labelKey 标签字段名
     * @param valueKey 值字段名
     * @return Markdown 无序列表字符串
     */
    public static String mapListToKeyValue(List<Map<String, Object>> dataList, String labelKey, String valueKey) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }
        return dataList.stream()
                .map(m -> String.format("- **%s**：%s", m.get(labelKey), m.get(valueKey)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 将单个值包装为 Markdown 提示
     *
     * @param label 标签
     * @param value 值
     * @return Markdown 文本
     */
    public static String singleValue(String label, Object value) {
        return String.format("**%s**：%s", label, value != null ? value : "--");
    }

    /**
     * 获取无数据时的提示信息
     *
     * @param subject 数据主题
     * @return Markdown 提示文本
     */
    public static String noData(String subject) {
        return String.format("> 暂无%s数据。", subject);
    }

    /**
     * 获取统计概要标题
     *
     * @param title 标题
     * @return Markdown 标题文本
     */
    public static String header(String title) {
        return "### " + title;
    }
}