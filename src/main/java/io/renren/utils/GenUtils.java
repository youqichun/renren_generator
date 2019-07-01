package io.renren.utils;

import io.renren.entity.ColumnEntity;
import io.renren.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class GenUtils {

    public static List<String> getTemplates(){
        List<String> templates = new ArrayList<String>();
        //VUE
        /*templates.add("template/version1/Entity.java.vm" );
        templates.add("template/version1/Mapper.java.vm" );
        templates.add("template/version1/Mapper.xml.vm" );
        templates.add("template/version1/Service.java.vm" );
        templates.add("template/version1/ServiceImpl.java.vm" );
        templates.add("template/version1/Controller.java.vm" );
        templates.add("template/version1/menu.sql.vm" );
        templates.add("template/version1/index.vue.vm" );
        templates.add("template/version1/add-or-update.vue.vm" );*/

        //商城模板  JSP
        /*templates.add("template/ecshop/Controller.java.vm");
        templates.add("template/ecshop/Entity.java.vm");
        templates.add("template/ecshop/Mapper.java.vm");
        templates.add("template/ecshop/Mapper.xml.vm");
        templates.add("template/ecshop/Service.java.vm");
        templates.add("template/ecshop/Form.jsp.vm");
        templates.add("template/ecshop/List.js.vm");
        templates.add("template/ecshop/List.jsp.vm");*/

        //福建智游官网    Html
		/*templates.add("template/zyserver/Api.java.vm");
		templates.add("template/zyserver/Page.java.vm");
		templates.add("template/zyserver/Repository.java.vm");
		templates.add("template/zyserver/Service.java.vm");
		templates.add("template/zyserver/_list.html.vm");
		templates.add("template/zyserver/_edit.html.vm");*/

        //共享洗衣机     JSP
		/*templates.add("template/wsh/Controller.java.vm");
		templates.add("template/wsh/Entity.java.vm");
		templates.add("template/wsh/Mapper.java.vm");
		templates.add("template/wsh/Mapper.xml.vm");
		templates.add("template/wsh/Service.java.vm");
		templates.add("template/wsh/Form.jsp.vm");
		templates.add("template/wsh/List.js.vm");
		templates.add("template/wsh/List.jsp.vm");*/

        //
        /*	templates.add("template/Entity.java.vm");
		templates.add("template/Dao.java.vm");
		templates.add("template/Dao.xml.vm");
		templates.add("template/Service.java.vm.vm");
		templates.add("template/ServiceImpl.java.vm");
		templates.add("template/Controller.java.vm");
		templates.add("template/list.html.vm");
		templates.add("template/list.js.vm");
		templates.add("template/menu.sql.vm");*/

        templates.add("template/version2/index.vue.vm" );
        templates.add("template/version2/index.js.vm" );
        templates.add("template/version2/indexTableOption.js.vm" );
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                 List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        boolean hasBigDecimal = false;
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for(Map<String, String> column : columns){
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType" );
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        Velocity.init(prop);
        String mainPath = config.getString("mainPath" );
        mainPath = StringUtils.isBlank(mainPath) ? "io.renren" : mainPath;
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());//首字母大写
        map.put("classname", tableEntity.getClassname());//首字母小写
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8" );
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package" ))));
                IOUtils.write(sw.toString(), zip, "UTF-8" );
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "" );
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "" );
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties" );
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + File.separator;
        }


        //第一种方式
        /*if (template.contains("Entity.java.vm" )) {
            return packagePath + "entity" + File.separator + className + ".java";
        }
        if (template.contains("Mapper.java.vm" )) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }
        if (template.contains("Service.java.vm" )) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }
        if (template.contains("ServiceImpl.java.vm" )) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }
        if (template.contains("Controller.java.vm" )) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }
        if (template.contains("Mapper.xml.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator  + File.separator + className + "Mapper.xml";
        }
        if (template.contains("menu.sql.vm" )) {
            return className.toLowerCase() + "_menu.sql";
        }
        if (template.contains("index.vue.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator+
                    File.separator  + File.separator + className.toLowerCase() + ".vue";
        }
        if (template.contains("add-or-update.vue.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator +
                    File.separator  + File.separator + className.toLowerCase() + "-add-or-update.vue";
        }*/


        /*String packagePath = "main" + File.separator + "java" + File.separator;

		if(StringUtils.isNotBlank(packageName)){
			packagePath += packageName.replace(".", File.separator) + File.separator;
		}*/


        //人人系统模板

		/*if(template.contains("Entity.java.vm")){
			return packagePath + "entity" + File.separator + className + "Entity.java";
		}

		if(template.contains("Dao.java.vm")){
			return packagePath + "dao" + File.separator + className + "Dao.java";
		}

		if(template.contains("Service.java.vm.vm.vm")){
			return packagePath + "service" + File.separator + className + "Service.java.vm.vm";
		}

		if(template.contains("ServiceImpl.java.vm")){
			return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
		}

		if(template.contains("Controller.java.vm.vm")){
			return packagePath + "controller" + File.separator + className + "Controller.java.vm";
		}

		if(template.contains("Dao.xml.vm")){
			return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + "generator" + File.separator + className + "Dao.xml";
		}

		if(template.contains("list.html.vm")){
			return "main" + File.separator + "resources" + File.separator + "views" + File.separator
					+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".html";
		}

		if(template.contains("list.js.vm")){
			return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
					+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".js";
		}

		if(template.contains("menu.sql.vm")){
			return className.toLowerCase() + "_menu.sql";
		}*/



        //智游官网后台管理系统模板
	/*	if(template.contains("Api.java.vm")){
			return packagePath + "controller" + File.separator+ "api" + File.separator  + className + "Api.java";
		}
		if(template.contains("Page.java.vm")){
			return packagePath  + "controller" + File.separator+ "page" + File.separator  + className + "Page.java";
		}
		if(template.contains("Service.java.vm")){
			return packagePath + "service" + File.separator  + className + "Service.java";
		}
		if(template.contains("Repository.java.vm")){
			return packagePath + "repository"  + File.separator  + className + "Repository.java";
		}
		if(template.contains("_list.html.vm")){
			return "main" + File.separator + "resources" + File.separator + "templates" + File.separator + className.toLowerCase()+"_list.html" ;
		}
		if(template.contains("_edit.html.vm")){
			return "main" + File.separator + "resources" + File.separator + "templates" + File.separator + className.toLowerCase()+"_edit.html" ;
		}*/


        //共享洗衣机模板
       /* String packagePath = "main"+File.separator;
        if(template.contains("Entity.java.vm")){
            return  packagePath + "entity" + File.separator+className + ".java";
        }
        if(template.contains("Controller.java.vm")){
            return   packagePath + "controller" + File.separator+className+"Controller.java";
        }
        if(template.contains("Mapper.java.vm")){
            return   packagePath + "mapper" + File.separator+className + "Mapper.java";
        }
        if(template.contains("Service.java.vm")){
            return   packagePath + "service" + File.separator+className + "Service.java";
        }
        if(template.contains("Mapper.xml.vm")){
            return   packagePath + "xml" + File.separator+className + "Mapper.xml";
        }

        String firstLower = className.substring(0,1).toLowerCase() + className.substring(1,className.length());

        if(template.contains("Form.jsp.vm")){
            return packagePath+ "resources" + File.separator + className.toLowerCase() + File.separator + firstLower+"Form.jsp" ;
        }
        if(template.contains("List.js.vm")){
            return packagePath+"resources" + File.separator + className.toLowerCase() + File.separator + firstLower+"List.js" ;
        }
        if(template.contains("List.jsp.vm")){
            return packagePath+"resources" + File.separator + className.toLowerCase() + File.separator + firstLower+"List.jsp" ;
        }*/

        if (template.contains("index.vue.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator+
                    File.separator  + File.separator + className.toLowerCase() + ".vue";
        }
        if (template.contains("index.js.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator
                    +"api"+File.separator + className.toLowerCase() + ".js";
        }
        if (template.contains("indexTableOption.js.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator
                    +"const"+File.separator+ className.toLowerCase() + ".js";
        }


        return null;
    }
}
