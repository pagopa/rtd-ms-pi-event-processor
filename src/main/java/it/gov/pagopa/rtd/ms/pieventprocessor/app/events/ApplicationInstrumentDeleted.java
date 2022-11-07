package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import lombok.Data;

@Data
public class ApplicationInstrumentDeleted implements ApplicationInstrumentEvent {

  public static final String TYPE = "ApplicationInstrumentDeleted";

  private final String hashPan;
  private final boolean allowTokenized;
  private final String application;

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public String hashPan() {
    return hashPan;
  }
}
