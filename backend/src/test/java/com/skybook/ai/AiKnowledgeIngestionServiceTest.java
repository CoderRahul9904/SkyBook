package com.skybook.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiKnowledgeIngestionServiceTest {

    @Test
    void ingestionAddsDocumentsWhenVectorStoreAvailable() {
        AiPolicyDocumentStateRepository stateRepository = mock(AiPolicyDocumentStateRepository.class);
        VectorStore vectorStore = mock(VectorStore.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);

        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStore);
        when(stateRepository.findByDocumentPath(anyString())).thenReturn(Optional.empty());
        when(stateRepository.save(any(AiPolicyDocumentState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiKnowledgeIngestionService ingestionService = new AiKnowledgeIngestionService(stateRepository, vectorStoreProvider);
        ingestionService.ingestPolicyDocuments();

        verify(vectorStore, atLeastOnce()).add(any());
    }
}
