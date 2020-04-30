package com.asiainfo.frame.remote.invoke;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AnnotationUtils
{
    public AnnotationUtils()
    {
    }

    public static String resolveInterfaceName(AnnotationAttributes attributes, Class<?> defaultInterfaceClass)
    {
        return resolveServiceInterfaceClass(attributes, defaultInterfaceClass).getName();
    }

    public static <T> T getAttribute(AnnotationAttributes attributes, String name)
    {
        return (T) attributes.get(name);
    }

    public static Class<?> resolveServiceInterfaceClass(AnnotationAttributes attributes, Class<?> defaultInterfaceClass) throws IllegalArgumentException
    {
        ClassLoader classLoader = defaultInterfaceClass != null ? defaultInterfaceClass.getClassLoader() : Thread.currentThread().getContextClassLoader();
        Class<?> interfaceClass = (Class) getAttribute(attributes, "interfaceClass");
        if (Void.TYPE.equals(interfaceClass))
        {
            interfaceClass = null;
            String interfaceClassName = (String) getAttribute(attributes, "interfaceName");
            if (StringUtils.hasText(interfaceClassName) && ClassUtils.isPresent(interfaceClassName, classLoader))
            {
                interfaceClass = ClassUtils.resolveClassName(interfaceClassName, classLoader);
            }
        }

        if (interfaceClass == null && defaultInterfaceClass != null)
        {
            Class<?>[] allInterfaces = ClassUtils.getAllInterfacesForClass(defaultInterfaceClass);
            if (allInterfaces.length > 0)
            {
                interfaceClass = allInterfaces[0];
            }
        }

        Assert.notNull(interfaceClass, "@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue(interfaceClass.isInterface(), "The annotated type must be an interface!");
        return interfaceClass;
    }


    public static <A extends Annotation> boolean isPresent(Method method, Class<A> annotationClass)
    {
        Map<ElementType, List<A>> annotationsMap = findAnnotations(method, annotationClass);
        return !annotationsMap.isEmpty();
    }

    public static <A extends Annotation> Map<ElementType, List<A>> findAnnotations(Method method, Class<A> annotationClass)
    {
        Retention retention = (Retention) annotationClass.getAnnotation(Retention.class);
        RetentionPolicy retentionPolicy = retention.value();
        if (!RetentionPolicy.RUNTIME.equals(retentionPolicy))
        {
            return Collections.emptyMap();
        }
        else
        {
            Map<ElementType, List<A>> annotationsMap = new LinkedHashMap();
            Target target = (Target) annotationClass.getAnnotation(Target.class);
            ElementType[] elementTypes = target.value();
            ElementType[] var7 = elementTypes;
            int var8 = elementTypes.length;

            for (int var9 = 0; var9 < var8; ++var9)
            {
                ElementType elementType = var7[var9];
                List<A> annotationsList = new LinkedList();
                switch (elementType)
                {
                    case PARAMETER:
                        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                        Annotation[][] var21 = parameterAnnotations;
                        int var22 = parameterAnnotations.length;

                        for (int var23 = 0; var23 < var22; ++var23)
                        {
                            Annotation[] annotations = var21[var23];
                            Annotation[] var17 = annotations;
                            int var18 = annotations.length;

                            for (int var19 = 0; var19 < var18; ++var19)
                            {
                                Annotation annotation = var17[var19];
                                if (annotationClass.equals(annotation.annotationType()))
                                {
                                    annotationsList.add((A) annotation);
                                }
                            }
                        }
                        break;
                    case METHOD:
                        A annotation = org.springframework.core.annotation.AnnotationUtils.findAnnotation(method, annotationClass);
                        if (annotation != null)
                        {
                            annotationsList.add(annotation);
                        }
                        break;
                    case TYPE:
                        Class<?> beanType = method.getDeclaringClass();
                        A annotation2 = org.springframework.core.annotation.AnnotationUtils.findAnnotation(beanType, annotationClass);
                        if (annotation2 != null)
                        {
                            annotationsList.add(annotation2);
                        }
                }

                if (!annotationsList.isEmpty())
                {
                    annotationsMap.put(elementType, annotationsList);
                }
            }

            return Collections.unmodifiableMap(annotationsMap);
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
                Entry entry;
                String attributeName;
                do
                {
                    if (!var4.hasNext())
                    {
                        return Collections.unmodifiableMap(resolvedAnnotationAttributes);
                    }

                    entry = (Entry) var4.next();
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

                            Entry<String, Object> entry = (Entry) var6.next();
                            attributeName = (String) entry.getKey();
                            attributeValue = entry.getValue();
                        } while(ignoreDefaultValue && ObjectUtils.nullSafeEquals(attributeValue, org.springframework.core.annotation.AnnotationUtils.getDefaultValue(annotation, attributeName)));
                    } while(attributeValue.getClass().isAnnotation());
                } while(attributeValue.getClass().isArray() && attributeValue.getClass().getComponentType().isAnnotation());

                actualAttributes.put(attributeName, attributeValue);
            }
        }
    }


    public static AnnotationAttributes getMergedAttributes(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType, PropertyResolver propertyResolver, boolean ignoreDefaultValue, String... ignoreAttributeNames)
    {
        Annotation annotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, annotationType);
        return annotation == null ? null : AnnotationAttributes.fromMap(getAttributes(annotation, propertyResolver, ignoreDefaultValue, ignoreAttributeNames));
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
