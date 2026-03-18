package com.support.tools.billing;

import com.support.tools.ToolDefinition;
import com.support.tools.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * Returns the customer's current subscription plan, price, and renewal date.
 * Data is mocked — in production this would query a billing database.
 */
public class GetCustomerPlanTool implements ToolDefinition {

    private static final java.util.regex.Pattern CUSTOMER_ID_PATTERN =
        java.util.regex.Pattern.compile("^[A-Z0-9\\-]{3,20}$");

    @Override
    public String getName() { return "get_customer_plan"; }

    @Override
    public String getDescription() {
        return "Retrieves the customer's current subscription plan, monthly price, and next renewal date.";
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

        // Mocked response — deterministic based on customer ID suffix
        String plan, price, renewal;
        if (customerId.endsWith("1") || customerId.endsWith("A")) {
            plan = "Starter Plan"; price = "$29/month"; renewal = "2026-04-01";
        } else if (customerId.endsWith("2") || customerId.endsWith("B")) {
            plan = "Pro Plan"; price = "$99/month"; renewal = "2026-04-15";
        } else {
            plan = "Enterprise Plan"; price = "$499/month"; renewal = "2026-05-01";
        }

        return ToolResult.success(
            "Customer " + customerId + " is currently on the " + plan + " at " + price + ". " +
            "The next renewal date is " + renewal + "."
        );
    }

    private String getString(Map<String, Object> input, String key) {
        Object val = input.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
