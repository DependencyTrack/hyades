package org.acme.common;

import io.smallrye.config.ConfigMapping;
import org.apache.kafka.common.protocol.types.Field;

@ConfigMapping(prefix = "poc")
public interface ApplicationProperty {

}
