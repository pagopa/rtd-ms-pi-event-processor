package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ApplicationInstrumentAdded extends ApplicationInstrumentEvent {

  public static final String TYPE = "ApplicationInstrumentAdded";

  public ApplicationInstrumentAdded(String hashPan, boolean allowTokenized, String application) {
    super(hashPan, allowTokenized, application);
  }

  @Override
  public String cloudEventType() {
    return TYPE;
  }
}
