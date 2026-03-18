package com.support.rag;

/**
 * A single chunk of text extracted from a documentation file.
 * Holds the raw text, its source filename, and the TF-IDF vector
 * computed at startup by EmbeddingService.
 */
public class DocumentChunk {

    private final String sourceFile;
    private final String text;
    private double[] vector; // set by EmbeddingService after vocabulary is built

    public DocumentChunk(String sourceFile, String text) {
        this.sourceFile = sourceFile;
        this.text = text;
    }

    public String getSourceFile() { return sourceFile; }
    public String getText() { return text; }

    public double[] getVector() { return vector; }
    public void setVector(double[] vector) { this.vector = vector; }
}
