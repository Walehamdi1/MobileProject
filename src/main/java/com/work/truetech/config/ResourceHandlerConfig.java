package com.work.truetech.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceHandlerConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String upload;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/phones/**")
                .addResourceLocations("file:"+upload+"/phones/").setCachePeriod(0);

        registry.addResourceHandler("/api/models/**")
                .addResourceLocations("file:"+upload+"/models/").setCachePeriod(0);

        registry.addResourceHandler("/api/options/**")
                .addResourceLocations("file:"+upload+"/options/").setCachePeriod(0);

        registry.addResourceHandler("/api/video/**")
                .addResourceLocations("file:"+upload+"/video/").setCachePeriod(0);

        registry.addResourceHandler("/api/products/**")
                .addResourceLocations("file:"+upload+"/products/").setCachePeriod(0);
    }
}
