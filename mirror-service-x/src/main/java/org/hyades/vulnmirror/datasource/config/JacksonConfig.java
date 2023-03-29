//package org.hyades.vulnmirror.datasource.config;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//
//import javax.enterprise.inject.Produces;
//import javax.inject.Named;
//
//public class JacksonConfig {
//    @Produces
//    @Named("objectMapper")
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper()
//                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        return objectMapper.registerModule(new JavaTimeModule());
//    }
//}
