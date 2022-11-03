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

  public record CardItem(String hpan, String par, CardChangeType action, List<HashTokenItem> htokens) {}

  public record HashTokenItem(String htoken, HashTokenChangeType haction) { }
}
