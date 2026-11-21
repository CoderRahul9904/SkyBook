package com.skybook.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    Optional<AiConversation> findByConversationId(String conversationId);

    List<AiConversation> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);
}
