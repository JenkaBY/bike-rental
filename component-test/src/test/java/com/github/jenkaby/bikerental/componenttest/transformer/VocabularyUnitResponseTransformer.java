package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.VocabularyUnit;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class VocabularyUnitResponseTransformer {

    @DataTableType
    public VocabularyUnit equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        return new VocabularyUnit(
                Optional.ofNullable(entry.get("id")).map(Integer::parseInt).orElse(null),
                entry.get("slug"),
                entry.get("name"),
                description
        );
    }
}
