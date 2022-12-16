package it.gov.pagopa.rtd.ms.pieventprocessor.ports.rest;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
public class KafkaRestControllerImpl implements KafkaRestController {

  private static final String TKM_BULK_CARD_BINDING = "tkmWalletTestProducer-out-0";
  private static final String RTD_FROM_APP_BINDING = "rtdPiFromAppTestProducer-out-0";

  private final StreamBridge streamBridge;

  @Autowired
  KafkaRestControllerImpl(
          StreamBridge streamBridge
  ) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void sendTkmUpdateEvent(TokenManagerWalletChanged event) {
    log.info("Sending tkm event {}", event);
    final var sent = streamBridge.send(
            TKM_BULK_CARD_BINDING,
            MessageBuilder.withPayload(event).setHeader("partitionKey", "0").build()
    );
    log.info("Tkm event sent {}", sent);
  }

  @Override
  public void sendApplicationInstrumentBulkEvent(ApplicationBulkEvent event) {
    log.info("Sending application instrument bulk event {}", event);
    final var sent = streamBridge.send(
            RTD_FROM_APP_BINDING,
            MessageBuilder.withPayload(event).setHeader("partitionKey", "0").build()
    );
    log.info("Application instrument bulk event sent {}", sent);
  }
}
