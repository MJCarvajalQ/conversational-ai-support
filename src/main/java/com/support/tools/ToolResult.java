package com.support.tools;

/**
 * Wraps the outcome of a tool execution.
 * Always returned — never throw from a tool's execute() method.
 */
public class ToolResult {

    private final String content;
    private final boolean error;

    private ToolResult(String content, boolean error) {
        this.content = content;
        this.error = error;
    }

    public static ToolResult success(String content) {
        return new ToolResult(content, false);
    }

    public static ToolResult error(String message) {
        return new ToolResult("Error: " + message, true);
    }

    public String getContent() { return content; }
    public boolean isError() { return error; }

    @Override
    public String toString() { return content; }
}
