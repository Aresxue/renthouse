package com.asiainfo.frame.remote.invoke;


import com.asiainfo.frame.base.ResponseBase;
import com.asiainfo.frame.exceptions.RemoteInvokeException;
import com.asiainfo.frame.utils.ClassTypeUtil;
import com.asiainfo.frame.utils.DateUtil;
import com.asiainfo.frame.utils.SpringUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.asiainfo.frame.base.ResponseEnum.INVOKE_FAILURE;
import static com.asiainfo.frame.base.ResponseEnum.INVOKE_FAILURE_DATE_ERROR;
import static com.asiainfo.frame.base.ResponseEnum.INVOKE_FAILURE_JSON_PARSE;
import static com.asiainfo.frame.base.ResponseEnum.INVOKE_FAILURE_MORE_THAN_ONE;
import static com.asiainfo.frame.base.ResponseEnum.INVOKE_FAILURE_NOT_FOUND_SERVICE;
import static com.asiainfo.frame.base.ResponseEnum.UNKNOWN_ERROR;
import static com.asiainfo.frame.base.ResponseEnum.UNKNOWN_EXCEPTION;

/**
 * @author: Ares
 * @date: 2019/5/31 16:40
 * @description: 公共请求入口
 * @version: JDK 1.8
 */
@RestController
@RequestMapping(value = "/common")
public class CommonController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class);

    private static final ObjectMapper OBJECT_MAPPER;
    /**
     * public方法的Lookup
     */
    private static final MethodHandles.Lookup PUBLIC_LOOKUP;

    static
    {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        PUBLIC_LOOKUP = MethodHandles.publicLookup();
    }

    /**
     * 远程代理方法Map
     */
    private static final MultiValueMap<String, RemoteProxyService> REMOTE_PROXY_SERVICE = new LinkedMultiValueMap<>();

    /**
     * 统一服务调用Map
     */
    private static final Map<String, Method> UNIFIED_SERVICE_MAP = new HashMap<>();

    /**
     * @author: Ares
     * @description: 添加代理方法
     * @date: 2019/6/13 10:59
     * @Param: [uniqueKey, service] 请求参数
     * @return: void 响应参数
     */
    public static void addProxyMethod(String uniqueKey, RemoteProxyService proxyService)
    {
        REMOTE_PROXY_SERVICE.add(uniqueKey, proxyService);
    }

    /**
     * @author: Ares
     * @description: 添加统一调用服务
     * @date: 2019/6/17 15:47
     * @Param: [service, uniqueKey] 请求参数
     * @return: void 响应参数
     */
    public static void addServiceId(String service, Method method)
    {
        UNIFIED_SERVICE_MAP.put(service, method);
    }

    /**
     * @author: Ares
     * @description: 统一调用地址
     * @date: 2019/6/10 20:21
     * @Param: [request] 请求参数
     * @return: java.lang.Object 响应参数
     */
    @RequestMapping(value = "/invoke")
    public Object invoke(HttpServletRequest request, @RequestBody String requestObject)
    {
        try
        {
            String serviceId = request.getParameter("serviceId");
            Method method = UNIFIED_SERVICE_MAP.get(serviceId);
            Object bean = SpringUtil.getBean(serviceId);
            Object requestParam = OBJECT_MAPPER.readValue(requestObject, method.getParameterTypes()[0]);
            return method.invoke(bean, requestParam);
        } catch (Exception e)
        {
            LOGGER.error("{}: ", UNKNOWN_ERROR.getResponseDesc(), e);
            ResponseBase response = new ResponseBase();
            response.setResponseEnum(UNKNOWN_ERROR);
            return response;
        }
    }

    /**
     * @author: Ares
     * @description: 内部实例互相调用
     * @date: 2019/6/10 20:22
     * @Param: [request] 请求参数
     * @return: java.lang.Object 响应参数
     **/
    @RequestMapping(value = "/innerInvoke")
    public Object innerInvoke(@RequestParam(required = false) MultiValueMap<String, Object> parameters)
    {
        ResponseBase response = new ResponseBase();
        List<RemoteProxyService> proxyServices = REMOTE_PROXY_SERVICE.get(Objects.requireNonNull(parameters.getFirst("uniqueKey")).toString());
        if (null == proxyServices)
        {
            LOGGER.error(INVOKE_FAILURE_NOT_FOUND_SERVICE.getResponseDesc());
            response.setResponseEnum(INVOKE_FAILURE_NOT_FOUND_SERVICE);
            return response;
        }
        if (proxyServices.size() > 1)
        {
            LOGGER.error(INVOKE_FAILURE_MORE_THAN_ONE.getResponseDesc());
            response.setResponseEnum(INVOKE_FAILURE_MORE_THAN_ONE);
            return response;
        }
        RemoteProxyService service = proxyServices.get(0);
        if (null == service || null == service.getProxyService() || null == service.getProxyMethod())
        {
            LOGGER.error(INVOKE_FAILURE_NOT_FOUND_SERVICE.getResponseDesc());
            response.setResponseEnum(INVOKE_FAILURE_NOT_FOUND_SERVICE);
            return response;
        }
        Method method = service.getProxyMethod();
        // 可以省略,为了预防权限问题这里设置一下
        method.setAccessible(true);
        try
        {
            Class<?>[] paramTypes = method.getParameterTypes();
            Type[] paramTypeList = service.getProxyMethod().getGenericParameterTypes();

            Object[] params = new Object[paramTypes.length];
            // 参数处理
            for (int i = 0; i < paramTypes.length; i++)
            {
                Object value = parameters.getFirst("arg" + i);
                params[i] = paramHandle(paramTypes[i], value, paramTypeList[i]);
            }

            return method.invoke(service.getProxyService(), params);
        } catch (Exception e)
        {
            LOGGER.error("{}: ", INVOKE_FAILURE.getResponseDesc(), e);
            response.setResponseEnum(INVOKE_FAILURE);
        }
        return response;
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
                if (requestType.isPrimitive())
                {
                    switch (requestType.getName())
                    {
                        case "int":
                            return Integer.valueOf(value.toString());
                        case "double":
                            return Double.valueOf(value.toString());
                        case "float":
                            return Float.valueOf(value.toString());
                        case "long":
                            return Long.valueOf(value.toString());
                        case "char":
                            return value.toString().charAt(1);
                        case "byte":
                            return Byte.valueOf(value.toString());
                        case "short":
                            return Short.valueOf(value.toString());
                        case "boolean":
                            return Boolean.valueOf(value.toString());
                        default:
                            break;
                    }
                }
                // 基础类型包装类
                else if (ClassTypeUtil.isBaseWrap(requestType))
                {
                    // 对Character做特殊处理
                    if (Character.class.getName().equals(requestType.getName()))
                    {
                        return value.toString().charAt(1);
                    }
                    else
                    {
                        // 使用方法句柄进行转换
                        MethodType methodType = MethodType.methodType(requestType, String.class);
                        MethodHandle valueOfHandle = PUBLIC_LOOKUP.findStatic(requestType, "valueOf", methodType);
                        return valueOfHandle.invoke(value.toString());
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
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e)
        {
            LOGGER.error("{}: ", INVOKE_FAILURE.getResponseDesc(), e);
            throw new RemoteInvokeException(INVOKE_FAILURE);
        } catch (ParseException e)
        {
            LOGGER.error("{}: ", INVOKE_FAILURE_DATE_ERROR.getResponseDesc(), e);
            throw new RemoteInvokeException(INVOKE_FAILURE_DATE_ERROR);
        } catch (JsonProcessingException e)
        {
            LOGGER.error("{}: ", INVOKE_FAILURE_JSON_PARSE.getResponseDesc(), e);
            throw new RemoteInvokeException(INVOKE_FAILURE_JSON_PARSE);
        } catch (Throwable e)
        {
            LOGGER.error("{}: ", UNKNOWN_EXCEPTION.getResponseDesc(), e);
            throw new RemoteInvokeException(UNKNOWN_EXCEPTION);
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
