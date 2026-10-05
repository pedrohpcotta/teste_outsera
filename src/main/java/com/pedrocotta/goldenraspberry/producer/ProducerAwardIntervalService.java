package com.pedrocotta.goldenraspberry.producer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProducerAwardIntervalService {

    private final ProducerRepository producerRepository;

    public ProducerAwardIntervalService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional(readOnly = true)
    public AwardIntervalsResponse findMinAndMaxIntervals() {
        List<ProducerWin> orderedWins = producerRepository.findAllWinsOrderedByProducerAndYear();

        List<ProducerAwardInterval> min = new ArrayList<>();
        List<ProducerAwardInterval> max = new ArrayList<>();
        int minInterval = Integer.MAX_VALUE;
        int maxInterval = Integer.MIN_VALUE;

        for (int i = 1; i < orderedWins.size(); i++) {
            ProducerWin previous = orderedWins.get(i - 1);
            ProducerWin following = orderedWins.get(i);
            if (!previous.producer().equals(following.producer())) {
                continue;
            }

            ProducerAwardInterval interval = new ProducerAwardInterval(
                    following.producer(),
                    following.year() - previous.year(),
                    previous.year(),
                    following.year());

            if (interval.interval() < minInterval) {
                minInterval = interval.interval();
                min.clear();
            }
            if (interval.interval() == minInterval) {
                min.add(interval);
            }

            if (interval.interval() > maxInterval) {
                maxInterval = interval.interval();
                max.clear();
            }
            if (interval.interval() == maxInterval) {
                max.add(interval);
            }
        }

        return new AwardIntervalsResponse(min, max);
    }
}
