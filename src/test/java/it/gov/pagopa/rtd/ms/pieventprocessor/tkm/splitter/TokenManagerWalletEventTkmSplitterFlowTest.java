package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationBulkEvent;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.CardChangeType;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerWalletChanged;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${topics.tkm-write-update.topic}"}, partitions = 1, bootstrapServersProperty = "spring.embedded.kafka.brokers")
@EnableAutoConfiguration(exclude = {TestSupportBinderAutoConfiguration.class})
@TestPropertySource("classpath:application-test.yml")
@ExtendWith(MockitoExtension.class)
@Import(TokenManagerWalletEventTkmSplitterFlowTest.Config.class)
class TokenManagerWalletEventTkmSplitterFlowTest {

  @Value("${topics.tkm-write-update.topic}")
  private String topic;

  @Autowired
  private AbstractMessageListenerContainer<String, TokenManagerWalletChanged> tkmBulkInput;

  @Autowired
  private Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> splitter;

  @Autowired
  private TokenManagerCardEventPublisher cardEventPublisher;

  private KafkaTemplate<String, TokenManagerWalletChanged> kafkaTemplate;

  @BeforeEach
  void setup(@Autowired EmbeddedKafkaBroker broker) {
    kafkaTemplate = new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(broker), new StringSerializer(), new JsonSerializer<>())
    );
    ContainerTestUtils.waitForAssignment(tkmBulkInput, broker.getPartitionsPerTopic());
  }

  @AfterEach
  void teardown() {
    kafkaTemplate.destroy();
    Mockito.reset(cardEventPublisher, splitter);
  }

  @Test
  void whenFailToPublishSplitEventsThenRetryWholeSplitting() {
    Mockito.doReturn(false).when(cardEventPublisher).sendTokenManagerCardChanged(any());

    Mockito.doReturn(List.of(
            new TokenManagerCardChanged("hpan", "taxCode", "par", List.of(), OffsetDateTime.now(), CardChangeType.INSERT_UPDATE))
    ).when(splitter).apply(any());

    final var walletChanged = new TokenManagerWalletChanged("taxCode", OffsetDateTime.now(), List.of(
            new TokenManagerWalletChanged.CardItem("hpan", "par", CardChangeType.INSERT_UPDATE, null)
    ));

    kafkaTemplate.send(topic, walletChanged);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      Mockito.verify(cardEventPublisher, Mockito.atLeast(3)).sendTokenManagerCardChanged(any());
    });
  }

  @ParameterizedTest
  @ValueSource(classes = {IllegalArgumentException.class})
  void whenFailToSplitDueToInvalidPayloadThenNoRetryHappens(Class<? extends Exception> exception) {
    final var walletChanged = new TokenManagerWalletChanged("taxCode", OffsetDateTime.now(), List.of(
            new TokenManagerWalletChanged.CardItem("hpan", "par", CardChangeType.INSERT_UPDATE, null)
    ));

    Mockito.doThrow(exception).when(splitter).apply(any());
    kafkaTemplate.send(topic, walletChanged);
    await().during(Duration.ofSeconds(5)).untilAsserted(() -> {
      Mockito.verify(splitter, Mockito.times(1)).apply(any());
    });
  }

  @Test
  void whenReceiveWalletChangeEventThenSplitAndPublish() {
    final ArgumentCaptor<TokenManagerCardChanged> captor = ArgumentCaptor.forClass(TokenManagerCardChanged.class);
    final var walletChanged = new TokenManagerWalletChanged("taxCode", OffsetDateTime.now(), List.of());
    Mockito.doReturn(List.of(
            new TokenManagerCardChanged("hpan", "taxCode", "par", List.of(), OffsetDateTime.now(), CardChangeType.INSERT_UPDATE),
            new TokenManagerCardChanged("hpan2", "taxCode", "par2", List.of(), OffsetDateTime.now(), CardChangeType.INSERT_UPDATE))
    ).when(splitter).apply(any());


    Mockito.doReturn(true).when(cardEventPublisher).sendTokenManagerCardChanged(any());
    kafkaTemplate.send(topic, walletChanged);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      Mockito.verify(cardEventPublisher, Mockito.times(2)).sendTokenManagerCardChanged(captor.capture());
      assertThat(captor.getAllValues()).hasSize(2);
    });
  }

  @TestConfiguration
  static class Config {
    @MockBean
    private Function<TokenManagerWalletChanged, List<TokenManagerCardChanged>> splitter;

    @MockBean
    private TokenManagerCardEventPublisher cardEventPublisher;

    @MockBean(name = "applicationSplitterFlow")
    private IntegrationFlow applicationSplitterFlow;

    @MockBean
    private AbstractMessageListenerContainer<String, ApplicationBulkEvent> applicationBulkEventContainer;
  }
}