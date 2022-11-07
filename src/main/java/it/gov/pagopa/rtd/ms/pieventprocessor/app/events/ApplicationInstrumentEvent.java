package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEventType;

public interface ApplicationInstrumentEvent extends CloudEventType {
  String hashPan();
}