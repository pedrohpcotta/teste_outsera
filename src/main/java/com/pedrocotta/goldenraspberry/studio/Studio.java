package com.pedrocotta.goldenraspberry.studio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "studio")
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studio_seq")
    @SequenceGenerator(name = "studio_seq", sequenceName = "studio_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Studio() {
    }

    public Studio(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
