package com.anonform.form.repository;

import com.anonform.form.domain.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormRepository extends JpaRepository<Form, UUID> {
    Optional<Form> findBySlug(String slug);
    Optional<Form> findByIdAndUser_Id(UUID id, UUID userId);
    boolean existsBySlug(String slug);
    List<Form> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);
}
