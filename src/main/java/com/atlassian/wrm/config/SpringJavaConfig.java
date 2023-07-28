package com.atlassian.wrm.config;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
public class SpringJavaConfig {
    @Bean
    public PageBuilderService pageBuilderService() {
        return importOsgiService(PageBuilderService.class);
    }

    @Bean
    public TemplateRenderer templateRenderer() {
        return importOsgiService(TemplateRenderer.class);
    }
}