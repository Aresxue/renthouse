package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.annotations.AresProvider;
import com.asiainfo.frame.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.asiainfo.frame.remote.invoke.ProviderController.addMethodHandle;
import static com.asiainfo.frame.remote.invoke.ProviderController.getMethodHandle;

/**
 * @author: Ares
 * @date: 2020/4/28 13:44
 * @description: 提供者Bean注册
 * @version: JDK 1.8
 */
public class ProviderAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAnnotationBeanPostProcessor.class);

    private final Set<String> packagesToScan;
    private Environment environment;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException
    {
        Set<String> resolvedPackagesToScan = packagesToScan.stream().filter(StringUtil::isNotBlank).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(resolvedPackagesToScan))
        {
            registerProviderBeans(resolvedPackagesToScan, registry);
        }
        else if (LOGGER.isWarnEnabled())
        {
            LOGGER.warn("packagesToScan is empty , ProviderBean registry will be ignored!");
        }

    }

    private void registerProviderBeans(Set<String> packagesToScan, BeanDefinitionRegistry registry)
    {
        AresClassPathBeanDefinitionScanner scanner = new AresClassPathBeanDefinitionScanner(registry, environment, resourceLoader);
        BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);
        scanner.setBeanNameGenerator(beanNameGenerator);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AresProvider.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(com.asiainfo.frame.annotations.AresProvider.class));
        packagesToScan.forEach(packageToScan -> {
            scanner.scan(packageToScan);
            Set<BeanDefinitionHolder> beanDefinitionHolders = findServiceBeanDefinitionHolders(scanner, packageToScan, registry, beanNameGenerator);
            if (!CollectionUtils.isEmpty(beanDefinitionHolders))
            {
                beanDefinitionHolders.forEach(beanDefinitionHolder -> registerProviderBean(beanDefinitionHolder,registry,scanner));

                if (LOGGER.isInfoEnabled())
                {
                    LOGGER.info(beanDefinitionHolders.size() + " annotated Ares's @AresProvider Components { " + beanDefinitionHolders + " } were scanned under package[" + packageToScan + "]");
                }
            }
            else if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("No Spring Bean annotating Ares's @AresProvider was found under package[" + packageToScan + "]");
            }
        });
    }

    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry)
    {
        BeanNameGenerator beanNameGenerator = null;
        if (registry instanceof SingletonBeanRegistry)
        {
            SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton("org.springframework.context.annotation.internalConfigurationBeanNameGenerator");
        }

        if (beanNameGenerator == null)
        {
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("BeanNameGenerator bean can't be found in BeanFactory with name [org.springframework.context.annotation.internalConfigurationBeanNameGenerator]");
                LOGGER.info("BeanNameGenerator will be a instance of " + AnnotationBeanNameGenerator.class.getName() + " , it maybe a potential problem on bean name generation.");
            }

            beanNameGenerator = new AnnotationBeanNameGenerator();
        }

        return beanNameGenerator;
    }

    private void registerProviderBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, AresClassPathBeanDefinitionScanner scanner)
    {
        Class<?> beanClass = resolveClass(beanDefinitionHolder);
        Annotation service = findProviderAnnotation(beanClass);
        AnnotationAttributes serviceAnnotationAttributes = AnnotationUtils.getAnnotationAttributes(service, false, false);

        Class<?> interfaceClass = com.asiainfo.frame.remote.invoke.AnnotationUtils.resolveServiceInterfaceClass(serviceAnnotationAttributes, beanClass);
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

        String group = serviceAnnotationAttributes.getString("group");
        String version = serviceAnnotationAttributes.getString("version");
        String interfaceName = interfaceClass.getName();

        String beanName = buildBeanName(interfaceName, group, version);

        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        if (scanner.checkCandidate(beanName, beanDefinition))
        {
            registry.registerBeanDefinition(beanName, beanDefinition);
        }

        Method[] methods = interfaceClass.getDeclaredMethods();


        for (Method method : methods)
        {
            StringJoiner uniqueKey = new StringJoiner("#");
            uniqueKey.add(interfaceName);
            uniqueKey.add(method.getName());
            uniqueKey.add(group);
            uniqueKey.add(version);
            String parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(","));
            uniqueKey.add(parameterTypes);

            String uniqueId = uniqueKey.toString();
            MethodHandle methodHandle = getMethodHandle(beanClass, method, uniqueId);

            if (null != methodHandle)
            {
                addMethodHandle(uniqueId, methodHandle);
            }
        }
    }

    public static String buildBeanName(String interfaceName, String group, String version)
    {
        StringJoiner stringJoiner = new StringJoiner(":");
        stringJoiner.add("AresBean");
        stringJoiner.add(interfaceName);
        stringJoiner.add(group);
        stringJoiner.add(version);
        return stringJoiner.toString();
    }


    private Annotation findProviderAnnotation(Class<?> beanClass)
    {
        Annotation provider = AnnotatedElementUtils.findMergedAnnotation(beanClass, AresProvider.class);
        if (provider == null)
        {
            provider = AnnotatedElementUtils.findMergedAnnotation(beanClass, AresProvider.class);
        }

        return provider;
    }

    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder)
    {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return resolveClass(beanDefinition);
    }

    private Class<?> resolveClass(BeanDefinition beanDefinition)
    {
        String beanClassName = beanDefinition.getBeanClassName();
        return ClassUtils.resolveClassName(beanClassName, classLoader);
    }


    private Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(ClassPathBeanDefinitionScanner scanner, String packageToScan, BeanDefinitionRegistry registry, BeanNameGenerator beanNameGenerator)
    {
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>(beanDefinitions.size());
        beanDefinitions.forEach(beanDefinition -> {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        });

        return beanDefinitionHolders;
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
    }

    public ProviderAnnotationBeanPostProcessor(Set<String> packagesToScan)
    {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment)
    {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(@NonNull ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

}

