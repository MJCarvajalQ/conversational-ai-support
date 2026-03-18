package com.support.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry and dispatcher for all registered tools.
 * BillingAgent calls getToolSchemas() to build the Claude API request,
 * and execute() to run whichever tool Claude selects.
 */
public class ToolExecutor {

    private final Map<String, ToolDefinition> registry = new HashMap<>();

    /** Register a tool so it can be discovered and dispatched. */
    public void register(ToolDefinition tool) {
        registry.put(tool.getName(), tool);
    }

    /**
     * Dispatches a tool call by name.
     * Returns an error ToolResult if the tool name is not recognized.
     */
    public ToolResult execute(String toolName, Map<String, Object> input) {
        ToolDefinition tool = registry.get(toolName);
        if (tool == null) {
            return ToolResult.error("Unknown tool: '" + toolName + "'");
        }
        return tool.execute(input);
    }

    /**
     * Returns the list of tool schemas formatted for the Claude API.
     * Each entry has "name", "description", and "input_schema".
     */
    public List<Map<String, Object>> getToolSchemas() {
        List<Map<String, Object>> schemas = new ArrayList<>();
        for (ToolDefinition tool : registry.values()) {
            Map<String, Object> schema = new HashMap<>();
            schema.put("name", tool.getName());
            schema.put("description", tool.getDescription());
            schema.put("input_schema", tool.getInputSchema());
            schemas.add(schema);
        }
        return schemas;
    }
}
