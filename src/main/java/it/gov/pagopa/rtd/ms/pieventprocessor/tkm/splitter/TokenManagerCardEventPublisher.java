package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.support.MessageBuilder;

public final class TokenManagerCardEventPublisher {

  public static GenericHandler<TokenManagerCardChanged> asHandler(
          String outputBindingName,
          StreamBridge bridge
  ) {
    final var publisher = new TokenManagerCardEventPublisher(outputBindingName, bridge);
    return (payload, headers) -> {
      publisher.sendTokenManagerCardChanged(payload);
      return null;
    };
  }

  private final String outputBindingName;
  private final StreamBridge bridge;

  public TokenManagerCardEventPublisher(String outputBindingName, StreamBridge bridge) {
    this.outputBindingName = outputBindingName;
    this.bridge = bridge;
  }

  public boolean sendTokenManagerCardChanged(TokenManagerCardChanged cardChanged) {
    return bridge.send(outputBindingName, MessageBuilder.withPayload(cardChanged)
            .setHeader("partitionKey", cardChanged.getHashPan()).build());
  }

}
