package it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentAdded;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentDeleted;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicationBulkEventSplitter implements Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> {
  @Override
  public List<ApplicationInstrumentEvent> apply(ApplicationBulkEvent applicationBulkEvent) {
    return Optional.ofNullable(applicationBulkEvent.getHashPans())
            .orElse(Collections.emptyList())
            .stream().map(it -> mapToInstrumentEvent(applicationBulkEvent.getOperationType(), applicationBulkEvent.getApplication(), it))
            .collect(Collectors.toList());
  }

  private ApplicationInstrumentEvent mapToInstrumentEvent(
          ApplicationBulkEvent.Operation operation,
          String application,
          ApplicationBulkEvent.HashPanConsentItem hashPan
  ) {
    return operation == ApplicationBulkEvent.Operation.ADD_INSTRUMENT ?
            new ApplicationInstrumentAdded(hashPan.getHashPan(), hashPan.isConsent(), application) :
            new ApplicationInstrumentDeleted(hashPan.getHashPan(), hashPan.isConsent(), application);
  }
}
