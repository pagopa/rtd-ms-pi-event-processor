package it.gov.pagopa.rtd.ms.pieventprocessor.tkm;

import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter.TokenManagerCardEventHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.List;
import java.util.function.Function;

public final class TkmSplitterFlow {

  private TkmSplitterFlow() {}

  public static IntegrationFlow createFlow(
          AbstractMessageListenerContainer<String, TokenManagerWalletChanged> tkmBulkInput,
          Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> tkmSplitter,
          RequestHandlerRetryAdvice tkmRetryAdvice,
          TokenManagerCardEventHandler cardEventHandler
  ) {
    final var input = Kafka.messageDrivenChannelAdapter(tkmBulkInput, KafkaMessageDrivenChannelAdapter.ListenerMode.record).get();
    return IntegrationFlows.from(input)
            .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
            .split(TokenManagerWalletChanged.class, tkmSplitter)
            .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
            .handle(TokenManagerCardChanged.class, cardEventHandler, e -> e.advice(tkmRetryAdvice))
            .get();
  }

}
