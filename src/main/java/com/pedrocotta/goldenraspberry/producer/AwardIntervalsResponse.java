package com.pedrocotta.goldenraspberry.producer;

import java.util.List;

public record AwardIntervalsResponse(List<ProducerAwardInterval> min, List<ProducerAwardInterval> max) {
}
