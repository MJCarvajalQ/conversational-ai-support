package com.support.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits raw Markdown text into smaller chunks suitable for TF-IDF retrieval.
 *
 * Strategy:
 *   1. Split on Markdown headings (lines starting with #) — each heading starts a new chunk.
 *   2. If a section is still too long, split further on blank lines (paragraph boundaries).
 *   3. Discard chunks below a minimum length to avoid noise.
 */
public class DocumentChunker {

    private static final int MIN_CHUNK_CHARS = 80;
    private static final int MAX_CHUNK_CHARS = 1200;

    /**
     * Produces a list of DocumentChunks from a single document's content.
     *
     * @param sourceFile The filename, included in each chunk for attribution.
     * @param content    The full Markdown text of the document.
     */
    public List<DocumentChunk> chunk(String sourceFile, String content) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // Split on heading boundaries (lines starting with #)
        String[] sections = content.split("(?m)(?=^#)");

        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.length() < MIN_CHUNK_CHARS) continue;

            if (trimmed.length() <= MAX_CHUNK_CHARS) {
                chunks.add(new DocumentChunk(sourceFile, trimmed));
            } else {
                // Section too large — split further on blank lines
                String[] paragraphs = trimmed.split("\n\\s*\n");
                StringBuilder buffer = new StringBuilder();

                for (String para : paragraphs) {
                    String p = para.trim();
                    if (p.isEmpty()) continue;

                    if (buffer.length() + p.length() > MAX_CHUNK_CHARS && buffer.length() > 0) {
                        String bufText = buffer.toString().trim();
                        if (bufText.length() >= MIN_CHUNK_CHARS) {
                            chunks.add(new DocumentChunk(sourceFile, bufText));
                        }
                        buffer = new StringBuilder();
                    }
                    buffer.append(p).append("\n\n");
                }

                String remaining = buffer.toString().trim();
                if (remaining.length() >= MIN_CHUNK_CHARS) {
                    chunks.add(new DocumentChunk(sourceFile, remaining));
                }
            }
        }

        return chunks;
    }
}
