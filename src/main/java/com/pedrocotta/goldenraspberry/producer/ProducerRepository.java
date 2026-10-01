package com.pedrocotta.goldenraspberry.producer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

    @Query("""
            select new com.pedrocotta.goldenraspberry.producer.ProducerWin(p.name, m.year)
            from Movie m
            join m.producers p
            where m.winner = true
            order by p.name, m.year
            """)
    List<ProducerWin> findAllWinsOrderedByProducerAndYear();
}
