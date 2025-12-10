package com.e_commerce.e_commerce.configuration;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    // create connection with Cloudinary
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "ddlicbwze");
        config.put("api_key", "584716938164654");
        config.put("api_secret", "mPjFUUnUhGrdmPDaGD-07UTj1Bs");
        return new Cloudinary(config);
    }
}
