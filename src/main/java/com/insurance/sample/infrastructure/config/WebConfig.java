package com.insurance.sample.infrastructure.config;

import com.insurance.sample.domain.model.LineOfBusiness;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Allows ?lineOfBusiness=A%26H (A&H) to map correctly to LineOfBusiness.ANH
        registry.addConverter(new StringToLineOfBusinessConverter());
    }

    static class StringToLineOfBusinessConverter implements Converter<String, LineOfBusiness> {
        @Override
        public LineOfBusiness convert(String source) {
            if (source == null || source.isBlank()) return null;
            // Try display name first ("A&H", "Property", etc.)
            try {
                return LineOfBusiness.fromDisplayName(source);
            } catch (IllegalArgumentException e) {
                // Fall back to enum name ("ANH", "Property", etc.)
                return LineOfBusiness.valueOf(source);
            }
        }
    }
}
