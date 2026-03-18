package com.support.tools.billing;

import com.support.tools.ToolDefinition;
import com.support.tools.ToolResult;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Returns the customer's recent billing history (last 3 invoices).
 * Data is mocked — in production this would query the billing database.
 */
public class GetBillingHistoryTool implements ToolDefinition {

    private static final Pattern CUSTOMER_ID_PATTERN =
        Pattern.compile("^[A-Z0-9\\-]{3,20}$");

    @Override
    public String getName() { return "get_billing_history"; }

    @Override
    public String getDescription() {
        return "Retrieves the customer's recent billing history, including invoice dates, amounts, and payment status.";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "customer_id", Map.of(
                    "type", "string",
                    "description", "The customer ID (uppercase letters, digits, and hyphens, 3–20 characters)."
                )
            ),
            "required", List.of("customer_id")
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

        // Mocked billing history — 3 recent invoices
        return ToolResult.success(
            "Billing history for customer " + customerId + " (last 3 invoices):\n" +
            "1. INV-2026-0301 | 2026-03-01 | $99.00 | Paid\n" +
            "2. INV-2026-0201 | 2026-02-01 | $99.00 | Paid\n" +
            "3. INV-2026-0101 | 2026-01-01 | $99.00 | Paid\n" +
            "Note: Invoice INV-2026-0201 and INV-2026-0301 show two charges in February and March respectively. " +
            "If you believe a charge is incorrect, a refund case can be opened."
        );
    }

    private String getString(Map<String, Object> input, String key) {
        Object val = input.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
