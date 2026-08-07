package com.carrental.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${file.upload-dir:uploads}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String base = Paths.get(uploadDir).toAbsolutePath().normalize().toString();

		// Car images: car.image is stored as a bare filename and looked up under uploads/cars/
		registry.addResourceHandler("/images/cars/**")
				.addResourceLocations("file:" + base + "/cars/");

		// Profile images: stored under uploads/
		registry.addResourceHandler("/images/**")
				.addResourceLocations("file:" + base + "/");
	}
}
