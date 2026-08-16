package com.mel.expensetracker.authserver.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "organisations")
public class Organisation {

    @Id
    private UUID id;

    private String name;

    private String slug;

    protected Organisation() {
        // JPA
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}
