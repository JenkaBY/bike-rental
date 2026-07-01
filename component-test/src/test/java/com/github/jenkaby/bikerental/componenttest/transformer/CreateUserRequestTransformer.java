package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.RoleHelper;
import com.github.jenkaby.bikerental.users.web.dto.CreateUserRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class CreateUserRequestTransformer {

    @DataTableType
    public CreateUserRequest createUserRequest(Map<String, String> entry) {
        return new CreateUserRequest(
                DataTableHelper.getStringOrNull(entry, "username"),
                DataTableHelper.getStringOrNull(entry, "email"),
                DataTableHelper.getStringOrNull(entry, "displayName"),
                RoleHelper.parseRoles(entry.get("roles")),
                DataTableHelper.getStringOrNull(entry, "password")
        );
    }


}
