package de.rearth.planty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@SpringBootApplication
public class PlantyApplication implements WebMvcConfigurer {

    public static void main(String[] args) {

        SpringApplication.run(PlantyApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(Paths.get("upload-dir").toAbsolutePath().toString())
                .setCachePeriod(10);
    }

}
