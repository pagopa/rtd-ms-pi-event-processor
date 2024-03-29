package it.gov.pagopa.rtd.ms.pieventprocessor.app.splitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.pieventprocessor.TestUtils;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentAdded;
import it.gov.pagopa.rtd.ms.pieventprocessor.app.events.ApplicationInstrumentDeleted;
import it.gov.pagopa.rtd.ms.pieventprocessor.common.cloudevent.CloudEvent;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${topics.rtd-slit-by-pi.topic}"}, partitions = 2, bootstrapServersProperty = "spring.embedded.kafka.brokers")
@TestPropertySource("classpath:application-test.yml")
class ApplicationInstrumentEventPublisherTest {

  private static final String OUT_BINDING = "rtdSplitByPi-out-0";
  private static final String CORRELATION_HEADER = "requestId";

  @Value("${topics.rtd-slit-by-pi.topic}")
  private String topic;

  @Autowired
  private StreamBridge bridge;

  private ApplicationInstrumentEventPublisher instrumentEventPublisher;

  private Consumer<String, String> consumer;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup(@Autowired EmbeddedKafkaBroker broker) {
    final var consumerProperties = KafkaTestUtils.consumerProps("group", "true", broker);
    consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProperties).createConsumer();
    consumer.subscribe(List.of(topic));
    instrumentEventPublisher = new ApplicationInstrumentEventPublisher(OUT_BINDING, bridge, CORRELATION_HEADER);
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

  @Test
  void whenPublishApplicationInstrumentEventThenShouldBeProducedOnDifferentPartitions() {
    final var events = IntStream.range(0, 10)
            .mapToObj(i -> TestUtils.randomApplicationInstrumentEvent());

    events.forEach(it -> instrumentEventPublisher.publish(it, null));

    await().ignoreException(NoSuchElementException.class).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var records = consumer.poll(Duration.ZERO);
      assertThat(records).hasSize(10);
      assertThat(records.partitions()).hasSize(2);
    });
  }

  @Test
  void whenPublishApplicationInstrumentAddedThenRightCloudEventMustBeProduced() {
    final var cloudEventType = new TypeReference<CloudEvent<ApplicationInstrumentAdded>>(){};
    final var hashPan = TestUtils.generateRandomHashPanAsString();
    final var event = new ApplicationInstrumentAdded(hashPan, true, "ID_PAY");
    instrumentEventPublisher.publish(event, "1234");
    await().ignoreException(NoSuchElementException.class).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var records = consumer.poll(Duration.ZERO);
      assertThat(records).map(it -> objectMapper.readValue(it.value(), cloudEventType))
              .allMatch(it -> Objects.equals(it.getType(), ApplicationInstrumentAdded.TYPE))
              .allMatch(it -> Objects.isNull(it.getData()))
              .allMatch(it -> Objects.equals(it.getData().getHashPan(), hashPan))
              .allMatch(it -> Objects.equals(it.getCorrelationId(), "1234"));
    });
  }

  @Test
  void whenPublishApplicationInstrumentDeletedThenRightCloudEventMustBeProduced() {
    final var cloudEventType = new TypeReference<CloudEvent<ApplicationInstrumentDeleted>>(){};
    final var hashPan = TestUtils.generateRandomHashPanAsString();
    final var event = new ApplicationInstrumentDeleted(hashPan, true, "ID_PAY");
    instrumentEventPublisher.publish(event, null);
    await().ignoreException(NoSuchElementException.class).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var records = consumer.poll(Duration.ZERO);
      assertThat(records).map(it -> objectMapper.readValue(it.value(), cloudEventType))
              .allMatch(it -> Objects.equals(it.getType(), ApplicationInstrumentAdded.TYPE))
              .allMatch(it -> Objects.isNull(it.getData()))
              .allMatch(it -> Objects.equals(it.getData().getHashPan(), hashPan))
              .allMatch(it -> Objects.isNull(it.getCorrelationId()));
    });
  }
}