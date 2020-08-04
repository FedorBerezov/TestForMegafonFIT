package ru.megafon.b2b.is.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.is.data.Subscriber;
import ru.megafon.b2b.is.repository.SubscriberMapper;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private SubscriberMapper subscriberMapper;

    @InjectMocks
    private SubscriberService subscriberService;

    @Test
    public void getSubscriber() {
        given(subscriberMapper.getSubscriber("1"))
                .willReturn(Subscriber.builder().msisdn("4").accountNumber("1").build());

        Mono<Subscriber> result = subscriberService.getSubscriber("1");
        Optional<Subscriber> subscriber = result.blockOptional();
        Assert.assertTrue(subscriber.isPresent());
        Assert.assertEquals("1", subscriber.get().getAccountNumber());
        Assert.assertEquals("4", subscriber.get().getMsisdn());

        verify(subscriberMapper).getSubscriber("1");
    }

    @Test
    public void subscriberNotFound() {
        given(subscriberMapper.getSubscriber("1")).willReturn(null);

        Mono<Subscriber> result = subscriberService.getSubscriber("1");
        Optional<Subscriber> subscriber = result.blockOptional();
        Assert.assertFalse(subscriber.isPresent());

        verify(subscriberMapper).getSubscriber("1");
    }
}