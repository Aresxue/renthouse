package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.javassist.CannotCompileException;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtField;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.NotFoundException;
import org.apache.ibatis.javassist.bytecode.AccessFlag;
import org.apache.ibatis.javassist.bytecode.AnnotationsAttribute;
import org.apache.ibatis.javassist.bytecode.ConstPool;
import org.apache.ibatis.javassist.bytecode.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author: Ares
 * @date: 2020/3/31 16:53
 * @description: 动态数据源自定义SqlSessionFactory
 * @version: JDK 1.8
 */
public class DynamicSqlSessionFactoryConfiguration implements ApplicationContextInitializer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSqlSessionFactoryConfiguration.class);

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext)
    {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass;
        try
        {
            ctClass = classPool.get("org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration");

            // 添加属性dynamicDataSource
            CtClass dynamicDataSourceCtClass = classPool.get("com.asiainfo.strategy.multiple.datasources.DynamicDataSource");
            CtField ctField = new CtField(dynamicDataSourceCtClass, "dynamicDataSource", ctClass);
            ctField.setModifiers(AccessFlag.PRIVATE);
            ConstPool constPool = ctField.getFieldInfo().getConstPool();
            AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            Annotation annotation = new Annotation("org.springframework.beans.factory.annotation.Autowired", constPool);
            attribute.setAnnotation(annotation);
            ctField.getFieldInfo().addAttribute(attribute);
            ctClass.addField(ctField);

            CtMethod ctMethod = ctClass.getDeclaredMethod("sqlSessionFactory");
            String sqlSessionFactoryBody = "{\n" + "        org.mybatis.spring.SqlSessionFactoryBean factory = new org.mybatis.spring.SqlSessionFactoryBean();\n" + "        factory.setDataSource(dynamicDataSource);\n" + "        factory.setTransactionFactory(new com.asiainfo.strategy.multiple.datasources.DynamicDataSourceTransactionFactory());\n" + "        factory.setVfs(org.mybatis.spring.boot.autoconfigure.SpringBootVFS.class);\n" + "        if (org.springframework.util.StringUtils.hasText(this.properties.getConfigLocation())) {\n" + "            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));\n" + "        }\n" + "\n" + "        this.applyConfiguration(factory);\n" + "        if (this.properties.getConfigurationProperties() != null) {\n" + "            factory.setConfigurationProperties(this.properties.getConfigurationProperties());\n" + "        }\n" + "\n" + "        if (!org.springframework.util.ObjectUtils.isEmpty(this.interceptors)) {\n" + "            factory.setPlugins(this.interceptors);\n" + "        }\n" + "\n" + "        if (this.databaseIdProvider != null) {\n" + "            factory.setDatabaseIdProvider(this.databaseIdProvider);\n" + "        }\n" + "\n" + "        if (org.springframework.util.StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {\n" + "            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());\n" + "        }\n" + "\n" + "        if (this.properties.getTypeAliasesSuperType() != null) {\n" + "            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());\n" + "        }\n" + "\n" + "        if (org.springframework.util.StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {\n" + "            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());\n" + "        }\n" + "\n" + "        if (!org.springframework.util.ObjectUtils.isEmpty(this.typeHandlers)) {\n" + "            factory.setTypeHandlers(this.typeHandlers);\n" + "        }\n" + "\n" + "        if (!org.springframework.util.ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {\n" + "            factory.setMapperLocations(this.properties.resolveMapperLocations());\n" + "        }\n" + "\n" + "        java.beans.PropertyDescriptor[] propertyDescriptors = (new org.springframework.beans.BeanWrapperImpl(org.mybatis.spring.SqlSessionFactoryBean.class)).getPropertyDescriptors();\n" + "\n" + "        java.util.Set/*<String>*/ factoryPropertyNames = new java.util.HashSet/*<>*/();\n" + "        for (int i = 0; i <propertyDescriptors.length ; i++)\n" + "        {\n" + "            factoryPropertyNames.add(propertyDescriptors[i]);\n" + "        }\n" + "\n" + "        java.lang.Class/*<? extends LanguageDriver>*/ defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();\n" + "        if (factoryPropertyNames.contains(\"scriptingLanguageDrivers\") && !org.springframework.util.ObjectUtils.isEmpty(this.languageDrivers)) {\n" + "            factory.setScriptingLanguageDrivers(this.languageDrivers);\n" + "            if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {\n" + "                defaultLanguageDriver = this.languageDrivers[0].getClass();\n" + "            }\n" + "        }\n" + "\n" + "        if (factoryPropertyNames.contains(\"defaultScriptingLanguageDriver\")) {\n" + "            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);\n" + "        }\n" + "\n" + "        return factory.getObject();\n" + "    }";
            ctMethod.setBody(sqlSessionFactoryBody);
            ctClass.toClass();
        } catch (NotFoundException e)
        {
            LOGGER.error("寻找mybatis自动加载类时失败: ", e);
        } catch (CannotCompileException e)
        {
            LOGGER.error("编译自定义sqlSessionFactory时失败: ", e);
        }
    }

}
