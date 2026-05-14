package com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SpecConstant {

    public static final String INSTANT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public class RentalField {
        public static final String STATUS = "status";
        public static final String CUSTOMER_ID = "customerId";
        public static final String CREATED_AT = "createdAt";
        public static final String EQUIPMENT_UID = "re.equipmentUid";
        public static final String PARAM_EQUIPMENT_UID = "equipmentUid";
        public static final String PARAM_CREATED_FROM = "createdFrom";
        public static final String PARAM_CREATED_TO = "createdTo";
    }
}
