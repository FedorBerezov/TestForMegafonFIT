package ru.megafon.b2b.sus.rest;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.response.ResponseCode;
import ru.megafon.b2b.service.RateLimitConfigService;
import ru.megafon.b2b.sus.data.Subscriber;
import ru.megafon.b2b.sus.service.DataCheckService;
import ru.megafon.b2b.sus.service.UpdateStatusService;

import java.time.Duration;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateStatusController.class)
class UpdateStatusControllerTest {

    private static final String REQUEST_BODY = "{\"id\": \"1\", \"data\": {\n" +
            "\"account_number\": \"1\",\n" +
            "\"msisdn\":\"4\",\n" +
            "\"status\": \"status\"\n" +
            "}}";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UpdateStatusService updateStatusService;

    @MockBean
    private RateLimitConfigService rateLimitConfigService;

    @MockBean
    private DataCheckService dataCheckService;

    @Test
    public void updateStatusIsOk() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        data.setStatus("status");
        when(updateStatusService.updateStatus(data))
                .thenReturn(Mono.just(data));

        MvcResult result = this.mockMvc.perform(
                post("/updatestatus")
                        .content(REQUEST_BODY)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        ResponseMessage<Subscriber> responseMessage = (ResponseMessage<Subscriber>) result.getAsyncResult();
        Assert.assertEquals(200, responseMessage.getStatus().getCode());
    }

    @Test
    public void updateStatusIs404() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        data.setStatus("status");
        when(updateStatusService.updateStatus(data)).thenReturn(Mono.empty());

        MvcResult result = this.mockMvc.perform(
                post("/updatestatus")
                        .content(REQUEST_BODY)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        ResponseMessage<Subscriber> responseMessage = (ResponseMessage<Subscriber>) result.getAsyncResult();
        Assert.assertEquals(404, responseMessage.getStatus().getCode());
    }

    @Test
    public void updateCommonException() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        data.setStatus("status");
        when(updateStatusService.updateStatus(data)).thenReturn(Mono.error(new AppException(ResponseCode.ESB_400)));

        MvcResult result = this.mockMvc.perform(
                post("/updatestatus")
                        .content(REQUEST_BODY)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        ResponseMessage<Subscriber> responseMessage = (ResponseMessage<Subscriber>) result.getAsyncResult();
        Assert.assertEquals(400, responseMessage.getStatus().getCode());
    }

    @Test
    public void updateStatusIs500() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        data.setStatus("status");
        when(updateStatusService.updateStatus(data))
                .thenReturn(Mono.error(new AppException(ResponseCode.ESB_500)));

        MvcResult result = this.mockMvc.perform(
                post("/updatestatus")
                        .content(REQUEST_BODY)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        ResponseMessage<Subscriber> responseMessage = (ResponseMessage<Subscriber>) result.getAsyncResult();
        Assert.assertEquals(503, responseMessage.getStatus().getCode());
    }

    @Test
    public void updateStatusIs429() {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(1, Duration.ofMinutes(1)));
        when(dataCheckService.isSubscriberValid(any())).thenReturn(true);
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        data.setStatus("status");
        when(updateStatusService.updateStatus(data)).thenReturn(Mono.just(data));

        Supplier<MvcResult> requestOp = () -> {
            try {
                return this.mockMvc.perform(
                        post("/updatestatus")
                                .content(REQUEST_BODY)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk()).andReturn();
            }
            catch (Exception e) {
                throw new RuntimeException("Exception while performing mvc request", e);
            }
        };
        MvcResult result = requestOp.get();
        ResponseMessage responseMessage = (ResponseMessage) result.getAsyncResult();
        Assert.assertEquals(200, responseMessage.getStatus().getCode());
        result = requestOp.get();
        responseMessage = (ResponseMessage) result.getAsyncResult();
        Assert.assertEquals(429, responseMessage.getStatus().getCode());
    }

    private Bucket createBucket(int rateLimit, Duration period) {
        Refill refill = Refill.intervally(rateLimit, period);
        Bandwidth limit = Bandwidth.classic(rateLimit, refill);
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}