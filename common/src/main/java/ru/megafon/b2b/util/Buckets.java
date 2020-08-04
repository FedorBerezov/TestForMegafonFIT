package ru.megafon.b2b.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import ru.megafon.b2b.consul.dto.ConsulKeyData;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class Buckets {

    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public static Bucket buildFromConsulData(ConsulKeyData keyData) {
        byte[] rpsValueBytes = DECODER.decode(keyData.getValue());
        String rpsValue = new String(rpsValueBytes, StandardCharsets.UTF_8);
        int rps = Integer.parseInt(rpsValue);
        return build(rps, Duration.ofSeconds(1));
    }

    public static Bucket build(int rps, Duration period) {
        Refill refill = Refill.intervally(rps, period);
        Bandwidth limit = Bandwidth.classic(rps, refill);
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
