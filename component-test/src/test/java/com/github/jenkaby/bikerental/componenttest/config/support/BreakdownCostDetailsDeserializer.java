package com.github.jenkaby.bikerental.componenttest.config.support;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Map;

public class BreakdownCostDetailsDeserializer extends StdDeserializer<BreakdownCostDetails> {

    public BreakdownCostDetailsDeserializer() {
        super(BreakdownCostDetails.class);
    }

    @Override
    public BreakdownCostDetails deserialize(JsonParser p, DeserializationContext ctxt) {
        ObjectReadContext objectReadContext = p.objectReadContext();

        JsonNode node = objectReadContext.readTree(p);
        String pattern = node.has("breakdownPatternCode") && !node.get("breakdownPatternCode").isNull()
                ? node.get("breakdownPatternCode").asString()
                : null;
        String message = node.has("message") && !node.get("message").isNull()
                ? node.get("message").asString()
                : null;
        Map<String, Object> params = node.has("params") && !node.get("params").isNull()
                ? node.get("params").traverse(objectReadContext).readValueAs(new TypeReference<Map<String, Object>>() {
        })
                : Map.of();

        return new TestBreakdownCostDetails(pattern, message, params);
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestBreakdownCostDetails extends BreakdownCostDetails {
        public TestBreakdownCostDetails(String breakdownPatternCode, String message, Object params) {
            super(breakdownPatternCode, message, params);
        }
    }
}
