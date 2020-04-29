package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.annotations.AresConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Ares
 * @date: 2020/4/28 19:11
 * @description:
 * @version: JDK 1.8
 */
@Configuration
public class ConsumerAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware, EnvironmentAware
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAnnotationBeanPostProcessor.class);

    private final Set<Class<? extends Annotation>> aresConsumerAnnotationTypes = new LinkedHashSet<>(4);
    private int order = 2147483645;
    @Nullable
    private ConfigurableListableBeanFactory beanFactory;
    private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));
    private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);
    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


    private String requiredParameterName = "required";
    private boolean requiredParameterValue = true;

    private Map<String, String> consumerParams = new HashMap<>();

    private Environment environment;

    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    public ConsumerAnnotationBeanPostProcessor()
    {
        this.aresConsumerAnnotationTypes.add(AresConsumer.class);
        try
        {
            this.aresConsumerAnnotationTypes.add((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Inject", ConsumerAnnotationBeanPostProcessor.class.getClassLoader()));
            LOGGER.trace("JSR-330 'javax.inject.Inject' annotation found and supported for aresConsumer");
        } catch (ClassNotFoundException var2)
        {
        }

    }

    public void setAresConsumerAnnotationType(Class<? extends Annotation> aresConsumerAnnotationTypes)
    {
        Assert.notNull(aresConsumerAnnotationTypes, "'aresConsumerAnnotationType' must not be null");
        this.aresConsumerAnnotationTypes.clear();
        this.aresConsumerAnnotationTypes.add(aresConsumerAnnotationTypes);
    }

    public void setAresConsumerAnnotationTypes(Set<Class<? extends Annotation>> aresConsumerAnnotationTypes)
    {
        Assert.notEmpty(aresConsumerAnnotationTypes, "'aresConsumerAnnotationTypes' must not be empty");
        this.aresConsumerAnnotationTypes.clear();
        this.aresConsumerAnnotationTypes.addAll(aresConsumerAnnotationTypes);
    }

    public void setRequiredParameterName(String requiredParameterName)
    {
        this.requiredParameterName = requiredParameterName;
    }

    public void setRequiredParameterValue(boolean requiredParameterValue)
    {
        this.requiredParameterValue = requiredParameterValue;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    @Override
    public int getOrder()
    {
        return this.order;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory)
    {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory))
        {
            throw new IllegalArgumentException("ConsumerAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        else
        {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName)
    {
        InjectionMetadata metadata = this.findAresConsumerMetadata(beanName, beanType, (PropertyValues) null);
        metadata.checkConfigMembers(beanDefinition);
    }

    @Override
    public void resetBeanDefinition(String beanName)
    {
        this.lookupMethodsChecked.remove(beanName);
        this.injectionMetadataCache.remove(beanName);
    }

    @Override
    @Nullable
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeanCreationException
    {
        if (!this.lookupMethodsChecked.contains(beanName))
        {
            if (org.springframework.core.annotation.AnnotationUtils.isCandidateClass(beanClass, Lookup.class))
            {
                try
                {
                    Class targetClass = beanClass;

                    do
                    {
                        ReflectionUtils.doWithLocalMethods(targetClass, (method) -> {
                            Lookup lookup = (Lookup) method.getAnnotation(Lookup.class);
                            if (lookup != null)
                            {
                                Assert.state(this.beanFactory != null, "No BeanFactory available");
                                LookupOverride override = new LookupOverride(method, lookup.value());

                                try
                                {
                                    RootBeanDefinition mbd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
                                    mbd.getMethodOverrides().addOverride(override);
                                } catch (NoSuchBeanDefinitionException var6)
                                {
                                    throw new BeanCreationException(beanName, "Cannot apply @Lookup to beans without corresponding bean definition");
                                }
                            }

                        });
                        targetClass = targetClass.getSuperclass();
                    } while(targetClass != null && targetClass != Object.class);
                } catch (IllegalStateException var22)
                {
                    throw new BeanCreationException(beanName, "Lookup method resolution failed", var22);
                }
            }

            this.lookupMethodsChecked.add(beanName);
        }

        Constructor<?>[] candidateConstructors = (Constructor[]) this.candidateConstructorsCache.get(beanClass);
        if (candidateConstructors == null)
        {
            synchronized (this.candidateConstructorsCache)
            {
                candidateConstructors = (Constructor[]) this.candidateConstructorsCache.get(beanClass);
                if (candidateConstructors == null)
                {
                    Constructor[] rawCandidates;
                    try
                    {
                        rawCandidates = beanClass.getDeclaredConstructors();
                    } catch (Throwable var20)
                    {
                        throw new BeanCreationException(beanName, "Resolution of declared constructors on bean Class [" + beanClass.getName() + "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", var20);
                    }

                    List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
                    Constructor<?> requiredConstructor = null;
                    Constructor<?> defaultConstructor = null;
                    Constructor<?> primaryConstructor = BeanUtils.findPrimaryConstructor(beanClass);
                    int nonSyntheticConstructors = 0;
                    Constructor[] var11 = rawCandidates;
                    int var12 = rawCandidates.length;
                    int var13 = 0;

                    while(true)
                    {
                        if (var13 >= var12)
                        {
                            if (!candidates.isEmpty())
                            {
                                if (requiredConstructor == null)
                                {
                                    if (defaultConstructor != null)
                                    {
                                        candidates.add(defaultConstructor);
                                    }
                                    else if (candidates.size() == 1 && LOGGER.isInfoEnabled())
                                    {
                                        LOGGER.info("Inconsistent constructor declaration on bean with name '" + beanName + "': single aresConsumer-marked constructor flagged as optional - this constructor is effectively required since there is no default constructor to fall back to: " + candidates.get(0));
                                    }
                                }

                                candidateConstructors = (Constructor[]) candidates.toArray(new Constructor[0]);
                            }
                            else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0)
                            {
                                candidateConstructors = new Constructor[]{rawCandidates[0]};
                            }
                            else if (nonSyntheticConstructors == 2 && primaryConstructor != null && defaultConstructor != null && !primaryConstructor.equals(defaultConstructor))
                            {
                                candidateConstructors = new Constructor[]{primaryConstructor, defaultConstructor};
                            }
                            else if (nonSyntheticConstructors == 1 && primaryConstructor != null)
                            {
                                candidateConstructors = new Constructor[]{primaryConstructor};
                            }
                            else
                            {
                                candidateConstructors = new Constructor[0];
                            }

                            this.candidateConstructorsCache.put(beanClass, candidateConstructors);
                            break;
                        }

                        label144:
                        {
                            Constructor<?> candidate = var11[var13];
                            if (!candidate.isSynthetic())
                            {
                                ++nonSyntheticConstructors;
                            }
                            else if (primaryConstructor != null)
                            {
                                break label144;
                            }

                            MergedAnnotation<?> ann = this.findAresConsumerAnnotation(candidate);
                            if (ann == null)
                            {
                                Class<?> userClass = ClassUtils.getUserClass(beanClass);
                                if (userClass != beanClass)
                                {
                                    try
                                    {
                                        Constructor<?> superCtor = userClass.getDeclaredConstructor(candidate.getParameterTypes());
                                        ann = this.findAresConsumerAnnotation(superCtor);
                                    } catch (NoSuchMethodException var19)
                                    {
                                    }
                                }
                            }

                            if (ann != null)
                            {
                                if (requiredConstructor != null)
                                {
                                    throw new BeanCreationException(beanName, "Invalid aresConsumer-marked constructor: " + candidate + ". Found constructor with 'required' AresConsumer annotation already: " + requiredConstructor);
                                }

                                boolean required = this.determineRequiredStatus(ann);
                                if (required)
                                {
                                    if (!candidates.isEmpty())
                                    {
                                        throw new BeanCreationException(beanName, "Invalid aresConsumer-marked constructors: " + candidates + ". Found constructor with 'required' AresConsumer annotation: " + candidate);
                                    }

                                    requiredConstructor = candidate;
                                }

                                candidates.add(candidate);
                            }
                            else if (candidate.getParameterCount() == 0)
                            {
                                defaultConstructor = candidate;
                            }
                        }

                        ++var13;
                    }
                }
            }
        }

        return candidateConstructors.length > 0 ? candidateConstructors : null;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
    {
        InjectionMetadata metadata = this.findAresConsumerMetadata(beanName, bean.getClass(), pvs);

        try
        {
            metadata.inject(bean, beanName, pvs);
            return pvs;
        } catch (BeanCreationException var6)
        {
            throw var6;
        } catch (Throwable var7)
        {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", var7);
        }
    }


    public void processInjection(Object bean) throws BeanCreationException
    {
        Class<?> clazz = bean.getClass();
        InjectionMetadata metadata = this.findAresConsumerMetadata(clazz.getName(), clazz, (PropertyValues) null);

        try
        {
            metadata.inject(bean, (String) null, (PropertyValues) null);
        } catch (BeanCreationException var5)
        {
            throw var5;
        } catch (Throwable var6)
        {
            throw new BeanCreationException("Injection of aresConsumer dependencies failed for class [" + clazz + "]", var6);
        }
    }

    private InjectionMetadata findAresConsumerMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs)
    {
        String cacheKey = StringUtils.hasLength(beanName) ? beanName : clazz.getName();
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz))
        {
            synchronized (this.injectionMetadataCache)
            {
                metadata = (InjectionMetadata) this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz))
                {
                    if (metadata != null)
                    {
                        metadata.clear(pvs);
                    }

                    metadata = this.buildAresConsumerMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }

        return metadata;
    }

    private InjectionMetadata buildAresConsumerMetadata(Class<?> clazz)
    {
        if (!org.springframework.core.annotation.AnnotationUtils.isCandidateClass(clazz, this.aresConsumerAnnotationTypes))
        {
            return InjectionMetadata.EMPTY;
        }
        else
        {
            List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
            Class targetClass = clazz;

            do
            {
                List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();
                ReflectionUtils.doWithLocalFields(targetClass, (field) -> {
                    MergedAnnotation<?> ann = this.findAresConsumerAnnotation(field);
                    if (ann != null)
                    {
                        if (Modifier.isStatic(field.getModifiers()))
                        {
                            if (LOGGER.isInfoEnabled())
                            {
                                LOGGER.info("AresConsumer annotation is not supported on static fields: " + field);
                            }

                            return;
                        }

                        boolean required = determineRequiredStatus(ann);

                        determineConsumerParams(ann);

                        currElements.add(new ConsumerFieldElement(field, required));
                    }

                });
                ReflectionUtils.doWithLocalMethods(targetClass, (method) -> {
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                    if (BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod))
                    {
                        MergedAnnotation<?> ann = this.findAresConsumerAnnotation(bridgedMethod);
                        if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz)))
                        {
                            if (Modifier.isStatic(method.getModifiers()))
                            {
                                if (LOGGER.isInfoEnabled())
                                {
                                    LOGGER.info("AresConsumer annotation is not supported on static methods: " + method);
                                }

                                return;
                            }

                            if (method.getParameterCount() == 0 && LOGGER.isInfoEnabled())
                            {
                                LOGGER.info("AresConsumer annotation should only be used on methods with parameters: " + method);
                            }

                            boolean required = this.determineRequiredStatus(ann);
                            PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                            currElements.add(new ConsumerMethodElement(method, required, pd));
                        }

                    }
                });
                elements.addAll(0, currElements);
                targetClass = targetClass.getSuperclass();
            } while(targetClass != null && targetClass != Object.class);

            return InjectionMetadata.forElements(elements, clazz);
        }
    }

    @Nullable
    private MergedAnnotation<?> findAresConsumerAnnotation(AccessibleObject ao)
    {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        Iterator var3 = this.aresConsumerAnnotationTypes.iterator();

        MergedAnnotation annotation;
        do
        {
            if (!var3.hasNext())
            {
                return null;
            }

            Class<? extends Annotation> type = (Class) var3.next();
            annotation = annotations.get(type);
        } while(!annotation.isPresent());

        return annotation;
    }

    protected boolean determineRequiredStatus(MergedAnnotation<?> ann)
    {
        AnnotationAttributes annotationAttributes = ann.asMap((mergedAnnotation) -> new AnnotationAttributes(mergedAnnotation.getType()));
        return !annotationAttributes.containsKey(this.requiredParameterName) || this.requiredParameterValue == annotationAttributes.getBoolean(this.requiredParameterName);
    }

    protected void determineConsumerParams(MergedAnnotation<?> ann)
    {
        consumerParams.put("center", ann.getString("center"));
        consumerParams.put("group", ann.getString("group"));
        consumerParams.put("version", ann.getString("version"));
    }

    protected <T> Map<String, T> findConsumerCandidates(Class<T> type) throws BeansException
    {
        if (this.beanFactory == null)
        {
            throw new IllegalStateException("No BeanFactory configured - override the getBeanOfType method or specify the 'beanFactory' property");
        }
        else
        {
            return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
        }
    }

    private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames)
    {
        if (beanName != null)
        {
            Iterator var3 = autowiredBeanNames.iterator();

            while(var3.hasNext())
            {
                String aresConsumerBeanName = (String) var3.next();
                if (this.beanFactory != null && this.beanFactory.containsBean(aresConsumerBeanName))
                {
                    this.beanFactory.registerDependentBean(aresConsumerBeanName, beanName);
                }

                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("aresConsumer by type from bean name '" + beanName + "' to bean named '" + aresConsumerBeanName + "'");
                }
            }
        }

    }

    @Nullable
    private Object resolvedCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument)
    {
        if (cachedArgument instanceof DependencyDescriptor)
        {
            DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
            Assert.state(this.beanFactory != null, "No BeanFactory available");
            return this.beanFactory.resolveDependency(descriptor, beanName, (Set) null, (TypeConverter) null);
        }
        else
        {
            return cachedArgument;
        }
    }

    @Override
    public void setEnvironment(@NonNull Environment environment)
    {
        this.environment = environment;
    }

    private static class ShortcutDependencyDescriptor extends DependencyDescriptor
    {
        private final String shortcut;
        private final Class<?> requiredType;

        public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType)
        {
            super(original);
            this.shortcut = shortcut;
            this.requiredType = requiredType;
        }

        @Override
        public Object resolveShortcut(BeanFactory beanFactory)
        {
            return beanFactory.getBean(this.shortcut, this.requiredType);
        }
    }

    private class ConsumerMethodElement extends InjectionMetadata.InjectedElement
    {
        private final boolean required;
        private volatile boolean cached = false;
        @Nullable
        private volatile Object[] cachedMethodArguments;

        public ConsumerMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd)
        {
            super(method, pd);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable
        {
            if (!this.checkPropertySkipping(pvs))
            {
                Method method = (Method) this.member;
                Object[] arguments;
                if (this.cached)
                {
                    arguments = this.resolveCachedArguments(beanName);
                }
                else
                {
                    int argumentCount = method.getParameterCount();
                    arguments = new Object[argumentCount];
                    DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
                    Set<String> aresConsumerBeans = new LinkedHashSet<>(argumentCount);
                    Assert.state(ConsumerAnnotationBeanPostProcessor.this.beanFactory != null, "No BeanFactory available");
                    TypeConverter typeConverter = ConsumerAnnotationBeanPostProcessor.this.beanFactory.getTypeConverter();

                    for (int ix = 0; ix < arguments.length; ++ix)
                    {
                        MethodParameter methodParam = new MethodParameter(method, ix);
                        DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required);
                        currDesc.setContainingClass(bean.getClass());
                        descriptors[ix] = currDesc;

                        try
                        {
                            Object arg = ConsumerAnnotationBeanPostProcessor.this.beanFactory.resolveDependency(currDesc, beanName, aresConsumerBeans, typeConverter);
                            if (arg == null && !this.required)
                            {
                                arguments = null;
                                break;
                            }

                            arguments[ix] = arg;
                        } catch (BeansException var19)
                        {
                            throw new UnsatisfiedDependencyException((String) null, beanName, new InjectionPoint(methodParam), var19);
                        }
                    }

                    synchronized (this)
                    {
                        if (!this.cached)
                        {
                            if (arguments != null)
                            {
                                DependencyDescriptor[] cachedMethodArguments = (DependencyDescriptor[]) Arrays.copyOf(descriptors, arguments.length);
                                ConsumerAnnotationBeanPostProcessor.this.registerDependentBeans(beanName, aresConsumerBeans);
                                if (aresConsumerBeans.size() == argumentCount)
                                {
                                    Iterator<String> it = aresConsumerBeans.iterator();
                                    Class<?>[] paramTypes = method.getParameterTypes();

                                    for (int i = 0; i < paramTypes.length; ++i)
                                    {
                                        String aresConsumerBeanName = (String) it.next();
                                        if (ConsumerAnnotationBeanPostProcessor.this.beanFactory.containsBean(aresConsumerBeanName) && ConsumerAnnotationBeanPostProcessor.this.beanFactory.isTypeMatch(aresConsumerBeanName, paramTypes[i]))
                                        {
                                            cachedMethodArguments[i] = new ConsumerAnnotationBeanPostProcessor.ShortcutDependencyDescriptor(descriptors[i], aresConsumerBeanName, paramTypes[i]);
                                        }
                                    }
                                }

                                this.cachedMethodArguments = cachedMethodArguments;
                            }
                            else
                            {
                                this.cachedMethodArguments = null;
                            }

                            this.cached = true;
                        }
                    }
                }

                if (arguments != null)
                {
                    try
                    {
                        ReflectionUtils.makeAccessible(method);
                        method.invoke(bean, arguments);
                    } catch (InvocationTargetException var17)
                    {
                        throw var17.getTargetException();
                    }
                }

            }
        }

        @Nullable
        private Object[] resolveCachedArguments(@Nullable String beanName)
        {
            Object[] cachedMethodArguments = this.cachedMethodArguments;
            if (cachedMethodArguments == null)
            {
                return null;
            }
            else
            {
                Object[] arguments = new Object[cachedMethodArguments.length];

                for (int i = 0; i < arguments.length; ++i)
                {
                    arguments[i] = ConsumerAnnotationBeanPostProcessor.this.resolvedCachedArgument(beanName, cachedMethodArguments[i]);
                }

                return arguments;
            }
        }
    }

    private static class AnnotationUtils
    {
        public static <T> T getAttribute(AnnotationAttributes attributes, String name)
        {
            return (T) attributes.get(name);
        }

        public static AnnotationAttributes getMergedAttributes(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType, PropertyResolver propertyResolver, boolean ignoreDefaultValue, String... ignoreAttributeNames)
        {
            Annotation annotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, annotationType);
            return annotation == null ? null : AnnotationAttributes.fromMap(getAttributes(annotation, propertyResolver, ignoreDefaultValue, ignoreAttributeNames));
        }

        public static Map<String, Object> getAttributes(Annotation annotation, PropertyResolver propertyResolver, boolean ignoreDefaultValue, String... ignoreAttributeNames)
        {
            if (annotation == null)
            {
                return Collections.emptyMap();
            }
            else
            {
                Map<String, Object> attributes = org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes(annotation);
                Map<String, Object> actualAttributes = new LinkedHashMap();
                Iterator var6 = attributes.entrySet().iterator();

                while(true)
                {
                    String attributeName;
                    Object attributeValue;
                    do
                    {
                        do
                        {
                            do
                            {
                                if (!var6.hasNext())
                                {
                                    return resolvePlaceholders(actualAttributes, propertyResolver, ignoreAttributeNames);
                                }

                                Map.Entry<String, Object> entry = (Map.Entry) var6.next();
                                attributeName = (String) entry.getKey();
                                attributeValue = entry.getValue();
                            } while(ignoreDefaultValue && ObjectUtils.nullSafeEquals(attributeValue, org.springframework.core.annotation.AnnotationUtils.getDefaultValue(annotation, attributeName)));
                        } while(attributeValue.getClass().isAnnotation());
                    } while(attributeValue.getClass().isArray() && attributeValue.getClass().getComponentType().isAnnotation());

                    actualAttributes.put(attributeName, attributeValue);
                }
            }
        }

        public static Map<String, Object> resolvePlaceholders(Map<String, Object> sourceAnnotationAttributes, PropertyResolver propertyResolver, String... ignoreAttributeNames)
        {
            if (CollectionUtils.isEmpty(sourceAnnotationAttributes))
            {
                return Collections.emptyMap();
            }
            else
            {
                Map<String, Object> resolvedAnnotationAttributes = new LinkedHashMap();
                Iterator var4 = sourceAnnotationAttributes.entrySet().iterator();

                while(true)
                {
                    Map.Entry entry;
                    String attributeName;
                    do
                    {
                        if (!var4.hasNext())
                        {
                            return Collections.unmodifiableMap(resolvedAnnotationAttributes);
                        }

                        entry = (Map.Entry) var4.next();
                        attributeName = (String) entry.getKey();
                    } while(ObjectUtils.containsElement(ignoreAttributeNames, attributeName));

                    Object attributeValue = entry.getValue();
                    if (attributeValue instanceof String)
                    {
                        attributeValue = resolvePlaceholders(String.valueOf(attributeValue), propertyResolver);
                    }
                    else if (attributeValue instanceof String[])
                    {
                        String[] values = (String[]) ((String[]) attributeValue);

                        for (int i = 0; i < values.length; ++i)
                        {
                            values[i] = resolvePlaceholders(values[i], propertyResolver);
                        }

                        attributeValue = values;
                    }

                    resolvedAnnotationAttributes.put(attributeName, attributeValue);
                }
            }
        }

        private static String resolvePlaceholders(String attributeValue, PropertyResolver propertyResolver)
        {
            String resolvedValue = attributeValue;
            if (propertyResolver != null)
            {
                resolvedValue = propertyResolver.resolvePlaceholders(attributeValue);
                resolvedValue = StringUtils.trimWhitespace(resolvedValue);
            }

            return resolvedValue;
        }
    }

    private class ConsumerFieldElement extends InjectionMetadata.InjectedElement
    {
        private final boolean required;
        private volatile boolean cached = false;
        @Nullable
        private volatile Object cachedFieldValue;

        public ConsumerFieldElement(Field field, boolean required)
        {
            super(field, (PropertyDescriptor) null);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable
        {
            Field field = (Field) this.member;
            Object value;
            if (this.cached)
            {
                value = ConsumerAnnotationBeanPostProcessor.this.resolvedCachedArgument(beanName, this.cachedFieldValue);
            }
            else
            {
                DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
                desc.setContainingClass(bean.getClass());
                Set<String> aresConsumerBeanNames = new LinkedHashSet<>(1);
                Assert.state(ConsumerAnnotationBeanPostProcessor.this.beanFactory != null, "No BeanFactory available");
                TypeConverter typeConverter = ConsumerAnnotationBeanPostProcessor.this.beanFactory.getTypeConverter();

                try
                {
                    value = ConsumerAnnotationBeanPostProcessor.this.beanFactory.resolveDependency(desc, beanName, aresConsumerBeanNames, typeConverter);
                } catch (BeansException var12)
                {
                    throw new UnsatisfiedDependencyException((String) null, beanName, new InjectionPoint(field), var12);
                }

                synchronized (this)
                {
                    if (!this.cached)
                    {
                        Class<?> fieldType = field.getType();
                        String interfaceName = ClassUtils.getShortName(fieldType.getName());
                        String shortName = ClassUtils.getShortName(interfaceName);
                        AnnotationAttributes attributes = ConsumerAnnotationBeanPostProcessor.AnnotationUtils.getMergedAttributes(field, AresConsumer.class, environment, true, new String[0]);
                        ConsumerBeanFactory consumerBeanFactory = new ConsumerBeanFactory();
                        consumerBeanFactory.setCenter(consumerParams.get("center"));
                        consumerBeanFactory.setGroup(consumerParams.get("group"));
                        consumerBeanFactory.setVersion(consumerParams.get("version"));
                        consumerBeanFactory.setInterfaceClass(fieldType);
                        consumerBeanFactory.setInterfaceName(interfaceName);

                        String consumerBeanName = getConsumerBeanName(attributes, fieldType);
                        if (beanFactory.containsBean(shortName))
                        {
                            AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanFactory.getBeanDefinition(shortName);
                            RuntimeBeanReference runtimeBeanReference = (RuntimeBeanReference) beanDefinition.getPropertyValues().get("ref");
                            String serviceBeanName = runtimeBeanReference.getBeanName();
                            beanFactory.registerAlias(serviceBeanName, beanName);
                        }
                        else if (!beanFactory.containsBean(shortName))
                        {
                            beanFactory.registerSingleton(consumerBeanName, consumerBeanFactory);
                        }

                        InvocationHandler handler = new ConsumerInvokeHandler(consumerBeanName);

                        value = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{fieldType}, handler);


                        if (value == null && !this.required)
                        {
                            this.cachedFieldValue = null;
                        }
                        else
                        {
                            this.cachedFieldValue = desc;
                            ConsumerAnnotationBeanPostProcessor.this.registerDependentBeans(beanName, aresConsumerBeanNames);
                            if (aresConsumerBeanNames.size() == 1)
                            {
                                String aresConsumerBeanName = (String) aresConsumerBeanNames.iterator().next();
                                if (ConsumerAnnotationBeanPostProcessor.this.beanFactory.containsBean(aresConsumerBeanName) && ConsumerAnnotationBeanPostProcessor.this.beanFactory.isTypeMatch(aresConsumerBeanName, field.getType()))
                                {
                                    this.cachedFieldValue = new ConsumerAnnotationBeanPostProcessor.ShortcutDependencyDescriptor(desc, aresConsumerBeanName, field.getType());
                                }
                            }
                        }

                        this.cached = true;
                    }
                }
            }

            if (value != null)
            {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, value);
            }

        }
    }


    private String getConsumerBeanName(AnnotationAttributes attributes, Class<?> interfaceClass)
    {
        String beanName = (String) AnnotationUtils.getAttribute(attributes, "id");
        if (!StringUtils.hasText(beanName))
        {
            beanName = this.generateReferenceBeanName(attributes, interfaceClass);
        }

        return beanName;
    }

    private String generateReferenceBeanName(AnnotationAttributes attributes, Class<?> interfaceClass)
    {
        StringBuilder beanNameBuilder = new StringBuilder("@Reference");
        if (!attributes.isEmpty())
        {
            beanNameBuilder.append('(');
            Iterator var4 = attributes.entrySet().iterator();

            while(var4.hasNext())
            {
                Map.Entry<String, Object> entry = (Map.Entry) var4.next();
                beanNameBuilder.append((String) entry.getKey()).append('=').append(entry.getValue()).append(',');
            }

            beanNameBuilder.setCharAt(beanNameBuilder.lastIndexOf(","), ')');
        }

        beanNameBuilder.append(" ").append(interfaceClass.getName());
        return beanNameBuilder.toString();
    }
}
