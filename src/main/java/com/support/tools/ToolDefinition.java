package com.support.tools;

import java.util.Map;

/**
 * Contract for all tool implementations.
 * Each tool provides its name, description, JSON schema (for Claude),
 * and an execute method that returns a ToolResult.
 */
public interface ToolDefinition {

    /** The tool name Claude uses to invoke it (snake_case). */
    String getName();

    /** Human-readable description sent to Claude in the tool schema. */
    String getDescription();

    /**
     * The input_schema object for the Claude API tool definition.
     * Must be a valid JSON Schema object with "type", "properties", and "required" keys.
     */
    Map<String, Object> getInputSchema();

    /**
     * Executes the tool with the given input map.
     * Validates input before executing — never throws on bad input,
     * returns a ToolResult with an error message instead.
     *
     * @param input Map of parameter name → value, as parsed from Claude's tool_use block.
     */
    ToolResult execute(Map<String, Object> input);
}
