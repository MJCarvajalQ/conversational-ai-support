package com.support.rag;

import com.support.config.AppConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory store of embedded DocumentChunks.
 * Supports top-k retrieval by cosine similarity against a query vector.
 */
public class VectorStore {

    private final List<DocumentChunk> chunks = new ArrayList<>();
    private final EmbeddingService embeddingService;

    public VectorStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public void addChunk(DocumentChunk chunk) {
        chunks.add(chunk);
    }

    /**
     * Returns the top-k most relevant chunks for the given query text.
     * Uses cosine similarity on TF-IDF vectors.
     */
    public List<DocumentChunk> search(String query) {
        double[] queryVector = embeddingService.embedQuery(query);
        int topK = AppConfig.TOP_K_CHUNKS;

        return chunks.stream()
            .filter(c -> c.getVector() != null)
            .map(c -> new ScoredChunk(c, embeddingService.cosineSimilarity(queryVector, c.getVector())))
            .filter(sc -> sc.score > 0.0)
            .sorted(Comparator.comparingDouble((ScoredChunk sc) -> sc.score).reversed())
            .limit(topK)
            .map(sc -> sc.chunk)
            .collect(java.util.stream.Collectors.toList());
    }

    public int size() {
        return chunks.size();
    }

    private static class ScoredChunk {
        final DocumentChunk chunk;
        final double score;

        ScoredChunk(DocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
