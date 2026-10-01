package com.pedrocotta.goldenraspberry.producer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/producers")
public class ProducerController {

    private final ProducerAwardIntervalService producerAwardIntervalService;

    public ProducerController(ProducerAwardIntervalService producerAwardIntervalService) {
        this.producerAwardIntervalService = producerAwardIntervalService;
    }

    @GetMapping("/award-intervals")
    public AwardIntervalsResponse awardIntervals() {
        return producerAwardIntervalService.findMinAndMaxIntervals();
    }
}
