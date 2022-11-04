package it.gov.pagopa.rtd.ms.pieventprocessor.common.errors;

public class FailToPublishException extends RuntimeException {
  public FailToPublishException(String message) {
    super(message);
  }
}
