package com.mel.expensetracker.authserver.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    // Eager: the organisation slug is needed for token claims after this
    // entity's persistence-context boundary has closed, so it must travel
    // with the User rather than be fetched lazily on demand.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<String> authorities = new HashSet<>();

    protected User() {
        // JPA
    }

    public UUID getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
