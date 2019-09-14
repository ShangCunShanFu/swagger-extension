package cn.hgd11.swagger.extension.config;

import cn.hgd11.swagger.extension.entity.MethodEntity;
import cn.hgd11.swagger.extension.util.PackageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/8/14 14:39
 * @description:
 **************************************/
public class Hgd11SwaggerExtConfig {

    public Map<MethodEntity, Method> getPathMethodMap() {
        return pathMethodMap;
    }

    public void setPathMethodMap(Map<MethodEntity, Method> pathMethodMap) {
        this.pathMethodMap = pathMethodMap;
    }

    /**
     * 封装controller中路径与方法的映射
     */
    private Map<MethodEntity, Method> pathMethodMap;


    /**
     * 在该方法中，使用都须调用cn.hgd11.swagger.extension.config.Hgd11SwaggerExtConfig#initPathMethodMapAssist(java.lang.String)<br/>
     * 并指定项目中controller层的根目录,如：com.navinfo.rainbow.devops.controller
     */

    public void initPathMethodMapAssist(String controllerBasePackage) {
        try {
            if(this.pathMethodMap == null){
                setPathMethodMap(new HashMap<>(256));
            }

            Map<MethodEntity, Method> pathMethodMap = getPathMethodMap();

            // 加载配置信息，读取到controller的根路径,暂时没有考虑多个根路径
            List<String> controllerNameList = null;
            if (controllerBasePackage != null) {
                controllerNameList = PackageUtils.getClassName(controllerBasePackage);
            }
            // 如果没有配置根路径，将无法断续下一步操作，直接结果
            if (controllerNameList == null) {
                return;
            }

            // 遍历每一个Controller，添加路径与方法映射
            for (String controllerName : controllerNameList) {
                Class<?> clazz = null;
                clazz = Class.forName(controllerName);
                if (clazz == null) {
                    continue;
                }

                RestController restController = clazz.getAnnotation(RestController.class);
                Controller controller = clazz.getAnnotation(Controller.class);
                if (restController == null && controller == null) {
                    // 如果RestController与Controller都没有配置，则不是Controller层类
                    continue;
                }

                // 类上的RequestMapping是路径前缀
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                List<String> requestMappingValue = null;
                if (requestMapping != null) {
                    String[] prePaths = requestMapping.value();
                    requestMappingValue = Arrays.asList(prePaths);
                }
                if (requestMapping == null) {
                    // 如果没有在类上配置RequestMapping，则没有前缀
                    requestMappingValue = new ArrayList<>();
                    requestMappingValue.add("");
                }

                Method[] declaredMethods = clazz.getDeclaredMethods();

                for (String preffixPath : requestMappingValue) {
                    preffixPath = checkUri(preffixPath);
                    for (Method declaredMethod : declaredMethods) {
                        String uri = preffixPath;
                        // GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
                        Map<String, String[]> requestMapping1 = getRequestMapping(declaredMethod);
                        String[] requestMappingValues = requestMapping1.get("values");
                        String[] methods = requestMapping1.get("method");
                        if (requestMappingValues != null) {
                            // 当requestMappingValues为NULL时，认为该方法没有对外暴露接口
                            if (requestMappingValues.length > 0) {
                                /*
                                 * 如果长度为0，说明没有在方法上配置路径
                                 * 当在路径上没有进行配置时，说该接口地址全路径即为前缀所表示的路径
                                 */
                                for (String methodRequestMappingValue : requestMappingValues) {
                                    methodRequestMappingValue = checkUri(methodRequestMappingValue);
                                    for (String method : methods) {
                                        MethodEntity methodEntity=new MethodEntity();
                                        uri += methodRequestMappingValue;
                                        methodEntity.setUri(uri);
                                        methodEntity.setRequestMethod(method);
                                        pathMethodMap.put(methodEntity, declaredMethod);
                                    }
                                }
                            } else {
                                for (String method : methods) {
                                    MethodEntity methodEntity=new MethodEntity();
                                    methodEntity.setUri(uri);
                                    methodEntity.setRequestMethod(method);
                                    pathMethodMap.put(methodEntity, declaredMethod);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果uri不是以'/'开始，则添加'/'
     *
     * @param preffixPath
     * @return
     */
    private String checkUri(String preffixPath) {
        if (!preffixPath.isEmpty()) {
            if ('/' != preffixPath.charAt(0)) {
                preffixPath = "/" + preffixPath;
            }
        }
        return preffixPath;
    }

    private Map<String, String[]> getRequestMapping(Method declaredMethod) {
        Map<String, String[]> result = new HashMap<>(4);
        RequestMapping methodRequestMapping = declaredMethod.getAnnotation(RequestMapping.class);
        if (methodRequestMapping != null) {
            result.put("method", requestMappingAnnoMethod(methodRequestMapping));
            result.put("values", methodRequestMapping.value());
            return result;
        }
        GetMapping getMapping = declaredMethod.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            result.put("method", new String[]{"get"});
            result.put("values", getMapping.value());
            return result;
        }
        PostMapping postMapping = declaredMethod.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            result.put("method", new String[]{"post"});
            result.put("values", postMapping.value());
            return result;
        }
        PutMapping putMapping = declaredMethod.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            result.put("method", new String[]{"put"});
            result.put("values", putMapping.value());
            return result;
        }
        DeleteMapping deleteMapping = declaredMethod.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            result.put("method", new String[]{"delete"});
            result.put("values", deleteMapping.value());
            return result;
        }
        PatchMapping patchMapping = declaredMethod.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            result.put("method", new String[]{"patch"});
            result.put("values", patchMapping.value());
            return result;
        }
        return new HashMap<>();
    }

    private String[] requestMappingAnnoMethod(RequestMapping methodRequestMapping) {
        RequestMethod[] method = methodRequestMapping.method();

        if(method.length==0){
            return new String[]{"get","post","head","patch","put","delete","options","trace"};
        }

        String[] result=new String[method.length];
        int count=0;
        for (RequestMethod requestMethod : method) {
            switch (requestMethod){
                case GET:
                    result[count]="get";
                    break;
                case HEAD:
                    result[count]="head";
                    break;
                case POST:
                    result[count]="post";
                    break;
                case PATCH:
                    result[count]="patch";
                    break;
                case PUT:
                    result[count]="put";
                    break;
                case DELETE:
                    result[count]="delete";
                    break;
                case OPTIONS:
                    result[count]="options";
                    break;
                case TRACE:
                    result[count]="trace";
                    break;
                default:
            }
            count++;
        }
        return result;
    }
}
