package it.gov.pagopa.rtd.ms.pieventprocessor;

import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.CardChangeType;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.HashTokenChangeType;
import it.gov.pagopa.rtd.ms.pieventprocessor.tkm.events.TokenManagerCardChanged;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestUtils {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    public static String generateRandomHashPanAsString() {
        return randomString(64);
    }

    public static String randomString(int length) {
        return IntStream.range(0, length)
                .mapToObj(i -> "" + ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())))
                .collect(Collectors.joining(""));
    }

    public static TokenManagerCardChanged.TokenManagerCardChangedBuilder prepareRandomTokenManagerEvent(CardChangeType action) {
        return TokenManagerCardChanged.builder()
                .par(randomString(4))
                .hashPan(generateRandomHashPanAsString())
                .taxCode(randomString(10))
                .hashTokens(generateRandomHashTokenEvent(10))
                .changeType(action);
    }

    public static List<TokenManagerCardChanged.HashTokenEvent> generateRandomHashTokenEvent(int which) {
        final var random = new Random();
        return IntStream.range(0, which)
                .mapToObj(i -> new TokenManagerCardChanged.HashTokenEvent(
                        generateRandomHashPanAsString(),
                        random.nextDouble() < 0.5 ? HashTokenChangeType.DELETE : HashTokenChangeType.INSERT_UPDATE
                ))
                .toList();
    }
}
