# 动态数据管理系统

这是一个基于 Spring Boot 和原生 JDBC 实现的动态数据管理系统，允许用户在运行时动态定义数据结构并进行完整的 CRUD 操作。

## 功能特点

1. **动态表结构创建**：用户可以通过提供示例数据来动态创建数据库表结构
2. **智能类型推断**：系统根据示例数据自动推断字段类型（VARCHAR, INT, DOUBLE, BOOLEAN 等）
3. **完整的 CRUD 操作**：支持数据的增删改查操作
4. **完全动态的前端界面**：前端界面根据用户定义的数据结构动态生成表单和数据显示
5. **表结构信息获取**：支持获取表的列信息，用于前端动态渲染

## 技术架构

- **后端**：Spring Boot + JPA/Hibernate
- **前端**：Vue.js 3 + 原生 HTML/CSS
- **数据库**：MySQL（可替换为其他关系型数据库）
- **构建工具**：Maven

## 核心组件

### 1. FullyDynamicController
处理所有动态数据管理的 REST API 请求：
- `/api/fully-dynamic/{tableName}/create-table` - 创建表结构
- `/api/fully-dynamic/{tableName}/columns` - 获取表列信息
- `/api/fully-dynamic/{tableName}` (POST) - 插入数据
- `/api/fully-dynamic/{tableName}` (GET) - 查询数据
- `/api/fully-dynamic/{tableName}` (PUT) - 更新数据
- `/api/fully-dynamic/{tableName}` (DELETE) - 删除数据
- `/api/fully-dynamic/{tableName}/drop-table` (DELETE) - 删除表

### 2. DynamicCrudService
提供底层的数据操作服务，包括事务管理和 SQL 执行。

### 3. DynamicSqlGenerator
动态生成各种 SQL 语句（INSERT, SELECT, UPDATE, DELETE）。

### 4. 前端界面 (index.html)
提供用户友好的 Web 界面，支持：
- 表结构定义
- 动态表单生成
- 数据展示和操作
- 模态框交互模式

## 使用指南

### 1. 定义数据结构
1. 打开 `index.html` 页面
2. 输入表名（可选，留空则自动生成）
3. 输入示例数据（JSON格式）
4. 点击"创建表"

### 2. 数据操作
1. 使用"添加记录"按钮打开模态框插入数据
2. 查看、编辑或删除已有数据
3. 刷新页面后系统会自动从后端获取表结构信息

## 类型推断规则

系统会根据提供的示例值自动推断字段类型：

| 示例值类型 | 推断的数据库类型 |
|------------|------------------|
| 长字符串 (>255字符) | TEXT |
| 短字符串 (≤255字符) | VARCHAR(255) |
| 整数 | INT |
| 长整数 | BIGINT |
| 小数 | DOUBLE |
| 布尔值 | BOOLEAN |
| 其他 | VARCHAR(255) |

## API 接口说明

### 创建表结构
```
POST /api/fully-dynamic/{tableName}/create-table
Content-Type: application/json

{
  "fieldName1": "exampleValue1",
  "fieldName2": 123,
  "fieldName3": 45.67
}
```

### 获取表列信息
```
GET /api/fully-dynamic/{tableName}/columns
```

### 插入数据
```
POST /api/fully-dynamic/{tableName}
Content-Type: application/json

{
  "fieldName1": "value1",
  "fieldName2": 123,
  "fieldName3": 45.67
}
```

### 查询数据
```
GET /api/fully-dynamic/{tableName}
```

### 更新数据
```
PUT /api/fully-dynamic/{tableName}?id=recordId
Content-Type: application/json

{
  "fieldName1": "updatedValue1",
  "fieldName2": 456
}
```

### 删除数据
```
DELETE /api/fully-dynamic/{tableName}?id=recordId
```

### 删除表
```
DELETE /api/fully-dynamic/{tableName}/drop-table
```

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       ├── controller/
│   │       │   └── FullyDynamicController.java
│   │       ├── service/
│   │       │   ├── DynamicCrudService.java
│   │       │   └── DynamicSqlGenerator.java
│   │       └── Main.java
│   └── resources/
│       ├── static/
│       │   └── index.html
│       └── application.properties
└── pom.xml
```

## 扩展性考虑

此系统具有良好的扩展性，可以通过以下方式进行增强：

1. **支持更多数据类型**：添加对日期时间、JSON 等类型的支持
2. **权限控制**：添加用户认证和授权机制
3. **审计日志**：记录数据操作历史
4. **数据校验**：添加更严格的数据校验规则
5. **批量操作**：支持批量导入导出数据
6. **表结构更新**：支持动态修改表结构

## 注意事项

1. 系统默认使用 MySQL 数据库
2. 为了保证性能，建议在生产环境中使用索引优化
3. 当前版本未实现外键约束和复杂的关系处理
4. 前端使用 localStorage 缓存表名，清除浏览器数据会导致需要重新创建表