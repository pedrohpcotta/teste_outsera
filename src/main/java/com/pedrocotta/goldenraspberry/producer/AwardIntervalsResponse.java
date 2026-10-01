package com.pedrocotta.goldenraspberry.producer;

import java.util.List;

public record AwardIntervalsResponse(List<ProducerAwardInterval> min, List<ProducerAwardInterval> max) {

    public static AwardIntervalsResponse empty() {
        return new AwardIntervalsResponse(List.of(), List.of());
    }
}
