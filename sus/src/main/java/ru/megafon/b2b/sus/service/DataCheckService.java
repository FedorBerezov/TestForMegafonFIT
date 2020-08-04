package ru.megafon.b2b.sus.service;

import org.springframework.stereotype.Service;
import ru.megafon.b2b.sus.data.Subscriber;

@Service
public class DataCheckService {

    public boolean isSubscriberValid(final Subscriber subscriber) {
        if (subscriber == null) {
            return false;
        }
        else {
            return subscriber.getAccountNumber() != null && subscriber.getStatus() != null;
        }
    }
}
