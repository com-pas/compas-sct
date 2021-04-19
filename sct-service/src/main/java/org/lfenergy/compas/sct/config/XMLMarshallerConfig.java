package org.lfenergy.compas.sct.config;

import org.lfenergy.compas.commons.MarshallerWrapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(XSDFileProperties.class)
public class XMLMarshallerConfig {

    @Bean
    public MarshallerWrapper marshallerWrapper(XSDFileProperties xsdFileProperties) throws Exception {
        MarshallerWrapper.Builder builder = new MarshallerWrapper.Builder();
        builder.withSchemaMap(xsdFileProperties.getPaths());
        return builder.build();
    }
}
