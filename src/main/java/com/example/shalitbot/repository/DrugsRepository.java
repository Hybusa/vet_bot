package com.example.shalitbot.repository;

import com.example.shalitbot.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugsRepository extends JpaRepository<Drug, Long> {
    Optional<Drug> findByNameLat(String name);
    List<Drug> findAllByTypeGroupIgnoreCase(String typeGroup);
    @Query(value = "SELECT type_group FROM drugs",nativeQuery = true)
    List<String> getTypeGroups();



 }
