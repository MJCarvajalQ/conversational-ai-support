package com.support.rag;

import com.support.config.AppConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * In-memory store of embedded DocumentChunks.
 * Supports top-k retrieval by cosine similarity against a query vector.
 */
public class VectorStore {

    private static final Logger logger = Logger.getLogger(VectorStore.class.getName());

    private final List<DocumentChunk> chunks = new ArrayList<>();
    private final EmbeddingService embeddingService;

    public VectorStore(EmbeddingService embeddingService) {
        this.embeddingService = Objects.requireNonNull(embeddingService, "embeddingService must not be null");
    }

    public void addChunk(DocumentChunk chunk) {
        chunks.add(chunk);
    }

    /**
     * Computes TF-IDF vectors for all chunks and stores them.
     * Replaces the separate embedAll + addChunk loop in Main.
     */
    public void addAll(List<DocumentChunk> allChunks) {
        for (DocumentChunk chunk : allChunks) {
            chunk.setVector(computeVector(chunk.getText()));
            chunks.add(chunk);
        }
        logger.info("VectorStore loaded " + chunks.size() + " chunks.");
    }

    /**
     * Returns the top-k most relevant chunks for the given query text.
     * Uses cosine similarity on TF-IDF vectors.
     */
    public List<DocumentChunk> search(String query) {
        double[] queryVector = computeVector(query);
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

    /**
     * Computes a TF-IDF vector for the given text using the vocabulary built by EmbeddingService.
     */
    private double[] computeVector(String text) {
        Map<String, Integer> termIndex = embeddingService.getTermIndex();
        double[] idfWeights = embeddingService.getIdfWeights();
        int vocabSize = embeddingService.getVocabSize();

        List<String> tokens = embeddingService.tokenize(text);
        double[] vector = new double[vocabSize];

        java.util.Map<String, Integer> tf = new java.util.HashMap<>();
        for (String token : tokens) {
            tf.merge(token, 1, Integer::sum);
        }

        int totalTokens = Math.max(tokens.size(), 1);
        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            Integer idx = termIndex.get(entry.getKey());
            if (idx != null) {
                double termFreq = (double) entry.getValue() / totalTokens;
                vector[idx] = termFreq * idfWeights[idx];
            }
        }
        return vector;
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
