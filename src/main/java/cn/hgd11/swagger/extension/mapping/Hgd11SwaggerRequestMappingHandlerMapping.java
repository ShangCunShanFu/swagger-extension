package cn.hgd11.swagger.extension.mapping;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriTemplate;
import springfox.documentation.spring.web.PropertySourcedMapping;
import springfox.documentation.swagger2.web.Swagger2Controller;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/9/7 15:47
 * @description: 用户替换Swagger自带的PropertySourcedRequestMappingHandlerMapping<br />
 * 在创建Hgd11SwaggerRequestMappingHandlerMapping bean时，要保证该类比PropertySourcedRequestMappingHandlerMapping实例化优先级高
 **************************************/
public class Hgd11SwaggerRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final Map<String, HandlerMethod> handlerMethods = new LinkedHashMap<String, HandlerMethod>();

    private final Environment environment;

    private final Swagger2Controller handler;

    public Hgd11SwaggerRequestMappingHandlerMapping(Environment environment, Swagger2Controller handler) {
        this.environment = environment;
        this.handler = handler;
    }

    @Override
    protected void initHandlerMethods() {
        logger.debug("initialising the handler methods");
        setOrder(Ordered.HIGHEST_PRECEDENCE + 1000);
        // 该HandlerMapping只服务于Swagger2Controller
        Class<?> clazz = Swagger2Controller.class;
        if (isHandler(clazz)) {
            for (Method method : clazz.getMethods()) {
                PropertySourcedMapping mapper = AnnotationUtils.getAnnotation(method, PropertySourcedMapping.class);
                if (mapper != null) {
                    RequestMappingInfo mapping = getMappingForMethod(method, clazz);
                    HandlerMethod handlerMethod = createHandlerMethod(handler, method);
                    String mappingPath = mappingPath(mapper);
                    if (mappingPath != null) {
                        logger.info(String
                            .format("Mapped URL path [%s] onto method [%s]", mappingPath, handlerMethod.toString()));
                        handlerMethods.put(mappingPath, handlerMethod);
                    } else {
                        for (String path : mapping.getPatternsCondition().getPatterns()) {
                            logger.info(
                                String.format("Mapped URL path [%s] onto method [%s]", path, handlerMethod.toString()));
                            handlerMethods.put(path, handlerMethod);
                        }
                    }
                }
            }
        }
    }

    private String mappingPath(final PropertySourcedMapping mapper) {
        final String key = mapper.propertyKey();
        final String target = mapper.value();
        return Optional.ofNullable(environment.getProperty(key))
            .map(input -> target.replace(String.format("${%s}", key), input)).orElse(null);
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return ((AnnotationUtils.findAnnotation(beanType, Controller.class) != null) || (
            AnnotationUtils.findAnnotation(beanType, RequestMapping.class) != null));
    }

    /**
     * The lookup handler method, maps the SEOMapper method to the request URL.
     * <p>If no mapping is found, or if the URL is disabled, it will simply drop through
     * to the standard 404 handling.</p>
     *
     * @param urlPath the path to match.
     * @param request the http servlet request.
     * @return The HandlerMethod if one was found.
     */
    @Override
    protected HandlerMethod lookupHandlerMethod(String urlPath, HttpServletRequest request) {
        logger.debug("looking up handler for path: " + urlPath);
        HandlerMethod handlerMethod = handlerMethods.get(urlPath);
        if (handlerMethod != null) {
            return handlerMethod;
        }
        for (String path : handlerMethods.keySet()) {
            UriTemplate template = new UriTemplate(path);
            if (template.matches(urlPath)) {
                request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, template.match(urlPath));
                return handlerMethods.get(path);
            }
        }
        return null;
    }
}
