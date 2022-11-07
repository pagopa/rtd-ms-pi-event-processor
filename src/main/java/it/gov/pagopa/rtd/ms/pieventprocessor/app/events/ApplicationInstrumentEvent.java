package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEventType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public abstract class ApplicationInstrumentEvent implements CloudEventType {
  private final String hashPan;
  private final boolean allowTokenized;
  private final String application;
}
