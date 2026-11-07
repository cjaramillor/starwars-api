package com.stw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.stw.entities.CharacterEntity;

public interface CharacterRepository extends JpaRepository<CharacterEntity,Long>, JpaSpecificationExecutor<CharacterEntity> {

}
