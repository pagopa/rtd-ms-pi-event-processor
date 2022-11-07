package it.gov.pagopa.rtd.ms.pieventprocessor.ports.rest;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("")
@Validated
public interface KafkaRestController {

  @PutMapping(value = "/tkm-bulk-update")
  @ResponseStatus(HttpStatus.OK)
  void sendTkmUpdateEvent(@RequestBody TokenManagerWalletChanged event);

  @PutMapping(value = "/rtd-pi-from-app")
  @ResponseStatus(HttpStatus.OK)
  void sendApplicationInstrumentBulkEvent(@RequestBody ApplicationBulkEvent event);
}
