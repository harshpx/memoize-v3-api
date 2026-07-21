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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        log.info("-------------- Loading memoize knowledge base --------------");

        jdbcTemplate.execute("TRUNCATE TABLE knowledge_vector_store");

        Resource[] resources = resourcePatternResolver.getResources("classpath:knowledge/*.md");
        TokenTextSplitter tokenSplitter = TokenTextSplitter.builder().build();
        List<Document> docs = new ArrayList<>();

        for (Resource resource : resources) {
            String fileName = resource.getFilename() == null ? "" : resource.getFilename();
            String markdown = resource.getContentAsString(StandardCharsets.UTF_8);
            Document currDoc = Document.builder().text(markdown)
                    .metadata(Map.of("source", fileName, "type", "memoize-doc"))
                    .build();
            List<Document> chunks = tokenSplitter.split(currDoc);
            docs.addAll(chunks);
        }

        knowledgeVectorStore.add(docs);
        log.info("Indexed {} knowledge chunks.", docs.size());
        log.info("---------- Memoize knowledge base loading complete ----------");
    }

}
