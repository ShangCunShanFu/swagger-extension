package cn.hgd11.swagger.extension.annotation;

import cn.hgd11.swagger.extension.config.Hgd11Swagger2DocumentationConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Documented
//@EnableSwagger2
@Import({ Hgd11Swagger2DocumentationConfiguration.class})
public @interface EnableHgd11Swagger {
}
