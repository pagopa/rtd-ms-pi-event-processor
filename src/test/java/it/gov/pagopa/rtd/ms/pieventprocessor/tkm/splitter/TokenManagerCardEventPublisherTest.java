package it.gov.pagopa.rtd.ms.pieventprocessor.tkm.splitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.pieventprocessor.TestUtils;
import it.gov.pagopa.rtd.ms.pieventprocessor.configuration.CommonConsumerConfiguration;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.CardChangeType;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = {CommonConsumerConfiguration.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${topics.rtd-slit-by-pi.topic}"}, partitions = 2, bootstrapServersProperty = "spring.embedded.kafka.brokers")
@EnableAutoConfiguration(exclude = {TestSupportBinderAutoConfiguration.class})
@TestPropertySource("classpath:application-test.yml")
@ExtendWith(MockitoExtension.class)
class TokenManagerCardEventPublisherTest {
  private static final String OUT_BINDING = "rtdSplitByPi-out-0";

  @Value("${topics.rtd-slit-by-pi.topic}")
  private String topic;

  @Autowired
  private StreamBridge bridge;
  private TokenManagerCardEventPublisher cardEventPublisher;

  private Consumer<String, String> consumer;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup(@Autowired EmbeddedKafkaBroker broker) {
    final var consumerProperties = KafkaTestUtils.consumerProps("group", "true", broker);
    consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProperties).createConsumer();
    consumer.subscribe(List.of(topic));
    cardEventPublisher = new TokenManagerCardEventPublisher(OUT_BINDING, bridge);
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

  @Test
  void whenPublishCardChangedEventThenShouldBeProducedOnDifferentPartitions() {
    final var events = IntStream.range(0, 10)
            .mapToObj(i -> TestUtils.prepareRandomTokenManagerEvent(CardChangeType.INSERT_UPDATE).build());

    events.forEach(it -> cardEventPublisher.sendTokenManagerCardChanged(it));

    await().ignoreException(NoSuchElementException.class).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var records = consumer.poll(Duration.ZERO);
      assertThat(records).hasSize(10);
      assertThat(records.partitions()).hasSize(2);
    });
  }

  @Test
  void whenPublishCardChangedEventThenMustHaveSamePayload() {
    final var events = IntStream.range(0, 10)
            .mapToObj(i -> TestUtils.prepareRandomTokenManagerEvent(CardChangeType.INSERT_UPDATE).build())
            .collect(Collectors.toList());

    events.forEach(it -> cardEventPublisher.sendTokenManagerCardChanged(it));

    await().ignoreException(NoSuchElementException.class).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var records = consumer.poll(Duration.ZERO);
      assertThat(records).map(it -> objectMapper.readValue(it.value(), TokenManagerCardChanged.class))
              .hasSameElementsAs(events);
    });
  }
}