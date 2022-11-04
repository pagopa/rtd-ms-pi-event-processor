package it.gov.pagopa.rtd.ms.pieventprocessor.configuration;

import it.gov.pagopa.rtd.ms.pieventprocessor.common.errors.FailToPublishException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UnknownFormatConversionException;

@Configuration
public class CommonConsumerConfiguration {

    private static final Long FIXED_RETRY_BACKOFF_INTERVAL = 3000L;

    /**
     * A list of exceptions considered as "transient", so these are used as
     * retryable exceptions with kafka consumers.
     */
    @Bean("retryableExceptions")
    Set<Class<? extends Exception>> consumerRetryableExceptions() {
        return Set.of(
                SocketTimeoutException.class,
                ConnectException.class,
                UnknownHostException.class,
                IOException.class,
                FailToPublishException.class
        );
    }

    /**
     * A list of non-transient exceptions. Tipically validation and schema error
     * where no recovery operation are available.
     */
    @Bean("fatalExceptions")
    Set<Class<? extends Exception>> consumerFatalExceptions() {
        return Set.of(
                IllegalArgumentException.class,
                ConstraintViolationException.class,
                UnknownFormatConversionException.class
        );
    }

    @Bean
    DefaultErrorHandler consumerErrorHandler(
            Set<Class<? extends Exception>> retryableExceptions,
            Set<Class<? extends Exception>> fatalExceptions
    ) {
        // previously called seek to error handler
        // this error handler allow to always retry the retryable exceptions
        // like db connection error or write error. While allow to set
        // not retryable exceptions like validation error which cannot be recovered with a retry.
        final var errorHandler = new DefaultErrorHandler(
                new FixedBackOff(FIXED_RETRY_BACKOFF_INTERVAL, FixedBackOff.UNLIMITED_ATTEMPTS)
        );
        errorHandler.defaultFalse();
        retryableExceptions.forEach(errorHandler::addRetryableExceptions);
        fatalExceptions.forEach(errorHandler::addNotRetryableExceptions);
        return errorHandler;
    }

    @Bean
    RetryTemplate retryTemplate(Set<Class<? extends Throwable>> retryableExceptions) {
        final var retryBuilder = RetryTemplate.builder()
                .infiniteRetry()
                .traversingCauses()
                .fixedBackoff(FIXED_RETRY_BACKOFF_INTERVAL);

        retryableExceptions.forEach(retryBuilder::retryOn);
        return retryBuilder.build();
    }
}
