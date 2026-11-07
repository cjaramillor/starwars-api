package com.stw.repository;

import com.stw.entities.StarshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StarshipRepository extends JpaRepository<StarshipEntity, Long> {
}
