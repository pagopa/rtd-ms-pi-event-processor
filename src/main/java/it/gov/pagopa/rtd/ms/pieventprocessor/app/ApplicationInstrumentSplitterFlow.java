package it.gov.pagopa.rtd.ms.pieventprocessor.app;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter.ApplicationInstrumentEventPublisher;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.List;
import java.util.function.Function;

public class ApplicationInstrumentSplitterFlow {

  private ApplicationInstrumentSplitterFlow() {
  }

  public static IntegrationFlow createFlow(
          AbstractMessageListenerContainer<String, ApplicationBulkEvent> applicationBulkEventInput,
          Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> splitter,
          ApplicationInstrumentEventPublisher publisher,
          RequestHandlerRetryAdvice retryAdvice
  ) {
    return IntegrationFlows.from(Kafka.messageDrivenChannelAdapter(
                    applicationBulkEventInput,
                    KafkaMessageDrivenChannelAdapter.ListenerMode.record
            ).id("applicationBulkEventInput").get())
            .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
            .split(ApplicationBulkEvent.class, splitter)
            .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
            .handle(ApplicationInstrumentEvent.class, publisher, e -> e.advice(retryAdvice))
            .get();

  }
}
