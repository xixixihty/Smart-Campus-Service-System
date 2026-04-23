# SQL错误修复与功能测试计划

## 1. 问题分析

从日志信息中发现，专业查询列表功能出现SQL错误：
```
java.sql.SQLIntegrityConstraintViolationException: Column 'id' in field list is ambiguous
```

错误原因：在`MajorMapper.xml`的`selectMajorList`查询中，`id`字段在`major`和`college`表中都存在，需要指定表别名。

## 2. 修复方案

### 2.1 修复SQL错误
- 修改`MajorMapper.xml`中的`selectMajorList`查询，为`id`字段添加表别名`m.id`

### 2.2 全面功能测试
- **学院管理模块**：测试增删改查功能
- **专业管理模块**：测试增删改查功能，重点测试查询列表功能
- **班级管理模块**：测试增删改查功能

## 3. 修复步骤

1. **修复SQL错误**：修改`MajorMapper.xml`中的`selectMajorList`查询
2. **验证修复**：重新启动项目，测试专业查询列表功能
3. **功能测试**：测试所有模块的核心功能
4. **边界测试**：测试边界条件和异常场景

## 4. 预期结果

- SQL错误已修复，专业查询列表功能正常运行
- 所有模块的核心功能都能正常工作
- 边界条件和异常场景处理正确
- 整体功能流程完整且正确