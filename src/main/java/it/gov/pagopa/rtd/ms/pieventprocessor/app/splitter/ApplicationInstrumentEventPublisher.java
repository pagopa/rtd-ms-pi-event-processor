package it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.common.errors.FailToPublishException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

public class ApplicationInstrumentEventPublisher implements GenericHandler<ApplicationInstrumentEvent> {

  private final String outputBindingName;
  private final StreamBridge bridge;

  public ApplicationInstrumentEventPublisher(String outputBindingName, StreamBridge bridge) {
    this.outputBindingName = outputBindingName;
    this.bridge = bridge;
  }

  public boolean publish(ApplicationInstrumentEvent instrumentEvent) {
    final var cloudEvent = CloudEvent.of(instrumentEvent);
    return bridge.send(outputBindingName, MessageBuilder.withPayload(cloudEvent)
            .setHeader("partitionKey", instrumentEvent.hashPan()).build());
  }

  @Override
  public Object handle(ApplicationInstrumentEvent payload, MessageHeaders headers) {
    if (this.publish(payload)) {
      return null;
    } else {
      throw new FailToPublishException("Fail to send application instrument event " + payload);
    }
  }
}
