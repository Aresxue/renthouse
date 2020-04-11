package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.javassist.CannotCompileException;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.NotFoundException;
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
public class DynamicSqlSessionFactoryInitializer implements ApplicationContextInitializer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSqlSessionFactoryInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext)
    {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass;
        try
        {
            ctClass = classPool.get("org.mybatis.spring.transaction.SpringManagedTransactionFactory");
            String[] classNames = new String[]{"javax.sql.DataSource", "org.apache.ibatis.session.TransactionIsolationLevel", "boolean"};
            CtMethod ctMethod = ctClass.getDeclaredMethod("newTransaction", classPool.get(classNames));
            String newTransactionBody = "{ return new com.asiainfo.strategy.multiple.datasources.DynamicDataSourceTransaction($1);}";

            ctMethod.setBody(newTransactionBody);
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
