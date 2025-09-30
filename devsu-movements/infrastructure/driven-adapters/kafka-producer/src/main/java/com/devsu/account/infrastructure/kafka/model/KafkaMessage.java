package com.devsu.account.infrastructure.kafka.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KafkaMessage<T> {

    private Header header;

    private Body<T> body;

    @Getter
    @Setter
    @ToString
    public static class Header {
        private String traceId;
    }

    @Getter
    @Setter
    @ToString
    public static class Body<T> {
        private String eventName;

        private String domainEntity;

        private String eventTimestamp;

        private T eventData;
    }
}
