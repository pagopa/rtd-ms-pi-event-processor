package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.errors.FailToPublishException;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;

public class TokenManagerCardEventHandler implements GenericHandler<TokenManagerCardChanged> {

  private final TokenManagerCardEventPublisher publisher;

  public TokenManagerCardEventHandler(TokenManagerCardEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public Object handle(TokenManagerCardChanged payload, MessageHeaders headers) {
    final var isSent = publisher.sendTokenManagerCardChanged(payload);
    if (isSent)
      return null;
    else
      throw new FailToPublishException("Failed to send token manager card changed event " + payload);
  }
}
