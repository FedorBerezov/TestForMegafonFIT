package ru.megafon.b2b.sus.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {
    @JsonProperty("account_number")
    private String accountNumber;
    private String msisdn;
    private String status;
}
