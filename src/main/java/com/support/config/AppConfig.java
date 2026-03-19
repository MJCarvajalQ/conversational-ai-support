package com.support.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    public static final String DOCS_DIRECTORY = "docs";
    public static final String CLAUDE_MODEL = "claude-sonnet-4-6";
    public static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    public static final int MAX_HISTORY_TURNS = 10;
    public static final int MAX_INPUT_CHARS = 2000;
    public static final int MAX_TOOL_ITERATIONS = 5;
    public static final int TOP_K_CHUNKS = 3;
    public static final double MAX_REFUND_AMOUNT = 1000.0;
    public static final int AGENT_MAX_TOKENS = 1024;

    public static final String CLAUDE_API_KEY;
    public static final String ANTHROPIC_VERSION;

    static {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(
                "Could not load config.properties. Copy config.properties.example to config.properties and fill in your API key.",
                e
            );
        }
        String key = props.getProperty("anthropic.api.key", "").trim();
        if (key.isEmpty() || key.equals("YOUR_ANTHROPIC_API_KEY")) {
            throw new RuntimeException("anthropic.api.key is not set in config.properties");
        }
        CLAUDE_API_KEY = key;
        String version = props.getProperty("anthropic.version", "").trim();
        if (version.isEmpty()) {
            throw new RuntimeException("anthropic.version is not set in config.properties");
        }
        ANTHROPIC_VERSION = version;
    }

    private AppConfig() {}
}
