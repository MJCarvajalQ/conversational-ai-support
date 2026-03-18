package com.support.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents one block inside a Claude API message content array.
 *
 * Types:
 *   "text"        — text response from the assistant
 *   "tool_use"    — assistant requesting a tool call (has id, name, input)
 *   "tool_result" — caller returning the tool result (has tool_use_id, content)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBlock {

    private String type;

    // type = "text"
    private String text;

    // type = "tool_use"
    private String id;
    private String name;
    private Map<String, Object> input;

    // type = "tool_result"
    @JsonProperty("tool_use_id")
    private String toolUseId;

    // tool_result content is a plain string
    private String content;

    public ContentBlock() {}

    /** Factory for a tool_result block. */
    public static ContentBlock toolResult(String toolUseId, String result) {
        ContentBlock block = new ContentBlock();
        block.type = "tool_result";
        block.toolUseId = toolUseId;
        block.content = result;
        return block;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Object> getInput() { return input; }
    public void setInput(Map<String, Object> input) { this.input = input; }

    @JsonProperty("tool_use_id")
    public String getToolUseId() { return toolUseId; }

    @JsonProperty("tool_use_id")
    public void setToolUseId(String toolUseId) { this.toolUseId = toolUseId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
