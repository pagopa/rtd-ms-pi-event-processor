package it.gov.pagopa.rtd.ms.pieventprocessor.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "integration-flow-consumers")
@Data
public final class IntegrationFlowKafkaProperties {
  public final Map<String, String> tkmBulkConsumer;
  public final Map<String, String> applicationBulkConsumer;
}
