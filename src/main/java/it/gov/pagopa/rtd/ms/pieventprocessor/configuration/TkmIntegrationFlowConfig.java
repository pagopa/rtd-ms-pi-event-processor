package it.gov.pagopa.rtd.ms.pieventprocessor.configuration;

import it.gov.pagopa.rtd.ms.pieventprocessor.configuration.properties.IntegrationFlowKafkaProperties;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter.TokenManagerCardEventPublisher;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter.TokenManagerWalletEventSplitter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.handler.advice.RetryStateGenerator;
import org.springframework.integration.handler.advice.SpelExpressionRetryStateGenerator;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Configuration
@EnableConfigurationProperties
public class TkmIntegrationFlowConfig {

    private static final String TARGET_OUT_BINDING = "rtdSplitByPi-out-0";

    @Bean
    public IntegrationFlow tkmSplitterFlow(
            KafkaMessageDrivenChannelAdapter<String, TokenManagerWalletChanged> tkmBulkInput,
            Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> tkmSplitter,
            RequestHandlerRetryAdvice tkmRetryAdvice,
            GenericHandler<TokenManagerCardChanged> cardEventPublisher
    ) {
        return IntegrationFlows.from(tkmBulkInput)
                .log(LoggingHandler.Level.INFO, m -> "Received message to split: " + m.getPayload())
                .split(TokenManagerWalletChanged.class, tkmSplitter)
                .log(LoggingHandler.Level.INFO, m -> "Split message " + m.getPayload())
                .handle(TokenManagerCardChanged.class, cardEventPublisher, e -> e.advice(tkmRetryAdvice))
                .get();
    }

    @Bean
    Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> tkmSplitter() {
        return new TokenManagerWalletEventSplitter();
    }

    @Bean
    GenericHandler<TokenManagerCardChanged> cardEventPublisher(StreamBridge bridge) {
        return TokenManagerCardEventPublisher.asHandler(TARGET_OUT_BINDING, bridge);
    }

    @Bean
    KafkaMessageDrivenChannelAdapter<String, TokenManagerWalletChanged> tkmBulkInput(
            IntegrationFlowKafkaProperties flowKafkaProperties,
            DefaultErrorHandler consumerErrorHandler
    ) {
        final var consumerFactory = new DefaultKafkaConsumerFactory<String, TokenManagerWalletChanged>(
                new HashMap<>(flowKafkaProperties.tkmBulkConsumer)
        );
        final var containerProperties = new ContainerProperties(flowKafkaProperties.tkmBulkConsumer.get("topic"));
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        final var container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setCommonErrorHandler(consumerErrorHandler);
        return Kafka.messageDrivenChannelAdapter(container, KafkaMessageDrivenChannelAdapter.ListenerMode.record).get();
    }


    /**
     * Retry configuration for splitter integration flow. It defines an infinite retry policy
     * with 3 seconds as backoff policy. Also allow to retry the message processing only when a
     * retryable exceptions happens.
     * Official doc: <a href="https://docs.spring.io/spring-integration/reference/html/messaging-endpoints.html#retry-advice">...</a>
     *
     * @param tkmSplitFlowRetryStateGenerator The state generator to enable statefull retry.
     */
    @Bean
    RequestHandlerRetryAdvice tkmRetryAdvice(
            RetryStateGenerator tkmSplitFlowRetryStateGenerator,
            RetryTemplate retryTemplate
    ) {
        final var retryAdvice = new RequestHandlerRetryAdvice();
        retryAdvice.setRetryStateGenerator(tkmSplitFlowRetryStateGenerator);
        retryAdvice.setRetryTemplate(retryTemplate);
        return retryAdvice;
    }

    @Bean
    RetryStateGenerator tkmSplitFlowRetryStateGenerator(
            Set<Class<? extends Throwable>> retryableExceptions
    ) {
        final var stateGenerator = new SpelExpressionRetryStateGenerator("headers['correlationId']");
        stateGenerator.setClassifier(new BinaryExceptionClassifier(retryableExceptions, true));
        return stateGenerator;
    }
}
