package com.github.jenkaby.bikerental.shared.web.advice;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    public static final String VALIDATION_ERROR = "shared.request.validation_failed";
    public static final String HANDLER_METHOD_ERROR = "shared.request.method_parameters_invalid";
    public static final String CONSTRAINT_VIOLATION = "shared.request.constraint_violation";
    public static final String METHOD_ARGUMENTS_VALIDATION_FAILED = "shared.method_arguments.validation_failed";
    public static final String METHOD_ARGUMENT_TYPE_MISMATCH = "shared.request.type_mismatch";
    public static final String REQUEST_PARAMS_MISSING = "shared.request.param_missing";
    public static final String API_VERSION_MISSING = "shared.api.version_missing";
    public static final String API_VERSION_INVALID = "shared.api.version_missing";
    public static final String INTERNAL_SERVER_ERROR = "shared.server.internal_error";
    public static final String RESOURCE_NOT_FOUND = "shared.resource.not_found";
    public static final String REFERENCE_NOT_FOUND = "shared.reference.not_found";
    public static final String RESOURCE_CONFLICT = "shared.resource.conflict";
    public static final String RESOURCE_OPTIMISTIC_LOCK = "shared.resource.optimistic_lock";
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
    public static final String OVER_BUDGET_SETTLEMENT = "finance.over_budget_settlement";
    public static final String INSUFFICIENT_HOLD = "finance.insufficient_hold";
}
