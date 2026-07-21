package com.memoize.api.Service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface RagService {
    List<Document> searchDocsFromKnowledgeStore(String query);
    boolean requireKnowledgeStore(String query);
}
