package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.exceptions.RemoteInvokeException;
import com.asiainfo.frame.utils.DateUtil;
import com.asiainfo.frame.utils.SpringUtil;
import com.asiainfo.frame.vo.ResponseBase;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_DATE_ERROR;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_JSON_PARSE;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_MORE_THAN_ONE;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_NOT_FOUND_CLASS;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_NOT_FOUND_LOCAL_METHOD;
import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_FAILURE_NOT_FOUND_SERVICE;
import static com.asiainfo.frame.constants.ResponseEnum.UNKNOWN_EXCEPTION;
import static com.asiainfo.frame.utils.ClassTypeUtil.isBaseWrap;

/**
 * @author: Ares
 * @date: 2020/4/28 15:31
 * @description: 请求入口
 * @version: JDK 1.8
 */
@RestController
@RequestMapping(value = "/provider")
public class ProviderController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    /***
     * 提供者的实例
     */
    private static final MultiValueMap<String, Object> SERVICE_BEANS;

    /**
     * json处理器
     */
    private static final ObjectMapper OBJECT_MAPPER;

    static
    {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        SERVICE_BEANS = new LinkedMultiValueMap<>();
    }


    /**
     * @author: Ares
     * @description: 添加服务实例
     * @date: 2020/4/28 15:36
     * @param: [uniqueId, serviceBean]
     * 唯一标识, 服务实例
     * @return: void 响应参数
     */
    public static void addServiceBean(String uniqueId, Object serviceBean)
    {
        SERVICE_BEANS.add(uniqueId, serviceBean);
    }

    @RequestMapping(value = "/innerInvoke")
    public Object innerInvoke(@RequestParam(required = false) MultiValueMap<String, Object> parameters)
    {
        ResponseBase response = new ResponseBase();


        String uniqueKey = Objects.requireNonNull(parameters.getFirst("uniqueKey")).toString();
        String[] strings = uniqueKey.split("#");
        String interfaceName = strings[0];
        String methodName = strings[1];
        String group = strings[2];
        String version = strings[3];
        String[] paramTypes = strings[4].split(",");

        String beanUnique = generateBeanUnique(interfaceName, group, version);
        List<Object> serviceBeans = SERVICE_BEANS.get(beanUnique);
        if (CollectionUtils.isEmpty(serviceBeans))
        {
            LOGGER.error(INVOKE_FAILURE_NOT_FOUND_SERVICE.getLoggerDesc(), uniqueKey);
            response.setResponseEnum(INVOKE_FAILURE_NOT_FOUND_SERVICE);
            return response;
        }
        if (serviceBeans.size() > 1)
        {
            LOGGER.error(INVOKE_FAILURE_MORE_THAN_ONE.getLoggerDesc(), uniqueKey);
            response.setResponseEnum(INVOKE_FAILURE_MORE_THAN_ONE);
            return response;
        }

        Object serviceBean = serviceBeans.get(0);
        if (null == serviceBean)
        {
            serviceBean = SpringUtil.getBean(beanUnique);
            serviceBeans.set(0, serviceBean);
        }

        try
        {
            Class<?> interfaceClass = Class.forName(interfaceName);
            Class<?>[] parameterTypes = new Class<?>[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++)
            {
                parameterTypes[i] = Class.forName(paramTypes[i]);
            }

            Method method = interfaceClass.getDeclaredMethod(methodName, parameterTypes);
            Type[] paramTypeList = method.getGenericParameterTypes();
            // 参数处理
            Object[] params = new Object[paramTypeList.length];
            for (int i = 0; i < parameterTypes.length; i++)
            {
                params[i] = paramHandle(parameterTypes[i], parameters.getFirst("arg" + i), paramTypeList[i]);
            }

            return method.invoke(serviceBean, params);
        } catch (ClassNotFoundException e)
        {
            LOGGER.error(INVOKE_FAILURE_NOT_FOUND_CLASS.getLoggerDesc(), strings[0], e);
            response.setResponseEnum(INVOKE_FAILURE_NOT_FOUND_CLASS);
        } catch (NoSuchMethodException e)
        {
            LOGGER.error(INVOKE_FAILURE_NOT_FOUND_LOCAL_METHOD.getLoggerDesc(), strings[1], e);
            response.setResponseEnum(INVOKE_FAILURE_NOT_FOUND_LOCAL_METHOD);
        } catch (RemoteInvokeException ex)
        {
            response.setRemoteInvokeException(ex);
        } catch (Exception e)
        {
            LOGGER.error(UNKNOWN_EXCEPTION.getLoggerDesc(), e);
            response.setResponseEnum(UNKNOWN_EXCEPTION);
        }

        return response;
    }


    /**
     * @author: Ares
     * @description: 生成实例唯一标识
     * @date: 2020/4/30 19:22
     * @param: [interfaceName, group, version]
     * @return: java.lang.String 响应参数
     */
    public static String generateBeanUnique(String interfaceName, String group, String version)
    {
        StringJoiner stringJoiner = new StringJoiner(":");
        stringJoiner.add(interfaceName);
        stringJoiner.add(group);
        stringJoiner.add(version);
        return stringJoiner.toString();
    }

    /**
     * @author: Ares
     * @description: 参数处理
     * @date: 2019/6/15 16:59
     * @Param: [requestType, value, paramType]
     * 请求Class, 请求值, 参数类型
     * @return: java.lang.Object 响应参数
     */
    private Object paramHandle(Class<?> requestType, Object value, Type paramType) throws RemoteInvokeException
    {
        try
        {
            if (value != null)
            {
                // 如果是基础类型,基本类型不会为null,否则编译器会报错
                if (isBaseWrap(requestType))
                {
                    switch (requestType.getName())
                    {
                        case "int":
                        case "java.lang.Integer":
                            return Integer.valueOf(value.toString());
                        case "double":
                        case "java.lang.Double":
                            return Double.valueOf(value.toString());
                        case "float":
                        case "java.lang.Float":
                            return Float.valueOf(value.toString());
                        case "long":
                        case "java.lang.Long":
                            return Long.valueOf(value.toString());
                        case "char":
                        case "java.lang.Character":
                            return value.toString().charAt(1);
                        case "byte":
                        case "java.lang.Byte":
                            return Byte.valueOf(value.toString());
                        case "short":
                        case "java.lang.Short":
                            return Short.valueOf(value.toString());
                        case "boolean":
                        case "java.lang.Boolean":
                            return Boolean.valueOf(value.toString());
                        default:
                            break;
                    }
                }
                // 字符串
                else if (String.class.isAssignableFrom(requestType))
                {
                    String tempValue = String.valueOf(value);
                    // 泛型擦除中Date类型会丢失,这里采用一种比较简单粗暴的方法处理
                    if (tempValue.contains(DateUtil.T_VALUE) && tempValue.contains(DateUtil.TIME_SUFFIX))
                    {
                        try
                        {
                            return DateUtil.parse(tempValue);
                        } catch (Exception e)
                        {
                            LOGGER.warn("{}: {}", INVOKE_FAILURE_DATE_ERROR.getResponseDesc(), tempValue);
                        }
                    }
                    return tempValue;
                }
                else if (Date.class.isAssignableFrom(requestType))
                {
                    return DateUtil.parse(value.toString().replaceAll("\"", ""));
                }
                else if (LocalDateTime.class.isAssignableFrom(requestType))
                {
                    return LocalDateTime.parse(value.toString().replaceAll("\"", ""));
                }
                // Map
                else if (Map.class.isAssignableFrom(requestType))
                {
                    return OBJECT_MAPPER.readValue(value.toString(), buildJavaType(requestType, paramType));
                }
                // 集合
                else if (Collection.class.isAssignableFrom(requestType))
                {
                    // 对待集合类接口, 默认使用List模式
                    if (Collection.class.getName().equals(requestType.getName()))
                    {
                        requestType = List.class;
                    }
                    return OBJECT_MAPPER.readValue(value.toString(), buildJavaType(requestType, paramType));
                }
                else
                {
                    return OBJECT_MAPPER.readValue(value.toString(), requestType);
                }
            }
        } catch (ClassNotFoundException e)
        {
            LOGGER.error(INVOKE_FAILURE.getResponseDesc(), e);
            throw new RemoteInvokeException(INVOKE_FAILURE);
        } catch (ParseException e)
        {
            LOGGER.error(INVOKE_FAILURE_DATE_ERROR.getLoggerDesc(), value, e);
            throw new RemoteInvokeException(INVOKE_FAILURE_DATE_ERROR);
        } catch (JsonProcessingException e)
        {
            LOGGER.error(INVOKE_FAILURE_JSON_PARSE.getLoggerDesc(), value, e);
            throw new RemoteInvokeException(INVOKE_FAILURE_JSON_PARSE);
        }

        return null;
    }

    /**
     * @author: Ares
     * @description: 构建泛型
     * @date: 2020/4/27 10:34
     * @param: [requestType, paramType]
     * 请求类型, 参数类型
     * @return: com.fasterxml.jackson.databind.JavaType 响应参数
     */
    private JavaType buildJavaType(Class<?> requestType, Type paramType) throws ClassNotFoundException
    {
        if (!(paramType instanceof ParameterizedType))
        {
            Class<?> clz = Class.forName(paramType.getTypeName());
            return OBJECT_MAPPER.getTypeFactory().constructParametricType(requestType, clz);
        }

        ParameterizedType parameterizedType = (ParameterizedType) paramType;
        Type[] types = parameterizedType.getActualTypeArguments();
        JavaType[] javaTypes = new JavaType[types.length];

        for (int i = 0; i < types.length; i++)
        {
            JavaType javaType;
            if (types[i] instanceof ParameterizedType)
            {
                ParameterizedType kType = (ParameterizedType) types[i];
                Class<?> vRawClass = Class.forName(kType.getRawType().getTypeName());
                javaType = buildJavaType(vRawClass, kType);
            }
            else
            {
                Class<?> kClass = Class.forName(types[i].getTypeName());
                javaType = OBJECT_MAPPER.getTypeFactory().constructType(kClass);
            }
            javaTypes[i] = javaType;
        }

        return OBJECT_MAPPER.getTypeFactory().constructParametricType(requestType, javaTypes);
    }

}
