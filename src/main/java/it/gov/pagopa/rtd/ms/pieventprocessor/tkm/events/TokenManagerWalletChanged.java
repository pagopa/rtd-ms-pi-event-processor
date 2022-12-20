package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;
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
