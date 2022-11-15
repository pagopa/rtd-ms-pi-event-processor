package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEventType;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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

  private LocalDateTime timestamp;

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
