package ru.megafon.b2b.service;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import ru.megafon.b2b.consul.dto.ConsulKeyData;
import ru.megafon.b2b.util.Buckets;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RateLimitConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitConfigService.class);

    private final AtomicReference<Bucket> bucketReference;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RateLimitConfigService(String rateLimitKey, WebClient webClient, long updateRateLimitIntervalMillis, int defaultRps) {
        LOG.debug(String.format("Setting default rate limit = %s rps", defaultRps));
        Bucket bucket = Buckets.build(defaultRps, Duration.ofSeconds(1));
        this.bucketReference = new AtomicReference<>(bucket);

        scheduler.scheduleAtFixedRate(() -> {
            LOG.debug("Trying to update rate limit info");
            try {
                Bucket newRateLimit = buildBucket(rateLimitKey, webClient);
                this.bucketReference.set(newRateLimit);
            }
            catch (Exception e) {
                LOG.error("Failed to update rate limit", e);
            }
        }, 10000, updateRateLimitIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public Bucket getBucket() {
        return bucketReference.get();
    }

    private Bucket buildBucket(String rateLimitKey, WebClient webClient) {
        ConsulKeyData keyData = webClient.get().uri("/v1/kv/" + rateLimitKey)
                .retrieve().bodyToFlux(ConsulKeyData.class).blockFirst();
        if (keyData == null) {
            throw new RuntimeException("Failed to get rate limit from consul");
        }

        return Buckets.buildFromConsulData(keyData);
    }

}

