package it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.TestUtils;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentAdded;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentDeleted;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationBulkEventSplitterTest {

  private ApplicationBulkEventSplitter splitter;


  @BeforeEach
  void setUp() {
    splitter = new ApplicationBulkEventSplitter();
  }

  @Test
  void whenBulkIsAddInstrumentThenSplitIntoAddedInstrumentEvents() {
    final var bulkEvent = new ApplicationBulkEvent(
            "ID_PAY",
            ApplicationBulkEvent.Operation.ADD_INSTRUMENT,
            List.of(
                    new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), true),
                    new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), false)
            )
    );

    final var events = splitter.apply(bulkEvent);

    assertThat(events).hasOnlyElementsOfType(ApplicationInstrumentAdded.class)
            .map(it -> (ApplicationInstrumentAdded) it)
            .allMatch(it -> Objects.equals(bulkEvent.getApplication(), it.getApplication()))
            .allMatch(it -> bulkEvent.getHashPans().contains(new ApplicationBulkEvent.HashPanConsentItem(it.getHashPan(), it.isAllowTokenized())));
  }

  @Test
  void whenBulkIsDeleteInstrumentThenSplitIntoDeletedInstrumentEvents() {
    final var bulkEvent = new ApplicationBulkEvent(
            "ID_PAY",
            ApplicationBulkEvent.Operation.DELETE_INSTRUMENT,
            List.of(
                    new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), true),
                    new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), false)
            )
    );

    final var events = splitter.apply(bulkEvent);

    assertThat(events).hasOnlyElementsOfType(ApplicationInstrumentDeleted.class)
            .map(it -> (ApplicationInstrumentDeleted) it)
            .allMatch(it -> Objects.equals(bulkEvent.getApplication(), it.getApplication()))
            .allMatch(it -> bulkEvent.getHashPans().contains(new ApplicationBulkEvent.HashPanConsentItem(it.getHashPan(), it.isAllowTokenized())));

  }

  @Test
  void whenBulkHasNullHashPanListThenSplitIntoEmptyList() {
    final var cardEvents = splitter.apply(
            new ApplicationBulkEvent("ID_PAY", ApplicationBulkEvent.Operation.ADD_INSTRUMENT, null)
    );
    assertThat(cardEvents).isEmpty();
  }
}