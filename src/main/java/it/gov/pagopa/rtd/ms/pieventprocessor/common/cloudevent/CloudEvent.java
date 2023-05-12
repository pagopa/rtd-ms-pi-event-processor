package it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudEvent<T> {

  public static <T extends CloudEventType> CloudEvent<T> of(T event) {
    return of(event, null);
  }

  public static <T extends CloudEventType> CloudEvent<T> of(T event, String correlationId) {
    return new CloudEvent<>(event.cloudEventType(), correlationId, event);
  }

  @NotNull
  private String type;
  private String correlationId;
  @NotNull
  private T data;
}
