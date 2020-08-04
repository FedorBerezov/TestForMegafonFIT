package ru.megafon.b2b.is.rest;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.is.data.Subscriber;
import ru.megafon.b2b.is.service.SubscriberService;
import ru.megafon.b2b.service.RateLimitConfigService;

import java.time.Duration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DbEnrichController.class)
class DbEnrichControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SubscriberService subscriberService;

    @MockBean
    private RateLimitConfigService rateLimitConfigService;

    @Test
    public void dbEnrichShouldReturnOkResponse() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        Subscriber data = new Subscriber();
        data.setMsisdn("4");
        data.setAccountNumber("1");
        when(subscriberService.getSubscriber("1"))
                .thenReturn(Mono.just(data));

        MvcResult result = this.mockMvc.perform(get("/dbenrich/subscriber/1"))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        ResponseMessage<Subscriber> responseMessage = (ResponseMessage<Subscriber>) result.getAsyncResult();
        Assert.assertEquals(200, responseMessage.getStatus().getCode());
        Assert.assertEquals("4", responseMessage.getData().getMsisdn());
    }

    @Test
    public void dbEnrichShouldReturn429Response() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(1, Duration.ofMinutes(1)));
        when(subscriberService.getSubscriber("1"))
                .thenReturn(Mono.just(new Subscriber("4", "1")));

        MvcResult firstResult = this.mockMvc.perform(get("/dbenrich/subscriber/1"))
                .andExpect(status().isOk())
                .andReturn();

        ResponseMessage responseMessage = (ResponseMessage) firstResult.getAsyncResult();
        Assert.assertEquals(200, responseMessage.getStatus().getCode());

        MvcResult secondResult = this.mockMvc.perform(get("/dbenrich/subscriber/2"))
                .andExpect(status().isOk())
                .andReturn();

        ResponseMessage secondResponse = (ResponseMessage) secondResult.getAsyncResult();
        Assert.assertEquals(429, secondResponse.getStatus().getCode());
    }

    @Test
    public void dbEnrichShouldReturn404Response() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(subscriberService.getSubscriber("1"))
                .thenReturn(Mono.empty());

        MvcResult firstResult = this.mockMvc.perform(get("/dbenrich/subscriber/1"))
                .andExpect(status().isOk())
                .andReturn();

        ResponseMessage responseMessage = (ResponseMessage) firstResult.getAsyncResult();
        Assert.assertEquals(404, responseMessage.getStatus().getCode());
    }

    @Test
    public void dbEnrichShouldReturn500Response() throws Exception {
        when(rateLimitConfigService.getBucket()).thenReturn(createBucket(10, Duration.ofSeconds(1)));
        when(subscriberService.getSubscriber("1"))
                .thenReturn(Mono.error(new RuntimeException("Failed to connect to db")));

        MvcResult firstResult = this.mockMvc.perform(get("/dbenrich/subscriber/1"))
                .andExpect(status().isOk())
                .andReturn();

        ResponseMessage responseMessage = (ResponseMessage) firstResult.getAsyncResult();
        Assert.assertEquals(500, responseMessage.getStatus().getCode());
    }


    private Bucket createBucket(int rateLimit, Duration period) {
        Refill refill = Refill.intervally(rateLimit, period);
        Bandwidth limit = Bandwidth.classic(rateLimit, refill);
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

}