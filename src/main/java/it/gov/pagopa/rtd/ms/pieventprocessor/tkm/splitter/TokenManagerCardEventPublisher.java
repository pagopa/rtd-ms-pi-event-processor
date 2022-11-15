package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;

public final class TokenManagerCardEventPublisher {

  private final String outputBindingName;
  private final StreamBridge bridge;

  public TokenManagerCardEventPublisher(String outputBindingName, StreamBridge bridge) {
    this.outputBindingName = outputBindingName;
    this.bridge = bridge;
  }

  public boolean sendTokenManagerCardChanged(TokenManagerCardChanged cardChanged) {
    final var cloudEvent = CloudEvent.of(cardChanged);
    return bridge.send(outputBindingName, MessageBuilder.withPayload(cloudEvent)
            .setHeader("partitionKey", cardChanged.getHashPan()).build());
  }

}
