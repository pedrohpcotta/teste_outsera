package com.pedrocotta.goldenraspberry.movie;

import com.pedrocotta.goldenraspberry.producer.Producer;
import com.pedrocotta.goldenraspberry.studio.Studio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq")
    @SequenceGenerator(name = "movie_seq", sequenceName = "movie_seq", allocationSize = 50)
    private Long id;

    @Column(name = "release_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean winner;

    @ManyToMany
    @JoinTable(
            name = "movie_studio",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id"))
    @OrderBy("name")
    private Set<Studio> studios = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "movie_producer",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "producer_id"))
    @OrderBy("name")
    private Set<Producer> producers = new LinkedHashSet<>();

    protected Movie() {
    }

    public Movie(int year, String title, boolean winner, Set<Studio> studios, Set<Producer> producers) {
        this.year = year;
        this.title = title;
        this.winner = winner;
        this.studios = new LinkedHashSet<>(studios);
        this.producers = new LinkedHashSet<>(producers);
    }

    public Long getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public boolean isWinner() {
        return winner;
    }

    public Set<Studio> getStudios() {
        return studios;
    }

    public Set<Producer> getProducers() {
        return producers;
    }
}
