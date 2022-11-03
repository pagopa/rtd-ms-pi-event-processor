package it.gov.pagopa.rtd.ms.pieventprocessor.tkm;

import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter.TokenManagerCardEventHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;

import java.util.List;
import java.util.function.Function;

public final class TkmSplitterFlow {

  private TkmSplitterFlow() {}

  public static IntegrationFlow createFlow(
          MessageProducerSupport tkmBulkInput,
          Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> tkmSplitter,
          RequestHandlerRetryAdvice tkmRetryAdvice,
          TokenManagerCardEventHandler cardEventHandler
  ) {
    return IntegrationFlows.from(tkmBulkInput)
            .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
            .split(TokenManagerWalletChanged.class, tkmSplitter)
            .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
            .handle(TokenManagerCardChanged.class, cardEventHandler, e -> e.advice(tkmRetryAdvice))
            .get();
  }

}
