package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
  @JsonDeserialize(using = TkmDateDeserializer.class)
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

  public static class TkmDateDeserializer extends JsonDeserializer<OffsetDateTime> {

    static final DateTimeFormatter TKM_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSSS");
    static final ZoneId EUROPE_ROME_ZONE = ZoneId.of("Europe/Rome");

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      final var dateTime = jsonParser.readValueAs(String.class);
      try {
        return OffsetDateTime.parse(dateTime);
      } catch (Exception e) {
        return LocalDateTime.parse(dateTime, TKM_DATE_FORMAT)
                .atZone(EUROPE_ROME_ZONE)
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
      }
    }
  }
}
