package cn.hgd11.swagger.extension.config;

import cn.hgd11.swagger.extension.aop.Hgd11SwaggerAspect;
import cn.hgd11.swagger.extension.converter.Hgd11SwaggerConverter;
import cn.hgd11.swagger.extension.mapping.Hgd11SwaggerRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;
import springfox.documentation.swagger2.web.Swagger2Controller;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/9/8 16:00
 * @modified By：
 */
public class Hgd11SwaggerBeanConfig {

    @Value("${hgd11-swagger.baseControllerPackage}")
    private String baseControllerPackage;

    @Bean
    public Swagger2Controller swagger2Controller(Environment environment, DocumentationCache documentationCache,
        ServiceModelToSwagger2Mapper mapper, JsonSerializer jsonSerializer) {
        return new Swagger2Controller(environment, documentationCache, mapper, jsonSerializer);
    }

    @Bean
    public Hgd11SwaggerExtConfig hgd11SwaggerExtConfig() {
        Hgd11SwaggerExtConfig hgd11SwaggerExtConfig = new Hgd11SwaggerExtConfig();
        hgd11SwaggerExtConfig.initPathMethodMapAssist(baseControllerPackage);
        return hgd11SwaggerExtConfig;
    }

    @Bean
    public Hgd11SwaggerConverter hgd11SwaggerConverter(Environment environment,
        @Qualifier("hgd11SwaggerExtConfig") Hgd11SwaggerExtConfig swaggerExtConfig) {
        return new Hgd11SwaggerConverter(environment, swaggerExtConfig);
    }

    @Bean
    @Order(Integer.MAX_VALUE - 2)
    public Hgd11SwaggerRequestMappingHandlerMapping hgd11SwaggerRequestMappingHandlerMapping(Environment environment,
        @Qualifier("swagger2Controller") Swagger2Controller swagger2Controller) {
        return new Hgd11SwaggerRequestMappingHandlerMapping(environment, swagger2Controller);
    }

    @Bean
    public Hgd11SwaggerAspect hgd11SwaggerAspect() {
        return new Hgd11SwaggerAspect();
    }
}
