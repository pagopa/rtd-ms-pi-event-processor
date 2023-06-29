package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEventType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class TokenManagerCardChanged implements CloudEventType {

  public static final String TYPE = "TokenManagerCardChanged";

  @NotNull
  @NotBlank
  private String hashPan;

  private String taxCode;

  private String par;

  private List<HashTokenEvent> hashTokens;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  @NotNull
  private CardChangeType changeType;

  @Override
  public String cloudEventType() {
    return TYPE;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static final class HashTokenEvent {
    private String hashToken;
    private HashTokenChangeType changeType;
  }
}
