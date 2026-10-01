package com.pedrocotta.goldenraspberry.producer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProducerAwardIntervalService {

    private static final Comparator<ProducerAwardInterval> TIE_ORDER = Comparator
            .comparing(ProducerAwardInterval::producer)
            .thenComparingInt(ProducerAwardInterval::previousWin);

    private final ProducerRepository producerRepository;

    public ProducerAwardIntervalService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional(readOnly = true)
    public AwardIntervalsResponse findMinAndMaxIntervals() {
        List<ProducerAwardInterval> intervals = consecutiveIntervals(producerRepository.findAllWinsOrderedByProducerAndYear());
        if (intervals.isEmpty()) {
            return AwardIntervalsResponse.empty();
        }

        int min = intervals.stream().mapToInt(ProducerAwardInterval::interval).min().orElseThrow();
        int max = intervals.stream().mapToInt(ProducerAwardInterval::interval).max().orElseThrow();

        return new AwardIntervalsResponse(withInterval(intervals, min), withInterval(intervals, max));
    }

    private static List<ProducerAwardInterval> consecutiveIntervals(List<ProducerWin> orderedWins) {
        List<ProducerAwardInterval> intervals = new ArrayList<>();
        for (int i = 1; i < orderedWins.size(); i++) {
            ProducerWin previous = orderedWins.get(i - 1);
            ProducerWin following = orderedWins.get(i);
            if (previous.producer().equals(following.producer())) {
                intervals.add(new ProducerAwardInterval(
                        following.producer(),
                        following.year() - previous.year(),
                        previous.year(),
                        following.year()));
            }
        }
        return intervals;
    }

    private static List<ProducerAwardInterval> withInterval(List<ProducerAwardInterval> intervals, int interval) {
        return intervals.stream()
                .filter(candidate -> candidate.interval() == interval)
                .sorted(TIE_ORDER)
                .toList();
    }
}
