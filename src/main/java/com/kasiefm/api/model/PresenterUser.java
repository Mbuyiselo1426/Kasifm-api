package com.kasiefm.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "presenter_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class PresenterUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100, unique = true)
    private String username;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 255)
    private String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private UserRole role;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public PresenterUser() { }
    public PresenterUser(String username, String passwordHash, String displayName, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }
    @PrePersist void setCreatedAt() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UserRole getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
