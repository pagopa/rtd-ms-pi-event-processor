package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEventType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class ApplicationInstrumentEvent implements CloudEventType {
  private final String hashPan;
  private final boolean allowTokenized;
  private final String application;
}
