package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.identity.web.dto.ChangePasswordRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class ChangePasswordRequestTransformer {

    @DataTableType
    public ChangePasswordRequest changePasswordRequest(Map<String, String> entry) {
        return new ChangePasswordRequest(
                DataTableHelper.getStringOrNull(entry, "currentPassword"),
                DataTableHelper.getStringOrNull(entry, "newPassword")
        );
    }
}
