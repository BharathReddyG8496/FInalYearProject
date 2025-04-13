package com.example.finalyearproject.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from /uploads/** path
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///D:/Games/FInalYearProject/uploads/");
    }
}
