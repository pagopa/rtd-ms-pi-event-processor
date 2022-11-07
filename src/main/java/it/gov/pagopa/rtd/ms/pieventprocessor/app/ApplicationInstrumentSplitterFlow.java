package it.gov.pagopa.rtd.ms.pieventprocessor.app;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter.ApplicationInstrumentEventPublisher;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;

import java.util.List;
import java.util.function.Function;

public class ApplicationInstrumentSplitterFlow {

  private ApplicationInstrumentSplitterFlow() {}

  public static IntegrationFlow createFlow(
          MessageProducerSupport applicationBulkEventInput,
          Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> splitter,
          ApplicationInstrumentEventPublisher publisher,
          RequestHandlerRetryAdvice retryAdvice
  ) {

    return IntegrationFlows.from(applicationBulkEventInput)
            .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
            .split(ApplicationBulkEvent.class, splitter)
            .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
            .handle(ApplicationInstrumentEvent.class, publisher, e -> e.advice(retryAdvice))
            .get();

  }
}
