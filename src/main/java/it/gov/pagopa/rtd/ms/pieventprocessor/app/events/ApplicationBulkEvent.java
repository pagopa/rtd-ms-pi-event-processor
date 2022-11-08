package it.gov.pagopa.rtd.ms.pieventprocessor.app.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ApplicationBulkEvent {

  @JsonProperty(required = true)
  @NotNull
  @NotBlank
  private String application;

  @JsonProperty(required = true)
  @NotNull
  @NotBlank
  private Operation operationType;

  @JsonAlias("hpanList")
  @JsonProperty(required = true)
  private List<HashPanConsentItem> hashPans;

  public enum Operation {
    ADD_INSTRUMENT,
    DELETE_INSTRUMENT
  }

  @Data
  public static final class HashPanConsentItem {
    @JsonAlias("hpan")
    private final String hashPan;
    private final boolean consent;
  }
}
