package org.lfenergy.compas.sct.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.commons.CommonConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("compas.scl.schema")
public class XSDFileProperties {

    private Map<String, String> paths;

    @PostConstruct
    public void init() {
        if (this.paths == null || this.paths.isEmpty()) {
            paths = new HashMap<>();
            paths.put(CommonConstants.XML_DEFAULT_NS_PREFIX, CommonConstants.XML_DEFAULT_XSD_PATH);
        }
    }
}
