package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.VocabularyUnit;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class VocabularyUnitResponseTransformer {

    @DataTableType
    public VocabularyUnit equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        return new VocabularyUnit(
                entry.get("slug"),
                entry.get("name"),
                description
        );
    }
}
