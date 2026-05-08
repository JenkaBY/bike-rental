package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;

public class TimeTravelResponseTransformer {

    @DataTableType
    public TimeTravelController.TimeResponse timeResponse(Map<String, String> entry) {
        return new TimeTravelController.TimeResponse(Instant.parse(entry.get("instant")));
    }
}
