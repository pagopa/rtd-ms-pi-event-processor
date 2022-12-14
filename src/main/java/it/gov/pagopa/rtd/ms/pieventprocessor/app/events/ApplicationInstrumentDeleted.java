package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ApplicationInstrumentDeleted extends ApplicationInstrumentEvent {

  public static final String TYPE = "ApplicationInstrumentDeleted";

  public ApplicationInstrumentDeleted(String hashPan, boolean allowTokenized, String application) {
    super(hashPan, allowTokenized, application);
  }

  @Override
  public String cloudEventType() {
    return TYPE;
  }
}
