package com.nowcoder.community.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class EsConversionsConfig {
    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(List.of(
                new LocalDateTimeToStringConverter()
        ));
    }

    @WritingConverter
    public static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime source) {
            // 写入 2025-10-14T12:34:56Z 这种带时区的 ISO 格式
            return source.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }
}