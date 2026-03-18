package com.support.rag;

import com.support.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads all Markdown files from the docs/ directory.
 * Includes path traversal protection: every resolved path is verified
 * to be inside the docs directory before reading.
 */
public class DocumentLoader {

    private final Path docsBase;

    public DocumentLoader() throws IOException {
        this.docsBase = Path.of(AppConfig.DOCS_DIRECTORY).toRealPath();
    }

    /**
     * Reads all .md files in the docs directory and returns their raw text.
     * Each entry is a pair of (filename, content).
     */
    public List<String[]> loadAll() throws IOException {
        List<String[]> results = new ArrayList<>();

        try (Stream<Path> files = Files.list(docsBase)) {
            files.filter(p -> p.toString().endsWith(".md"))
                 .sorted()
                 .forEach(filePath -> {
                     try {
                         Path resolved = filePath.toRealPath();
                         // Path traversal check
                         if (!resolved.startsWith(docsBase)) {
                             throw new SecurityException(
                                 "Path traversal attempt blocked: " + filePath
                             );
                         }
                         String content = Files.readString(resolved);
                         results.add(new String[]{resolved.getFileName().toString(), content});
                     } catch (IOException e) {
                         throw new RuntimeException("Failed to read doc file: " + filePath, e);
                     }
                 });
        }

        if (results.isEmpty()) {
            throw new RuntimeException(
                "No .md files found in '" + AppConfig.DOCS_DIRECTORY + "'. " +
                "Please add documentation files before starting."
            );
        }

        System.out.println("[RAG] Loaded " + results.size() + " documentation file(s).");
        return results;
    }
}
