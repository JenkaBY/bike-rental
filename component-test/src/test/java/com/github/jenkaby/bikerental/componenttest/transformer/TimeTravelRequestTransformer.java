package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;

public class TimeTravelRequestTransformer {

    @DataTableType
    public TimeTravelController.SetTimeRequest setTimeRequest(Map<String, String> entry) {
        return new TimeTravelController.SetTimeRequest(Instant.parse(entry.get("instant")));
    }
}
