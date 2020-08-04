package ru.megafon.b2b.sus.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.megafon.b2b.sus.data.Subscriber;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataCheckServiceTest {
    @InjectMocks
    private DataCheckService dataCheckService;

    @Test
    public void isSubscriberValidTest() {

        assertTrue(dataCheckService.isSubscriberValid(Subscriber.builder().accountNumber("1").status("1").build()));

        assertFalse(dataCheckService.isSubscriberValid(null));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().build()));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().accountNumber("1").build()));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().accountNumber("1").msisdn("1").build()));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().msisdn("1").build()));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().msisdn("1").status("status").build()));
        assertFalse(dataCheckService.isSubscriberValid(Subscriber.builder().status("status").build()));
    }

}