package it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent;

import lombok.Data;

@Data
public class CloudEvent<T> {

  public static <T extends CloudEventType> CloudEvent<T> of(T event) {
    return new CloudEvent<>(event.cloudEventType(), event);
  }

  private final String type;
  private final T data;
}
