package com.projectmaster.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule to handle Java 8 time types (Instant, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        // Configure time serialization
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Register Hibernate6Module to handle Hibernate proxies
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        
        // Configure the module to handle lazy loading properly
        hibernate6Module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernate6Module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        hibernate6Module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        
        mapper.registerModule(hibernate6Module);
        
        return mapper;
    }
}