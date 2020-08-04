package ru.megafon.b2b.consul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConsulKeyData {
    @JsonProperty("Key")
    private String key;
    @JsonProperty("Flags")
    private String flags;
    @JsonProperty("Value")
    private String value;
    @JsonProperty("CreateIndex")
    private String createIndex;
    @JsonProperty("ModifyIndex")
    private String modifyIndex;
}
