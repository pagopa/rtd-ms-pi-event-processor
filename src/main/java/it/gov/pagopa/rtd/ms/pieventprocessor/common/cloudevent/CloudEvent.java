package it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudEvent<T> {

  public static <T extends CloudEventType> CloudEvent<T> of(T event) {
    return new CloudEvent<>(event.cloudEventType(), event);
  }

  private String type;
  private T data;
}
