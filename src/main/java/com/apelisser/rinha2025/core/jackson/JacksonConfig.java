package com.apelisser.rinha2025.core.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    @ConditionalOnProperty(value = "spring.jackson.install-afterburner", havingValue = "true")
    public Jackson2ObjectMapperBuilderCustomizer jacksonInstallAfterburner() {
        return builder -> builder.modulesToInstall(new AfterburnerModule());
    }

    @Bean
    @ConditionalOnProperty(value = "spring.jackson.install-time-module", havingValue = "true")
    public Jackson2ObjectMapperBuilderCustomizer jacksonInstallTimeModule() {
        return builder -> builder.modulesToInstall(new JavaTimeModule());
    }

    @Bean
    @ConditionalOnProperty(value = "spring.jackson.disable-default-features", havingValue = "true")
    public Jackson2ObjectMapperBuilderCustomizer jacksonDisableDefaultFeatures() {
        return builder -> builder.featuresToDisable(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            SerializationFeature.FAIL_ON_EMPTY_BEANS,
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
            MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_GETTERS,
            MapperFeature.AUTO_DETECT_IS_GETTERS,
            MapperFeature.AUTO_DETECT_SETTERS
        );
    }
    
}
