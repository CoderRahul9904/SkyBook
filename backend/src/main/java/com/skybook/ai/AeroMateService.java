package com.skybook.ai;

import com.skybook.ai.dto.AiChatRequest;
import com.skybook.ai.dto.AiChatResponse;
import com.skybook.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AeroMateService {

    private static final Logger log = LoggerFactory.getLogger(AeroMateService.class);

    private static final String CHAT_MEMORY_CONVERSATION_ID = "chat_memory_conversation_id";

    private static final String SYSTEM_PROMPT = """
            You are AeroMate, SkyBook's airline assistant.
            Follow these rules:
            - Prefer grounded answers from retrieved policy context when available.
            - If policy information is missing, clearly say the policy knowledge base does not contain that answer.
            - Use tools only for read-only operations.
            - Never claim that you booked, cancelled, refunded, or modified data.
            """;

    private final ChatClient chatClient;
    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    private final ObjectProvider<RetrievalAugmentationAdvisor> ragAdvisorProvider;
    private final AeroMateTools aeroMateTools;
    private final AiConversationService aiConversationService;

    public AeroMateService(ChatClient chatClient,
                           MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                           ObjectProvider<RetrievalAugmentationAdvisor> ragAdvisorProvider,
                           AeroMateTools aeroMateTools,
                           AiConversationService aiConversationService) {
        this.chatClient = chatClient;
        this.messageChatMemoryAdvisor = messageChatMemoryAdvisor;
        this.ragAdvisorProvider = ragAdvisorProvider;
        this.aeroMateTools = aeroMateTools;
        this.aiConversationService = aiConversationService;
    }

    @Transactional
    public AiChatResponse chat(User user, AiChatRequest request) {
        AiConversation conversation = aiConversationService.resolveOwnedConversation(user, request.conversationId());
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(messageChatMemoryAdvisor);

        RetrievalAugmentationAdvisor ragAdvisor = ragAdvisorProvider.getIfAvailable();
        boolean ragApplied = false;
        if (ragAdvisor != null) {
            advisors.add(ragAdvisor);
            ragApplied = true;
        }

        String response;
        try {
            response = invokeAssistant(request.message(), conversation.getConversationId(), advisors);
        }
        catch (Exception ex) {
            if (!ragApplied) {
                throw ex;
            }
            log.warn("RAG-assisted call failed, retrying without RAG advisor", ex);
            response = invokeAssistant(request.message(), conversation.getConversationId(), List.of(messageChatMemoryAdvisor));
            ragApplied = false;
        }

        aiConversationService.touch(conversation);
        return new AiChatResponse(conversation.getConversationId(), response, ragApplied);
    }

    private String invokeAssistant(String userMessage, String conversationId, List<Advisor> advisors) {
        ChatClientRequestSpec promptSpec = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .advisors(advisors)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID, conversationId))
                .toolCallbacks(ToolCallbacks.from(aeroMateTools));

        return promptSpec.call().content();
    }
}
