package org.example.controller;

import org.example.service.DynamicCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fully-dynamic")
public class FullyDynamicController {

    @Autowired
    private DynamicCrudService dynamicCrudService;
    
    /**
     * 获取表的列信息
     *
     * @param tableName 表名
     * @return 表的列信息
     */
    @GetMapping("/{tableName}/columns")
    public ResponseEntity<Map<String, Object>> getTableColumns(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Object[]> result = dynamicCrudService.getTableColumns(tableName);
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取表列信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建表（基于传入的数据字段）
     *
     * @param tableName 表名
     * @param sampleData 示例数据，用于推断字段类型
     * @return 操作结果
     */
    @PostMapping("/{tableName}/create-table")
    public ResponseEntity<Map<String, Object>> createTable(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> sampleData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 根据示例数据创建表
            String sql = generateCreateTableSql(tableName, sampleData);
            dynamicCrudService.executeDdlSql(sql);
            
            // 验证表是否创建成功
            try {
                dynamicCrudService.select(tableName, new HashMap<>());
                response.put("success", true);
                response.put("message", "表创建成功");
                response.put("sql", sql); // 返回执行的SQL语句，便于调试
            } catch (Exception e) {
                // 如果验证失败，尝试直接查询验证
                try {
                    dynamicCrudService.executeSelectSql("SELECT 1 FROM " + tableName + " LIMIT 1");
                    response.put("success", true);
                    response.put("message", "表创建成功");
                    response.put("sql", sql);
                } catch (Exception ex) {
                    // 如果两种方式都失败了，说明表创建可能真的失败了
                    response.put("success", false);
                    response.put("message", "表创建失败或者无法验证表是否创建成功: " + ex.getMessage());
                    return ResponseEntity.status(500).body(response);
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "表创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 删除表
     *
     * @param tableName 表名
     * @return 操作结果
     */
    @DeleteMapping("/{tableName}/drop-table")
    public ResponseEntity<Map<String, Object>> dropTable(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dynamicCrudService.dropTable(tableName);
            response.put("success", true);
            response.put("message", "表删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "表删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 插入数据
     *
     * @param tableName 表名
     * @param data 数据内容
     * @return 操作结果
     */
    @PostMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> insert(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> data) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = dynamicCrudService.insert(tableName, data);
            response.put("success", true);
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "插入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 查询数据
     *
     * @param tableName 表名
     * @param conditions 查询条件
     * @return 查询结果
     */
    @GetMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> select(
            @PathVariable String tableName,
            @RequestParam(required = false) Map<String, Object> conditions) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Object[]> result = dynamicCrudService.select(tableName, conditions);
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新数据
     *
     * @param tableName 表名
     * @param conditions 更新条件
     * @param data 新数据
     * @return 操作结果
     */
    @PutMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String tableName,
            @RequestParam Map<String, Object> conditions,
            @RequestBody Map<String, Object> data) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = dynamicCrudService.update(tableName, data, conditions);
            response.put("success", true);
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除数据
     *
     * @param tableName 表名
     * @param conditions 删除条件
     * @return 操作结果
     */
    @DeleteMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable String tableName,
            @RequestParam Map<String, Object> conditions) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = dynamicCrudService.delete(tableName, conditions);
            response.put("success", true);
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据示例数据生成CREATE TABLE SQL语句
     *
     * @param tableName 表名
     * @param sampleData 示例数据
     * @return CREATE TABLE SQL语句
     */
    private String generateCreateTableSql(String tableName, Map<String, Object> sampleData) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        sql.append("id BIGINT AUTO_INCREMENT PRIMARY KEY");
        
        for (Map.Entry<String, Object> entry : sampleData.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过id字段，因为我们已经添加了主键
            if ("id".equalsIgnoreCase(columnName)) {
                continue;
            }
            
            sql.append(", ");
            sql.append(columnName).append(" ");
            
            // 根据值的类型推断列类型
            if (value instanceof String) {
                // 如果字符串较长，使用TEXT类型
                sql.append(((String) value).length() > 255 ? "TEXT" : "VARCHAR(255)");
            } else if (value instanceof Integer) {
                sql.append("INT");
            } else if (value instanceof Long) {
                sql.append("BIGINT");
            } else if (value instanceof Double || value instanceof Float) {
                sql.append("DOUBLE");
            } else if (value instanceof Boolean) {
                sql.append("BOOLEAN");
            } else {
                // 默认使用VARCHAR(255)
                sql.append("VARCHAR(255)");
            }
        }
        
        sql.append(")");
        return sql.toString();
    }
}