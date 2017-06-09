package com.anxin.replile.web.configs;

import com.anxin.replile.web.interceps.AppInterceptorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * JSP文件必须放在/src/main/webapp/目录下
 * 因为/src/main/resources/下的任何一个位置在打包war时都会被编译到/WEB-INF/classes/下面，这个目录下的JSP文件是不会被JavaEE容器识别的
 * Created by hui.ran on 2017/3/14.
 */
@Configuration
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    protected AppInterceptorAdapter appInterceptor;

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(){
        return container -> {
        container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/templates/views/common/404.htm"));
        container.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/templates/views/common/500.htm"));
        container.addErrorPages(new ErrorPage(Throwable.class, "/templates/views/common/500.htm"));
        };
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/login");
        registry.addViewController("/home").setViewName("/loginAmin");
        registry.addViewController("/login").setViewName("/loginAmin");
        registry.addViewController("/index").setViewName("/index");
        registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
        super.addViewControllers(registry);
    }
    /**
     * 资源处理
     * @param registry
     * */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/classpath:/templates/views/");
        irvr.setSuffix(".htm");
        irvr.setViewClass(JstlView.class);
        registry.viewResolver(irvr);
        super.configureViewResolvers(registry);
    }
    /**
      * 拦截器配置
      * @param registry
     * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        registry.addInterceptor(appInterceptor).addPathPatterns("/**").excludePathPatterns("classpath:/static/");
        super.addInterceptors(registry);
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*/*")
                .allowedMethods("*")
                .maxAge(120);
     }

    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        return registration;
    }
}
