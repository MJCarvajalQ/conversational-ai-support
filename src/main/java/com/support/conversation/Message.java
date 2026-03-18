package com.support.conversation;

/**
 * Represents a single message in the conversation.
 * Content can be a plain String (for simple user/assistant turns)
 * or a List of content-block maps (for tool_use / tool_result turns).
 */
public class Message {

    private String role;
    private Object content; // String or List<Map<String, Object>>

    public Message() {}

    public Message(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    /** Factory for a simple user text message. */
    public static Message user(String text) {
        return new Message("user", text);
    }

    /** Factory for a simple assistant text message. */
    public static Message assistant(String text) {
        return new Message("assistant", text);
    }

    public String getRole() { return role; }
    public Object getContent() { return content; }
    public void setRole(String role) { this.role = role; }
    public void setContent(Object content) { this.content = content; }
}
