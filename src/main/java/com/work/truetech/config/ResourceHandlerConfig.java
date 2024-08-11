package com.work.truetech.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceHandlerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/phones/**")
                .addResourceLocations("classpath:/fils/phones/");
        registry.addResourceHandler("/models/**")
                .addResourceLocations("classpath:/fils/models/");
        registry.addResourceHandler("/options/**")
                .addResourceLocations("classpath:/fils/options/");
    }
}
