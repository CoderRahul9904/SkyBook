package com.skybook.ai;

import com.skybook.auth.User;
import com.skybook.common.exception.ForbiddenException;
import com.skybook.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiConversationServiceTest {

    private AiConversationRepository conversationRepository;
    private ChatMemoryRepository chatMemoryRepository;
    private AiConversationService service;

    @BeforeEach
    void setUp() {
        conversationRepository = mock(AiConversationRepository.class);
        chatMemoryRepository = mock(ChatMemoryRepository.class);
        service = new AiConversationService(conversationRepository, chatMemoryRepository);
    }

    @Test
    void resolveOwnedConversationCreatesNewWhenConversationIdMissing() {
        User user = user(10L);
        when(conversationRepository.save(any(AiConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiConversation created = service.resolveOwnedConversation(user, null);

        assertThat(created.getConversationId()).isNotBlank();
        assertThat(created.getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void resolveOwnedConversationRejectsOtherUsersConversation() {
        User user = user(10L);
        AiConversation existing = new AiConversation();
        existing.setConversationId("conv-1");
        existing.setOwnerUserId(99L);
        when(conversationRepository.findByConversationId("conv-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.resolveOwnedConversation(user, "conv-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void listOwnedConversationRequiresAuthentication() {
        assertThatThrownBy(() -> service.listOwnedConversations(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getOwnedConversationMessagesReturnsStoredMessages() {
        User user = user(10L);

        AiConversation existing = new AiConversation();
        existing.setConversationId("conv-1");
        existing.setOwnerUserId(10L);

        Message message = mock(Message.class);
        when(conversationRepository.findByConversationId("conv-1")).thenReturn(Optional.of(existing));
        when(chatMemoryRepository.findByConversationId("conv-1")).thenReturn(List.of(message));

        List<Message> messages = service.getOwnedConversationMessages(user, "conv-1");

        assertThat(messages).hasSize(1);
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
