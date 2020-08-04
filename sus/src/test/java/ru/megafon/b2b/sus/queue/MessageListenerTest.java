package ru.megafon.b2b.sus.queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.RequestMessage;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.sus.data.Subscriber;
import ru.megafon.b2b.sus.service.DataCheckService;
import ru.megafon.b2b.sus.service.UpdateStatusService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.megafon.b2b.response.ResponseCode.ESB_404;
import static ru.megafon.b2b.response.ResponseCode.ESB_500;
import static ru.megafon.b2b.response.ResponseCode.ESB_503;


@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class MessageListenerTest {

    private final String outQueueName = "TEST.OUT";

    @Mock
    private AmqpTemplate template;

    @Mock
    private UpdateStatusService updateStatusService;

    @Mock
    private DataCheckService dataCheckService;

    @InjectMocks
    private MessageListener messageListener;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(messageListener, "outQueue", outQueueName);
    }

    @Test
    public void testSuccess() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").status("new_status").build();
        Subscriber subscriberRes = Subscriber.builder().msisdn("1").accountNumber("4").status("old_status").build();

        when(updateStatusService.updateStatus(any())).thenReturn(Mono.just(subscriberRes));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);

        messageListener.in(RequestMessage.<Subscriber>builder().data(subscriberReq).build());
        verify(template, times(1)).convertAndSend(eq(outQueueName), eq(ResponseMessage.ok(subscriberRes)));
    }

    @Test
    public void testError500() {
        Subscriber subscriberReq = Subscriber.builder().build();

        when(updateStatusService.updateStatus(any())).thenReturn(Mono.error(new AppException(ESB_500)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);

        messageListener.in(RequestMessage.<Subscriber>builder().data(subscriberReq).build());
        verify(template, times(1)).convertAndSend(eq(outQueueName), eq(ResponseMessage.error(ESB_503, subscriberReq)));
    }

    @Test
    public void testError() {
        Subscriber subscriberReq = Subscriber.builder().build();

        when(updateStatusService.updateStatus(any())).thenReturn(Mono.error(new AppException(ESB_404)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);

        messageListener.in(RequestMessage.<Subscriber>builder().data(subscriberReq).build());
        verify(template, times(1)).convertAndSend(eq(outQueueName), eq(ResponseMessage.error(ESB_404, subscriberReq)));
    }

    @Test
    public void testCommonException() {
        Subscriber subscriberReq = Subscriber.builder().build();

        when(updateStatusService.updateStatus(any())).thenReturn(Mono.error(new RuntimeException("Test exception")));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);

        messageListener.in(RequestMessage.<Subscriber>builder().data(subscriberReq).build());
        verify(template, times(1)).convertAndSend(eq(outQueueName), eq(ResponseMessage.error(ESB_500, subscriberReq)));
    }

}