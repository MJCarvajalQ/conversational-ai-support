package com.support.tools.billing;

import com.support.tools.ToolDefinition;
import com.support.tools.ToolResult;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simulates sending a refund request form to the customer's email address.
 * In production this would trigger an email via a transactional email service.
 */
public class SendRefundFormTool implements ToolDefinition {

    private static final Pattern CUSTOMER_ID_PATTERN =
        Pattern.compile("^[A-Z0-9\\-]{3,20}$");

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    public String getName() { return "send_refund_form"; }

    @Override
    public String getDescription() {
        return "Sends a refund request form to the customer's email address. " +
               "The customer must complete and submit the form to finalize the refund.";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "customer_id", Map.of(
                    "type", "string",
                    "description", "The customer ID (uppercase letters, digits, and hyphens, 3–20 characters)."
                ),
                "email", Map.of(
                    "type", "string",
                    "description", "The customer's email address where the refund form will be sent."
                )
            ),
            "required", List.of("customer_id", "email")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> input) {
        String customerId = getString(input, "customer_id");
        if (customerId == null || !CUSTOMER_ID_PATTERN.matcher(customerId).matches()) {
            return ToolResult.error(
                "Invalid customer_id '" + customerId + "'. " +
                "Must be 3–20 uppercase letters, digits, or hyphens."
            );
        }

        String email = getString(input, "email");
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return ToolResult.error(
                "Invalid email address '" + email + "'. Please provide a valid email."
            );
        }

        return ToolResult.success(
            "Refund form successfully sent to " + email + " for customer " + customerId + ". " +
            "The customer should receive the email within a few minutes. " +
            "The form must be submitted within 7 days to complete the refund process."
        );
    }

    private String getString(Map<String, Object> input, String key) {
        Object val = input.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
