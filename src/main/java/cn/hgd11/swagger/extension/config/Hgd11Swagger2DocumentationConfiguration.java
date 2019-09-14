package cn.hgd11.swagger.extension.config;

import cn.hgd11.swagger.extension.aop.Hgd11SwaggerAspect;
import cn.hgd11.swagger.extension.converter.Hgd11SwaggerConverter;
import cn.hgd11.swagger.extension.mapping.Hgd11SwaggerRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerMapping;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.PropertySourcedRequestMappingHandlerMapping;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger.configuration.SwaggerCommonConfiguration;
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;
import springfox.documentation.swagger2.web.Swagger2Controller;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/9/9 22:05
 * @modified By：
 */
@Configuration
@Import({ SpringfoxWebMvcConfiguration.class, SwaggerCommonConfiguration.class })
@ComponentScan(basePackages = {
    "springfox.documentation.swagger2.mappers"
})
@ConditionalOnWebApplication
public class Hgd11Swagger2DocumentationConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public JacksonModuleRegistrar swagger2Module() {
        return new Swagger2JacksonModule();
    }

    @Bean
    public HandlerMapping swagger2ControllerMapping(
        @Qualifier("swagger2Controller") Swagger2Controller swagger2Controller) {
        return new PropertySourcedRequestMappingHandlerMapping(environment, swagger2Controller);
    }

    @Bean
    public Swagger2Controller swagger2Controller(DocumentationCache documentationCache,
        ServiceModelToSwagger2Mapper mapper, JsonSerializer jsonSerializer) {
        return new Swagger2Controller(environment, documentationCache, mapper, jsonSerializer);
    }

    @Bean
    public Hgd11SwaggerExtConfig hgd11SwaggerExtConfig() {
        String baseControllerPackage = environment.getProperty("hgd11-swagger.baseControllerPackage");
        Assert.notNull(baseControllerPackage,
            "请配置controller层根目录，如果您使用properties配置文件，请添加\nhgd11-swagger.baseControllerPackage\n如果您使用yml配置文件，请添加\nhgd11-swagger:\n\tbaseControllerPackage:");

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
    public Hgd11SwaggerRequestMappingHandlerMapping hgd11SwaggerRequestMappingHandlerMapping(Environment environment,
        @Qualifier("swagger2Controller") Swagger2Controller swagger2Controller) {
        return new Hgd11SwaggerRequestMappingHandlerMapping(environment, swagger2Controller);
    }

    @Bean
    public Hgd11SwaggerAspect hgd11SwaggerAspect() {
        return new Hgd11SwaggerAspect();
    }

//    @Bean
//    private Hgd11SwaggerRunner hgd11SwaggerRunner(DispatcherServlet dispatcherServlet) {
//        return new Hgd11SwaggerRunner(dispatcherServlet);
//    }
}
