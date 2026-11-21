package com.skybook.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiPolicyDocumentStateRepository extends JpaRepository<AiPolicyDocumentState, Long> {

    Optional<AiPolicyDocumentState> findByDocumentPath(String documentPath);
}
