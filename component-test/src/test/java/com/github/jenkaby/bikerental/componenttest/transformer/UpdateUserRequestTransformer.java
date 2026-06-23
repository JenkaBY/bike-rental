package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.RoleHelper;
import com.github.jenkaby.bikerental.identity.domain.model.UserStatus;
import com.github.jenkaby.bikerental.identity.web.dto.UpdateUserRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class UpdateUserRequestTransformer {

    @DataTableType
    public UpdateUserRequest updateUserRequest(Map<String, String> entry) {
        var status = DataTableHelper.getStringOrNull(entry, "status");
        return new UpdateUserRequest(
                DataTableHelper.getStringOrNull(entry, "displayName"),
                RoleHelper.parseRoles(entry.get("roles")),
                status != null ? UserStatus.valueOf(status) : null
        );
    }
}
