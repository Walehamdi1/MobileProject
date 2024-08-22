package com.work.truetech.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceHandlerConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/phones/**")
                .addResourceLocations("classpath:/fils/phones/");
        registry.addResourceHandler("/api/models/**")
                .addResourceLocations("classpath:/fils/models/");
        registry.addResourceHandler("/api/options/**")
                .addResourceLocations("classpath:/fils/options/");
        registry.addResourceHandler("/api/video/**")
                .addResourceLocations("classpath:/fils/video/");
    }
}
