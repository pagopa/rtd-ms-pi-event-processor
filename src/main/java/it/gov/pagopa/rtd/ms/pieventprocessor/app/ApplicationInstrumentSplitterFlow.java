package it.gov.pagopa.rtd.ms.pieventprocessor.app;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter.ApplicationInstrumentEventPublisher;
import java.util.List;
import java.util.function.Function;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

public class ApplicationInstrumentSplitterFlow {

  private ApplicationInstrumentSplitterFlow() {
  }

  public static IntegrationFlow createFlow(
          AbstractMessageListenerContainer<String, ApplicationBulkEvent> applicationBulkEventInput,
          Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> splitter,
          ApplicationInstrumentEventPublisher publisher,
          RequestHandlerRetryAdvice retryAdvice,
          String enrichRequestIdHeader
  ) {
    return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(
                    applicationBulkEventInput,
                    KafkaMessageDrivenChannelAdapter.ListenerMode.record
            ).id("applicationBulkEventInput").get())
            .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
            .transform(Transformers.fromJson(ApplicationBulkEvent.class))
            .enrich(enricher -> enricher.<ApplicationBulkEvent>headerFunction(enrichRequestIdHeader, m -> m.getPayload().getCorrelationId()))
            .split(ApplicationBulkEvent.class, splitter)
            .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
            .handle(ApplicationInstrumentEvent.class, publisher, e -> e.advice(retryAdvice))
            .get();

  }
}
