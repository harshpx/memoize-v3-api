package com.memoize.api.Startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AppKnowledgeLoader {
    private final ResourcePatternResolver resourcePatternResolver;
    private final JdbcTemplate jdbcTemplate;
    private final VectorStore knowledgeVectorStore;

    public AppKnowledgeLoader(
        ResourcePatternResolver resourcePatternResolver,
        JdbcTemplate jdbcTemplate,
        @Qualifier("knowledge-vector-store") VectorStore knowledgeVectorStore
    ) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.jdbcTemplate = jdbcTemplate;
        this.knowledgeVectorStore = knowledgeVectorStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadKnowledgeBase() throws IOException {
        log.info("Loading memoize knowledge base .....");

        Resource[] resources = resourcePatternResolver.getResources("classpath:knowledge/*.md");
        TokenTextSplitter tokenSplitter = TokenTextSplitter.builder().build();

        Set<String> currentFiles = new HashSet<>();

        List<Document> docs = new ArrayList<>();

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) continue;

            currentFiles.add(fileName);

            String markdown = resource.getContentAsString(StandardCharsets.UTF_8);

            String hash = sha256(markdown);
            if (hash == null) continue;

            String existingHash = getStoredHash(fileName);
            if (existingHash != null && existingHash.equals(hash)) {
                log.info("Skipping {}, unchanged.", fileName);
                continue;
            }

            log.info("Indexing {}", fileName);

            deleteEmbeddings(fileName);

            Document currDoc = Document.builder().text(markdown)
                    .metadata(Map.of("source", fileName, "type", "memoize-doc", "hash", hash))
                    .build();
            List<Document> chunks = tokenSplitter.split(currDoc);
            docs.addAll(chunks);
        }
        knowledgeVectorStore.add(docs);
        removeDeletedFiles(currentFiles);
        log.info("Indexed {} knowledge chunks.", docs.size());
        log.info("Memoize knowledge base loading complete!");
    }

    // helpers
    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private String getStoredHash(String source) {
        var hashes = jdbcTemplate.query(
        """
                SELECT metadata->>'hash'
                from knowledge_vector_store
                where metadata->>'source'=?
            """,
            (rs, rowNum) -> rs.getString(1),
            source
        );
        return hashes.isEmpty() ? null : hashes.getFirst();
    }

    private void deleteEmbeddings(String source) {
        jdbcTemplate.update(
        "DELETE FROM knowledge_vector_store WHERE metadata->>'source'=?",
            source
        );
    }

    private void removeDeletedFiles(Set<String> currentFiles) {
        List<String> storedFiles = jdbcTemplate.query("""
                SELECT DISTINCT metadata->>'source'
                FROM knowledge_vector_store
                """,
                (rs, rowNum) -> rs.getString(1));

        List<String> deletedFiles = new ArrayList<>();

        for (String file : storedFiles) {
            if (!currentFiles.contains(file)) {
                deletedFiles.add(file);
            }
        }

        if (deletedFiles.isEmpty()) {
            return;
        }

        String placeholders = deletedFiles.stream().map(file -> "?").collect(Collectors.joining(","));

        String sql = """
            DELETE FROM knowledge_vector_store
            WHERE metadata->>'source' IN (%s)
        """.formatted(placeholders);

        jdbcTemplate.update(sql, deletedFiles.toArray());
    }
}
