package smart.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import smart.util.Helper;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Configuration
public class JsonConfig {
    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat(AppConfig.DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer() {
            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return Helper.parseDate(p.getText());
            }
        });
        javaTimeModule.addDeserializer(Timestamp.class, new DateDeserializers.TimestampDeserializer() {
            @Override
            public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new Timestamp(Helper.parseDate(p.getText()).getTime());
            }
        });

        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(Helper.DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(Helper.DATE_TIME_FORMATTER));

        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(Helper.DATE_TIME_FORMATTER_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(Helper.DATE_TIME_FORMATTER_DATE));

        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(Helper.DATE_TIME_FORMATTER_TIME));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(Helper.DATE_TIME_FORMATTER_TIME));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(javaTimeModule);
        return mapper;
    }
}
