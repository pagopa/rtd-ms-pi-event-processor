package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TkmDateDeserializerTest {

  private static final String NON_ISO_DATE_TIME = "2022-10-19 12:17:10:1770";
  private static final String ISO_DATE_TIME = "2022-10-19T12:17:10.177Z";
  private TokenManagerWalletChanged.TkmDateDeserializer tkmDateDeserializer;
  private JsonParser stubJsonParser;

  @BeforeEach
  void setup() {
    tkmDateDeserializer = new TokenManagerWalletChanged.TkmDateDeserializer();
    stubJsonParser = Mockito.mock(JsonParser.class);
  }

  @Test
  void whenDateIsCustomThenParseToOffsetDateTime() throws IOException {
    final var expectedOffsetDateTime = LocalDateTime.parse(NON_ISO_DATE_TIME, TokenManagerWalletChanged.TkmDateDeserializer.TKM_DATE_FORMAT)
            .atZone(TokenManagerWalletChanged.TkmDateDeserializer.EUROPE_ROME_ZONE)
            .toOffsetDateTime();

    Mockito.doReturn(NON_ISO_DATE_TIME).when(stubJsonParser).readValueAs(String.class);

    final var parsedDate = tkmDateDeserializer.deserialize(stubJsonParser, null);

    assertThat(parsedDate).isEqualTo(expectedOffsetDateTime);
  }

  @Test
  void whenDateIsIS8061ThenParseToOffsetDateTime() throws IOException {
    final var expectedOffsetDateTime = OffsetDateTime.parse(ISO_DATE_TIME);

    Mockito.doReturn(ISO_DATE_TIME).when(stubJsonParser).readValueAs(String.class);

    final var parsedDate = tkmDateDeserializer.deserialize(stubJsonParser, null);

    assertThat(parsedDate).isEqualTo(expectedOffsetDateTime);
  }

}