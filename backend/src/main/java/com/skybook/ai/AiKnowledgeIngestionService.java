package com.skybook.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class AiKnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeIngestionService.class);

    private final AiPolicyDocumentStateRepository documentStateRepository;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public AiKnowledgeIngestionService(AiPolicyDocumentStateRepository documentStateRepository,
                                       ObjectProvider<VectorStore> vectorStoreProvider) {
        this.documentStateRepository = documentStateRepository;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ingestPolicyDocuments() {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.warn("VectorStore bean not available; skipping policy ingestion");
            return;
        }

        Resource[] resources;
        try {
            resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:airline-policies/*.md");
        }
        catch (IOException ex) {
            log.error("Unable to resolve policy documents", ex);
            return;
        }

        if (resources.length == 0) {
            log.warn("No policy markdown files found under classpath:airline-policies/");
            return;
        }

        List<Document> toIngest = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                String path = resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
                String checksum = sha256(resource);
                boolean skip = documentStateRepository.findByDocumentPath(path)
                        .map(existing -> existing.getChecksum().equals(checksum))
                        .orElse(false);

                if (skip) {
                    continue;
                }

                List<Document> documents = new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig()).get();
                documents.forEach(document -> {
                    document.getMetadata().put("source", path);
                    document.getMetadata().put("category", "skybook-policy");
                });

                List<Document> chunks = new TokenTextSplitter().apply(documents);
                toIngest.addAll(chunks);

                AiPolicyDocumentState state = documentStateRepository.findByDocumentPath(path)
                        .orElseGet(AiPolicyDocumentState::new);
                state.setDocumentPath(path);
                state.setChecksum(checksum);
                documentStateRepository.save(state);
            }
            catch (Exception ex) {
                log.warn("Failed to process policy document: {}", resource.getDescription(), ex);
            }
        }

        if (toIngest.isEmpty()) {
            log.info("Policy ingestion skipped; no new or changed documents");
            return;
        }

        try {
            vectorStore.add(toIngest);
            log.info("Ingested {} policy chunks into vector store", toIngest.size());
        }
        catch (Exception ex) {
            log.warn("Vector ingestion failed; RAG will gracefully fallback without retrieved context", ex);
        }
    }

    private String sha256(Resource resource) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = resource.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
