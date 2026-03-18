package com.support.tools.billing;

import com.support.config.AppConfig;
import com.support.tools.ToolDefinition;
import com.support.tools.ToolResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Opens a refund case for a customer.
 * Returns a case ID and estimated resolution timeframe.
 * Data is mocked — in production this would create a record in a CRM or ticketing system.
 */
public class OpenRefundCaseTool implements ToolDefinition {

    private static final java.util.regex.Pattern CUSTOMER_ID_PATTERN =
        java.util.regex.Pattern.compile("^[A-Z0-9\\-]{3,20}$");

    @Override
    public String getName() { return "open_refund_case"; }

    @Override
    public String getDescription() {
        return "Opens a refund case for the customer. Returns a case ID and estimated resolution time. " +
               "Maximum refund amount is $" + (int) AppConfig.MAX_REFUND_AMOUNT + ".";
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
                "amount", Map.of(
                    "type", "number",
                    "description", "The refund amount in USD. Must be greater than 0 and at most $" +
                        (int) AppConfig.MAX_REFUND_AMOUNT + "."
                ),
                "reason", Map.of(
                    "type", "string",
                    "description", "Brief description of the reason for the refund."
                )
            ),
            "required", List.of("customer_id", "amount", "reason")
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

        double amount;
        try {
            amount = Double.parseDouble(input.get("amount").toString());
        } catch (Exception e) {
            return ToolResult.error("Invalid amount. Must be a numeric value.");
        }
        if (amount <= 0) {
            return ToolResult.error("Refund amount must be greater than $0.");
        }
        if (amount > AppConfig.MAX_REFUND_AMOUNT) {
            return ToolResult.error(
                "Refund amount $" + amount + " exceeds the maximum allowed amount of $" +
                (int) AppConfig.MAX_REFUND_AMOUNT + ". Please contact your account manager."
            );
        }

        String reason = getString(input, "reason");
        if (reason == null || reason.isEmpty()) {
            return ToolResult.error("A reason for the refund is required.");
        }

        // Generate a mocked case ID
        String caseId = "RF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return ToolResult.success(
            "Refund case " + caseId + " has been opened for customer " + customerId + ". " +
            "Refund amount: $" + String.format("%.2f", amount) + ". " +
            "Reason: " + reason + ". " +
            "Estimated resolution: 3–5 business days."
        );
    }

    private String getString(Map<String, Object> input, String key) {
        Object val = input.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
