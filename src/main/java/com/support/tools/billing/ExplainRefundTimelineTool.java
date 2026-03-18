package com.support.tools.billing;

import com.support.tools.ToolDefinition;
import com.support.tools.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * Explains the refund timeline policy based on the customer's payment method.
 * Returns policy text — no external calls needed.
 */
public class ExplainRefundTimelineTool implements ToolDefinition {

    @Override
    public String getName() { return "explain_refund_timeline"; }

    @Override
    public String getDescription() {
        return "Explains how long a refund will take based on the customer's payment method " +
               "(credit_card, bank_transfer, or paypal).";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "payment_method", Map.of(
                    "type", "string",
                    "description", "The payment method used: 'credit_card', 'bank_transfer', or 'paypal'."
                )
            ),
            "required", List.of("payment_method")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> input) {
        String method = getString(input, "payment_method");
        if (method == null || method.isEmpty()) {
            return ToolResult.error("payment_method is required.");
        }

        return switch (method.toLowerCase()) {
            case "credit_card" -> ToolResult.success(
                "Refund timeline for credit card payments: " +
                "Once the refund case is approved, the credit will appear on your statement within 5–10 business days. " +
                "The exact timing depends on your card issuer. " +
                "You will receive an email confirmation when the refund is processed."
            );
            case "bank_transfer" -> ToolResult.success(
                "Refund timeline for bank transfers: " +
                "Bank transfer refunds take 7–14 business days to process after approval. " +
                "The funds will be returned to the originating bank account. " +
                "International transfers may take up to 21 business days."
            );
            case "paypal" -> ToolResult.success(
                "Refund timeline for PayPal payments: " +
                "PayPal refunds are typically processed within 3–5 business days after approval. " +
                "The refund will appear in your PayPal balance. " +
                "If the original payment was funded by a bank account or card, it may take an additional 3–5 days."
            );
            default -> ToolResult.error(
                "Unknown payment method '" + method + "'. " +
                "Supported values are: 'credit_card', 'bank_transfer', or 'paypal'."
            );
        };
    }

    private String getString(Map<String, Object> input, String key) {
        Object val = input.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
