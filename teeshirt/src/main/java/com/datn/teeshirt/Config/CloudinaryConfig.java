package com.datn.teeshirt.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dzfxd7vnz",
                "api_key", "757486764318297",
                "api_secret", "KG7PobVNKOA-xoN__M3HOje3CQo"));
    }
}