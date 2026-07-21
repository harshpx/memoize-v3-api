package com.memoize.api.Service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagServiceImpl implements RagService {
    private final VectorStore knowledgeVectorStore;

    public RagServiceImpl(@Qualifier("knowledge-vector-store") VectorStore knowledgeVectorStore) {
        this.knowledgeVectorStore = knowledgeVectorStore;
    }

    @Override
    public List<Document> searchDocsFromKnowledgeStore(String query) {
        return knowledgeVectorStore.similaritySearch(
          SearchRequest.builder()
                  .query(query)
                  .similarityThreshold(0.65)
                  .topK(3)
                  .build()
        );
    }

    @Override
    public boolean requireKnowledgeStore(String query) {
        return !searchDocsFromKnowledgeStore(query).isEmpty();
    }
}
