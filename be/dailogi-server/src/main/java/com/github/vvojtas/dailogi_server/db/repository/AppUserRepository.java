package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByName(String name);
    boolean existsByName(String name);
} 