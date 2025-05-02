package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {
} 