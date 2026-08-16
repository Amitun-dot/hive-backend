package com.hive.user;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
