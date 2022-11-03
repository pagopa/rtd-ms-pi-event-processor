package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class TokenManagerWalletEventSplitter implements Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> {

  @Override
  public List<TokenManagerCardChanged> apply(TokenManagerWalletChanged tokenManagerWalletChanged) {
    return Optional.ofNullable(tokenManagerWalletChanged.getCards())
            .orElse(Collections.emptyList())
            .stream()
            .map(it -> TokenManagerCardChanged.builder()
                    .hashPan(it.hpan())
                    .par(it.par())
                    .changeType(it.action())
                    .taxCode(tokenManagerWalletChanged.getTaxCode())
                    .hashTokens(buildHashTokenEvents(it))
                    .timestamp(tokenManagerWalletChanged.getTimestamp())
                    .build()
            )
            .toList();
  }

  private List<TokenManagerCardChanged.HashTokenEvent> buildHashTokenEvents(TokenManagerWalletChanged.CardItem card) {
    return Optional.ofNullable(card.htokens())
            .orElse(Collections.emptyList())
            .stream()
            .map(token -> new TokenManagerCardChanged.HashTokenEvent(token.htoken(), token.haction()))
            .toList();
  }
}
