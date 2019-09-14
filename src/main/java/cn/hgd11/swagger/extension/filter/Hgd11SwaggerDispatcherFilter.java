package cn.hgd11.swagger.extension.filter;

import cn.hgd11.swagger.extension.mapping.Hgd11SwaggerRequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/9/8 17:26
 * @modified By：
 */
//@WebFilter(urlPatterns = {"/*"}, filterName = "hgd11SwaggerDispatcherFilter")
//@Slf4j
public class Hgd11SwaggerDispatcherFilter implements Filter {

    @Autowired
    private Environment environment;

    @Autowired
    private DispatcherServlet dispatcherServlet;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        String property = environment.getProperty("springfox.documentation.swagger.v2.path");

        String toMatch;
        if (property != null) {
            toMatch = property;
        } else {
            toMatch = "/v2/api-docs";
        }

        List<HandlerMapping> newHandlerMappings = new ArrayList<>(16);
        HandlerMapping targetHandlerMapping = null;
        if (toMatch.equals(requestURI)) {
            List<HandlerMapping> handlerMappings = dispatcherServlet.getHandlerMappings();
            if (handlerMappings != null) {
                for (HandlerMapping handlerMapping : handlerMappings) {
                    if (handlerMapping instanceof Hgd11SwaggerRequestMappingHandlerMapping) {
                        newHandlerMappings.add(handlerMapping);
                    }
                }
                for (HandlerMapping newHandlerMapping : handlerMappings) {
                    if (newHandlerMapping instanceof Hgd11SwaggerRequestMappingHandlerMapping) {
                        continue;
                    }
                    newHandlerMappings.add(newHandlerMapping);
                }
            }
        }

        filterChain.doFilter(request, servletResponse);
    }
}
