package com.hims.flashsale.userservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * @Entity tells Hibernate/JPA: "this class maps to a database table."
 * Without this annotation, it's just a plain Java class with no persistence behavior at all.
 *
 * @Table(name = "users") - IMPORTANT: we explicitly name the table "users", plural.
 * "user" (singular) is a RESERVED KEYWORD in PostgreSQL's SQL grammar (and ANSI SQL in general).
 * If we let Hibernate default to the class name "User" -> table "user", every query
 * would need to be quoted specially to avoid syntax errors. Naming it "users" sidesteps
 * this entirely - a small but real gotcha worth knowing before you hit it blindly.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * @Id marks this field as the primary key.
     * @GeneratedValue(strategy = GenerationType.UUID) tells Hibernate to generate
     * a random UUID itself (in Java, before the INSERT even happens) rather than
     * asking the database to generate it (which is how auto-increment Long ids work).
     * This is part of why UUIDs suit distributed systems: the ID exists before
     * the row is ever persisted, so it can be referenced elsewhere (e.g. put in a
     * Kafka event) even before the transaction commits.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // unique = true creates a UNIQUE constraint at the DB level - the database itself
    // will reject a duplicate email even if application code somehow tries to allow it.
    // nullable = false means the DB column is NOT NULL.
    @Column(nullable = false, unique = true)
    private String email;

    // We will NEVER store a plain-text password. This field holds a BCrypt HASH,
    // produced in Phase 1.2 when we build the registration endpoint. The entity
    // itself doesn't know or care about hashing - that's the service layer's job.
    @Column(nullable = false)
    private String passwordHash;

    /**
     * @Enumerated(EnumType.STRING) stores the enum as its NAME ("BUYER", "ADMIN")
     * in the database column, not as a number (EnumType.ORDINAL would store 0, 1, 2).
     * STRING is safer: if you ever reorder or insert a new value in the Role enum,
     * ORDINAL storage would silently corrupt existing data's meaning. STRING is
     * immune to that, at the minor cost of a few extra bytes per row.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * @CreationTimestamp is a Hibernate-specific annotation (not raw JPA) that
     * automatically sets this field to "now" the moment the row is first inserted -
     * we never set it manually in our own code. updatable = false ensures it can
     * never be silently changed on a later update.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // JPA REQUIRES a no-argument constructor (even if private/protected) - it's how
    // Hibernate instantiates entities internally via reflection before populating fields.
    protected User() {
    }

    // Our own constructor for actually creating a new User in application code.
    // Notice: no "id" or "createdAt" parameter - those are generated automatically,
    // never set by us directly.
    public User(String email, String passwordHash, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // --- Getters only, no setters ---
    // Deliberately no setters here. Entities that are "immutable after creation"
    // (aside from fields explicitly meant to change) are easier to reason about -
    // any future field updates should go through explicit, intention-revealing
    // methods (e.g. changePassword(...)) rather than generic setters, once we need them.
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}