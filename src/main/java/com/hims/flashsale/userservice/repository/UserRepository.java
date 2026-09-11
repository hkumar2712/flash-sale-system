package com.hims.flashsale.userservice.repository;

import com.hims.flashsale.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * This is where Spring Data JPA does something that looks like magic the first time
 * you see it: this is just an INTERFACE - we never write an implementation class,
 * yet at runtime Spring generates a full working implementation automatically.
 *
 * Extending JpaRepository<User, UUID> immediately gives us, for free, without writing
 * any code: save(), findById(), findAll(), deleteById(), count(), and more - all backed
 * by real SQL that Hibernate generates.
 *
 * The <User, UUID> generic parameters tell Spring: "this repository manages User entities,
 * whose primary key type is UUID" - that's how it knows what SQL to generate.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * THIS is the more impressive part: we wrote NO SQL and NO implementation,
     * just this method signature - yet Spring Data JPA parses the method NAME itself
     * ("findBy" + "Email") and derives a working query from it:
     *   SELECT * FROM users WHERE email = ?
     *
     * Optional<User> (not just User) is a deliberate signal to every caller:
     * "this might not find anything - you MUST handle the not-found case,"
     * instead of silently returning null and risking a NullPointerException later.
     */
    Optional<User> findByEmail(String email);
}