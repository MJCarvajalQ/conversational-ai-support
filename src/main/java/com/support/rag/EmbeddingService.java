package com.support.rag;

import java.util.*;

/**
 * TF-IDF embedding service.
 *
 * Workflow:
 *   1. buildVocabulary(chunks)  — scans all chunks, builds term→index map + IDF weights.
 *   2. embedAll(chunks)         — computes and stores a TF-IDF vector on each chunk.
 *   3. embedQuery(text)         — produces a TF-IDF vector for a user query at runtime.
 *   4. cosineSimilarity(a, b)   — measures relevance between query and chunk vectors.
 *
 * Limitation: TF-IDF relies on exact keyword overlap. "authentication errors" and
 * "login failures" are not considered similar unless both terms appear in the same chunk.
 * A semantic embedding model (e.g. text-embedding-ada-002) would handle synonyms correctly
 * and is recommended for a production environment.
 */
public class EmbeddingService {

    private Map<String, Integer> termIndex; // term → position in vector
    private double[] idfWeights;            // IDF weight per term
    private int vocabSize;

    /**
     * Step 1: Build vocabulary and IDF weights from all chunks.
     * Must be called before embedAll or embedQuery.
     */
    public void buildVocabulary(List<DocumentChunk> chunks) {
        int totalDocs = chunks.size();

        // Count how many chunks contain each term (document frequency)
        Map<String, Integer> docFrequency = new HashMap<>();
        for (DocumentChunk chunk : chunks) {
            Set<String> uniqueTerms = new HashSet<>(tokenize(chunk.getText()));
            for (String term : uniqueTerms) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }

        // Build term→index map (sorted for determinism)
        List<String> sortedTerms = new ArrayList<>(docFrequency.keySet());
        Collections.sort(sortedTerms);

        termIndex = new HashMap<>();
        for (int i = 0; i < sortedTerms.size(); i++) {
            termIndex.put(sortedTerms.get(i), i);
        }
        vocabSize = sortedTerms.size();

        // Compute IDF: log((N + 1) / (df + 1)) + 1  (smoothed to avoid division by zero)
        idfWeights = new double[vocabSize];
        for (String term : sortedTerms) {
            int idx = termIndex.get(term);
            int df = docFrequency.get(term);
            idfWeights[idx] = Math.log((double)(totalDocs + 1) / (df + 1)) + 1.0;
        }

        System.out.println("[RAG] Vocabulary built: " + vocabSize + " unique terms across "
            + totalDocs + " chunks.");
    }

    /**
     * Step 2: Compute and assign TF-IDF vectors to all chunks.
     */
    public void embedAll(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            chunk.setVector(computeVector(chunk.getText()));
        }
    }

    /**
     * Step 3: Compute a TF-IDF vector for a user query string.
     */
    public double[] embedQuery(String query) {
        return computeVector(query);
    }

    /**
     * Cosine similarity between two vectors. Returns 0 if either vector is zero.
     */
    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, magA = 0.0, magB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        if (magA == 0.0 || magB == 0.0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    // --- private helpers ---

    private double[] computeVector(String text) {
        List<String> tokens = tokenize(text);
        double[] vector = new double[vocabSize];

        // Count raw term frequency
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.merge(token, 1, Integer::sum);
        }

        // TF-IDF = (count / total_tokens) * IDF
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

    /** Lowercase + split on non-alphanumeric characters. Filters single-char tokens. */
    private List<String> tokenize(String text) {
        String[] rawTokens = text.toLowerCase().split("[^a-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (t.length() > 1) tokens.add(t);
        }
        return tokens;
    }
}
