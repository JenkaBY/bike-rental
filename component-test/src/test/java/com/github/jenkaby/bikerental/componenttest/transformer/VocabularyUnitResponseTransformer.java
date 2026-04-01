package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.DictionaryUnit;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class VocabularyUnitResponseTransformer {

    @DataTableType
    public DictionaryUnit transform(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        return new DictionaryUnit(
                Optional.ofNullable(entry.get("id")).map(Integer::parseInt).orElse(null),
                entry.get("slug"),
                entry.get("name"),
                description
        );
    }
}
