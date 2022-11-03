package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This event is sent by tkm microservice, basically represent a user wallet with cards and token associated.
 * This message will be splitter to avoid bulk operations over db and
 * provide better horizontal scalability using Splitter EIP
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class TokenManagerWalletChanged {

  private String taxCode;
  @JsonFormat(pattern =  "yyyy-MM-dd HH:mm:ss:SSSS")
  private LocalDateTime timestamp;
  private List<CardItem> cards;

  @Data
  public static final class CardItem {
    private final String hpan;
    private final String par;
    private final CardChangeType action;
    private final List<HashTokenItem> htokens;
  }

  @Data
  public static final class HashTokenItem {
    private final String htoken;
    private final HashTokenChangeType haction;
  }
}
