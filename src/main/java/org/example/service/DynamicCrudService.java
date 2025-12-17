package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;

@Service
public class DynamicCrudService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private DynamicSqlGenerator sqlGenerator;
    
    /**
     * 动态插入数据
     */
    @Transactional
    public int insert(String tableName, Map<String, Object> data) {
        // 验证表是否存在
        validateTableExists(tableName);
        
        String sql = sqlGenerator.generateInsertSql(data, tableName);
        Query query = entityManager.createNativeQuery(sql);
        
        // 绑定参数
        List<Object> values = sqlGenerator.getInsertValues(data);
        for (int i = 0; i < values.size(); i++) {
            query.setParameter(i + 1, values.get(i));
        }
        
        return query.executeUpdate();
    }
    
    /**
     * 动态查询数据
     */
    @Transactional(readOnly = true)
    public List<Object[]> select(String tableName, Map<String, Object> conditions) {
        // 验证表是否存在
        validateTableExists(tableName);
        
        String sql = sqlGenerator.generateSelectSql(tableName, conditions);
        Query query = entityManager.createNativeQuery(sql);
        
        // 绑定查询条件参数
        if (conditions != null) {
            int index = 1;
            for (Object value : conditions.values()) {
                query.setParameter(index++, value);
            }
        }
        
        return query.getResultList();
    }
    
    /**
     * 动态更新数据
     */
    @Transactional
    public int update(String tableName, Map<String, Object> data, Map<String, Object> conditions) {
        // 验证表是否存在
        validateTableExists(tableName);
        
        String sql = sqlGenerator.generateUpdateSql(tableName, data, conditions);
        Query query = entityManager.createNativeQuery(sql);
        
        // 绑定参数
        int index = 1;
        for (Object value : data.values()) {
            query.setParameter(index++, value);
        }
        
        for (Object value : conditions.values()) {
            query.setParameter(index++, value);
        }
        
        return query.executeUpdate();
    }
    
    /**
     * 动态删除数据
     */
    @Transactional
    public int delete(String tableName, Map<String, Object> conditions) {
        // 验证表是否存在
        validateTableExists(tableName);
        
        String sql = sqlGenerator.generateDeleteSql(tableName, conditions);
        Query query = entityManager.createNativeQuery(sql);
        
        // 绑定查询条件参数
        if (conditions != null) {
            int index = 1;
            for (Object value : conditions.values()) {
                query.setParameter(index++, value);
            }
        }
        
        return query.executeUpdate();
    }
    
    /**
     * 删除整个表
     */
    @Transactional
    public void dropTable(String tableName) {
        // 验证表是否存在
        validateTableExists(tableName);
        
        String sql = "DROP TABLE " + tableName;
        entityManager.createNativeQuery(sql).executeUpdate();
    }
    
    /**
     * 执行任意SQL语句 (仅用于更新操作)
     */
    @Transactional
    public int executeUpdateSql(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }
    
    /**
     * 执行DDL语句 (如CREATE TABLE, ALTER TABLE等)
     */
    @Transactional
    public void executeDdlSql(String sql) {
        entityManager.createNativeQuery(sql).executeUpdate();
    }
    
    /**
     * 执行SELECT语句 (仅用于查询操作)
     */
    @Transactional(readOnly = true)
    public List<Object> executeSelectSql(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
    
    /**
     * 验证表是否存在
     * @param tableName 表名
     */
    private void validateTableExists(String tableName) {
        try {
            // 使用DESCRIBE语句检查表是否存在，这是MySQL中检查表是否存在的更好方法
            entityManager.createNativeQuery("DESCRIBE " + tableName).getResultList();
        } catch (Exception e) {
            // 如果DESCRIBE失败，再尝试使用SHOW TABLES检查
            try {
                List<Object> tables = entityManager.createNativeQuery("SHOW TABLES LIKE '" + tableName + "'").getResultList();
                if (tables.isEmpty()) {
                    throw new RuntimeException("表 '" + tableName + "' 不存在");
                }
            } catch (Exception ex) {
                throw new RuntimeException("表 '" + tableName + "' 不存在或无法访问: " + ex.getMessage());
            }
        }
    }
}