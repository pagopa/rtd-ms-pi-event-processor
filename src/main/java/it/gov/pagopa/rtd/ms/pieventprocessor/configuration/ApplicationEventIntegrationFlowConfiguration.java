package it.gov.pagopa.rtd.ms.pieventprocessor.configuration;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.ApplicationInstrumentSplitterFlow;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter.ApplicationBulkEventSplitter;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter.ApplicationInstrumentEventPublisher;
import it.gov.pagopa.rtd.ms.pieventprocessor.configuration.properties.IntegrationFlowKafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
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
public class ApplicationEventIntegrationFlowConfiguration {

    private static final String TARGET_OUT_BINDING = "rtdSplitByPi-out-0";

    @Bean("applicationSplitterFlow")
    public IntegrationFlow applicationEventSplitterFlow(
            KafkaMessageDrivenChannelAdapter<String, ApplicationBulkEvent> applicationBulkEventInput,
            Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> applicationSplitter,
            ApplicationInstrumentEventPublisher applicationInstrumentEventPublisher,
            RequestHandlerRetryAdvice applicationRetryAdvice
    ) {
        return ApplicationInstrumentSplitterFlow.createFlow(
                applicationBulkEventInput, applicationSplitter, applicationInstrumentEventPublisher, applicationRetryAdvice
        );
    }

    @Bean
    Function<ApplicationBulkEvent, List<ApplicationInstrumentEvent>> applicationSplitter() {
        return new ApplicationBulkEventSplitter();
    }

    @Bean
    ApplicationInstrumentEventPublisher applicationInstrumentEventPublisher(StreamBridge bridge) {
        return new ApplicationInstrumentEventPublisher(TARGET_OUT_BINDING, bridge);
    }

    @Bean
    KafkaMessageDrivenChannelAdapter<String, ApplicationBulkEvent> applicationBulkEventInput(
            IntegrationFlowKafkaProperties flowKafkaProperties,
            DefaultErrorHandler consumerErrorHandler
    ) {
        final var consumerFactory = new DefaultKafkaConsumerFactory<String, ApplicationBulkEvent>(
                new HashMap<>(flowKafkaProperties.applicationBulkConsumer)
        );
        final var containerProperties = new ContainerProperties(flowKafkaProperties.applicationBulkConsumer.get("topic"));
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        final var container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setCommonErrorHandler(consumerErrorHandler);
        return Kafka.messageDrivenChannelAdapter(container, KafkaMessageDrivenChannelAdapter.ListenerMode.record)
                .id("applicationBulkEventInput")
                .get();
    }


    @Bean
    RequestHandlerRetryAdvice applicationRetryAdvice(
            RetryStateGenerator applicationSplitFlowRetryGenerator,
            RetryTemplate retryTemplate
    ) {
        final var retryAdvice = new RequestHandlerRetryAdvice();
        retryAdvice.setRetryStateGenerator(applicationSplitFlowRetryGenerator);
        retryAdvice.setRetryTemplate(retryTemplate);
        return retryAdvice;
    }

    @Bean
    RetryStateGenerator applicationSplitFlowRetryGenerator(
            Set<Class<? extends Throwable>> retryableExceptions
    ) {
        final var stateGenerator = new SpelExpressionRetryStateGenerator("headers['correlationId']");
        stateGenerator.setClassifier(new BinaryExceptionClassifier(retryableExceptions, true));
        return stateGenerator;
    }
}
