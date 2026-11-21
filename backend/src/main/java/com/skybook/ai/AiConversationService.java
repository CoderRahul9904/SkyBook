package com.skybook.ai;

import com.skybook.auth.User;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.ForbiddenException;
import com.skybook.common.exception.UnauthorizedException;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AiConversationService {

    private final AiConversationRepository aiConversationRepository;
    private final ChatMemoryRepository chatMemoryRepository;

    public AiConversationService(AiConversationRepository aiConversationRepository, ChatMemoryRepository chatMemoryRepository) {
        this.aiConversationRepository = aiConversationRepository;
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Transactional
    public AiConversation resolveOwnedConversation(User user, String requestedConversationId) {
        Long userId = extractUserId(user);
        String conversationId = normalizeConversationId(requestedConversationId);

        if (conversationId == null) {
            AiConversation created = new AiConversation();
            created.setConversationId(UUID.randomUUID().toString());
            created.setOwnerUserId(userId);
            return aiConversationRepository.save(created);
        }

        return aiConversationRepository.findByConversationId(conversationId)
                .map(existing -> {
                    if (!existing.getOwnerUserId().equals(userId)) {
                        throw new ForbiddenException("You cannot access another customer's conversation");
                    }
                    return aiConversationRepository.save(existing);
                })
                .orElseGet(() -> {
                    AiConversation created = new AiConversation();
                    created.setConversationId(conversationId);
                    created.setOwnerUserId(userId);
                    return aiConversationRepository.save(created);
                });
    }

    @Transactional(readOnly = true)
    public List<AiConversation> listOwnedConversations(User user) {
        Long userId = extractUserId(user);
        return aiConversationRepository.findByOwnerUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public AiConversation getOwnedConversation(User user, String conversationId) {
        Long userId = extractUserId(user);
        return aiConversationRepository.findByConversationId(conversationId)
                .map(conversation -> {
                    if (!conversation.getOwnerUserId().equals(userId)) {
                        throw new ForbiddenException("You cannot access another customer's conversation");
                    }
                    return conversation;
                })
                .orElseThrow(() -> new BusinessRuleViolationException("Conversation not found"));
    }

    @Transactional(readOnly = true)
    public List<Message> getOwnedConversationMessages(User user, String conversationId) {
        AiConversation conversation = getOwnedConversation(user, conversationId);
        return chatMemoryRepository.findByConversationId(conversation.getConversationId());
    }

    @Transactional
    public void touch(AiConversation conversation) {
        conversation.touch();
        aiConversationRepository.save(conversation);
    }

    private Long extractUserId(User user) {
        if (user == null || user.getId() == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        return user.getId();
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return null;
        }
        String normalized = conversationId.trim();
        if (normalized.length() > 100) {
            throw new BusinessRuleViolationException("conversationId cannot exceed 100 characters");
        }
        return normalized;
    }
}
