package it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.TestUtils;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${topics.rtd-pi-from-app.topic}"}, partitions = 1, bootstrapServersProperty = "spring.embedded.kafka.brokers")
@EnableAutoConfiguration(exclude = {TestSupportBinderAutoConfiguration.class})
@TestPropertySource("classpath:application-test.yml")
@ExtendWith(MockitoExtension.class)
@Import(ApplicationEventSplitterFlowTest.Config.class)
class ApplicationEventSplitterFlowTest {

  @Value("${topics.rtd-pi-from-app.topic}")
  private String topic;

  @Autowired
  private AbstractMessageListenerContainer<String, ApplicationBulkEvent> applicationBulkEventContainer;

  @Autowired
  private ApplicationInstrumentEventPublisher instrumentEventPublisher;

  private KafkaTemplate<String, ApplicationBulkEvent> kafkaTemplate;

  @BeforeEach
  void setup(@Autowired EmbeddedKafkaBroker broker) {
    kafkaTemplate = new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(broker), new StringSerializer(), new JsonSerializer<>())
    );
    ContainerTestUtils.waitForAssignment(applicationBulkEventContainer, broker.getPartitionsPerTopic());
    Mockito.doCallRealMethod().when(instrumentEventPublisher).handle(Mockito.any(), Mockito.any());
  }

  @AfterEach
  void teardown() {
    kafkaTemplate.destroy();
    Mockito.reset(instrumentEventPublisher);
  }

  @Test
  void whenFailToPublishSplitEventsThenRetryWholeSplitting() {
    Mockito.doReturn(false).when(instrumentEventPublisher).publish(Mockito.any(), any());
    final var applicationBulkEvent = new ApplicationBulkEvent(
            "ID_PAY",
            ApplicationBulkEvent.Operation.ADD_INSTRUMENT,
            List.of(new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), true)),
            null
    );

    kafkaTemplate.send(topic, applicationBulkEvent);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      Mockito.verify(instrumentEventPublisher, Mockito.atLeast(3)).publish(any(), any());
    });
  }

  @Test
  void whenHandleValidApplicationBulkEventEventThenSplitAndPublish() {
    final ArgumentCaptor<ApplicationInstrumentEvent> captor = ArgumentCaptor.forClass(ApplicationInstrumentEvent.class);
    final var applicationBulkEvent = new ApplicationBulkEvent(
            "ID_PAY",
            ApplicationBulkEvent.Operation.ADD_INSTRUMENT,
            List.of(new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), true),
                    new ApplicationBulkEvent.HashPanConsentItem(TestUtils.generateRandomHashPanAsString(), true)),
            null
    );


    Mockito.doReturn(true).when(instrumentEventPublisher).publish(any(), any());
    kafkaTemplate.send(topic, applicationBulkEvent);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      Mockito.verify(instrumentEventPublisher, Mockito.times(2)).publish(captor.capture(), any());
      assertThat(captor.getAllValues()).hasSize(2);
    });
  }

  @TestConfiguration
  static class Config {
    @MockBean
    private Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> splitter;

    @MockBean
    private ApplicationInstrumentEventPublisher instrumentEventPublisher;

    @MockBean
    private AbstractMessageListenerContainer<String, TokenManagerWalletChanged> tkmBulkInput;

    @MockBean(name = "tkmSplitterFlow")
    private IntegrationFlow tkmSplitterFlow;
  }
}
