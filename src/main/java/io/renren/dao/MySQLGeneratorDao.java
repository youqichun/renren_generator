package io.renren.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


/**
 * MySQL代码生成器
 *
 * @author Mark sunlightcs@gmail.com
 * @since 2018-07-24
 */
@Mapper
public interface MySQLGeneratorDao {
    List<Map<String, Object>> queryList(Map<String, Object> map);

    Map<String, String> queryTable(String tableName);

    List<Map<String, String>> queryColumns(String tableName);
}
