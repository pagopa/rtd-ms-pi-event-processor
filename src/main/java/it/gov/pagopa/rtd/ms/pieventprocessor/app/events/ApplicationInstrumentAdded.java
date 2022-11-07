package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import lombok.Data;

@Data
public class ApplicationInstrumentAdded implements ApplicationInstrumentEvent {

  public static final String TYPE = "ApplicationInstrumentAdded";

  private final String hashPan;
  private final boolean allowTokenized;
  private final String application;

  @Override
  public String cloudEventType() {
    return TYPE;
  }

  @Override
  public String hashPan() {
    return hashPan;
  }
}
