package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.componenttest.config.WebConfig;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaEntityInserter {

    private static final ObjectMapper OBJECT_MAPPER = WebConfig.DEFAULT_OBJECT_MAPPER;

    private final JdbcClient jdbcClient;

    public <T> T insert(T entity) {
        return insert(entity, false);
    }

    public <T> T insert(T entity, boolean resetSequence) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        log.info("Inserted entity {}", entity);
        Class<?> entityClass = entity.getClass();
        validateEntityClass(entityClass);

        String tableName = extractTableName(entityClass);
        List<FieldMetadata> fields = extractFieldsMetadataForEntity(entity, entityClass);
        List<ElementCollectionMetadata> elementCollections = extractElementCollections(entityClass);
        Field idField = extractIdField(entityClass);

        String sql = buildInsertSql(tableName, fields);
        log.info("Generated SQL: {}", sql);

        Object entityId = null;
        if (idField != null) {
            entityId = getFieldValue(entity, idField);
        }

        boolean hasGeneratedId = idField != null
                && idField.isAnnotationPresent(GeneratedValue.class)
                && entityId == null;

        if (hasGeneratedId) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient.sql(sql)
                    .params(extractParameters(entity, fields))
                    .update(keyHolder);

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && !keys.isEmpty()) {
                Object generatedKey = keys.get(getIdColumnName(idField));
                setFieldValue(entity, idField, convertToFieldType(generatedKey, idField.getType()));
                entityId = generatedKey;
            }

            log.debug("Inserted entity with generated ID: {}", entityId);
        } else {
            jdbcClient.sql(sql)
                    .params(extractParameters(entity, fields))
                    .update();

            log.debug("Inserted entity with predefined ID: {}", entityId);
        }

        if (!elementCollections.isEmpty() && entityId != null) {
            insertElementCollections(entity, elementCollections, idField, entityId);
        }
        if (resetSequence && idField != null) {
            resetSequence(entityClass);
        }
        return entity;
    }

    public <T> List<T> insertAll(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(insert(entity));
        }
        if (!result.isEmpty()) {
            Class<?> entityClass = result.get(0).getClass();
            resetSequence(entityClass);
        }
        return result;
    }

    public <T> void resetSequence(Class<T> entityClass) {
        validateEntityClass(entityClass);

        String tableName = extractTableName(entityClass);
        Field idField = extractIdField(entityClass);

        if (idField == null) {
            log.warn("Entity {} has no @Id field, skipping sequence reset", entityClass.getSimpleName());
            return;
        }

        if (!idField.isAnnotationPresent(GeneratedValue.class)) {
            log.debug("Entity {} ID field is not auto-generated, skipping sequence reset", entityClass.getSimpleName());
            return;
        }

        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        if (generatedValue.strategy() != GenerationType.IDENTITY) {
            log.warn("Entity {} uses non-IDENTITY generation strategy, skipping sequence reset", entityClass.getSimpleName());
            return;
        }

        String idColumnName = getIdColumnName(idField);
        String sequenceName = tableName + "_" + idColumnName + "_seq";

        try {
            Long maxId = jdbcClient.sql(String.format("SELECT MAX(%s) FROM %s", idColumnName, tableName))
                    .query(Long.class)
                    .optional()
                    .orElse(0L);

            long nextVal = maxId + 1;

            jdbcClient.sql(String.format("SELECT setval('%s', %d, false)", sequenceName, nextVal))
                    .query()
                    .singleRow();

            log.info("Reset sequence {} to next value: {}", sequenceName, nextVal);
        } catch (Exception e) {
            log.error("Failed to reset sequence for table {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to reset sequence for table " + tableName, e);
        }
    }

    public void resetSequence(String tableName, String idColumnName) {
        Objects.requireNonNull(tableName, "Table name cannot be null");
        Objects.requireNonNull(idColumnName, "ID column name cannot be null");

        String sequenceName = tableName + "_" + idColumnName + "_seq";

        try {
            Long maxId = jdbcClient.sql(String.format("SELECT MAX(%s) FROM %s", idColumnName, tableName))
                    .query(Long.class)
                    .optional()
                    .orElse(0L);

            long nextVal = maxId + 1;

            jdbcClient.sql(String.format("SELECT setval('%s', %d, false)", sequenceName, nextVal))
                    .query()
                    .singleRow();

            log.info("Reset sequence {} to next value: {}", sequenceName, nextVal);
        } catch (Exception e) {
            log.error("Failed to reset sequence for table {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to reset sequence for table " + tableName, e);
        }
    }

    private void validateEntityClass(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(
                    "Class " + entityClass.getName() + " is not a JPA entity (@Entity annotation missing)");
        }
    }

    private String extractTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return convertCamelCaseToSnakeCase(entityClass.getSimpleName());
    }


    private List<FieldMetadata> extractFieldsMetadataForEntity(Object entity, Class<?> entityClass) {
        List<FieldMetadata> fields = new ArrayList<>();
        for (Field field : getAllFields(entityClass)) {
            if (!shouldIncludeField(field)) {
                continue;
            }

            // Primary id: only include if present (we don't insert null PKs for entities with generated ids)
            if (field.isAnnotationPresent(Id.class)) {
                Object idValue = getFieldValue(entity, field);
                if (idValue == null) {
                    continue;
                }
            }

            // Handle ManyToOne specially: include foreign key column and remember referenced id field
            if (field.isAnnotationPresent(ManyToOne.class)) {
                // determine join column name (prefer @JoinColumn, fallback to fieldName + "_id")
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                String columnName;
                if (joinColumn != null && !joinColumn.name().isEmpty()) {
                    columnName = joinColumn.name();
                } else {
                    columnName = convertCamelCaseToSnakeCase(field.getName()) + "_id";
                }

                // find id field on referenced entity
                Class<?> referencedClass = field.getType();
                Field referencedId = extractIdField(referencedClass);
                if (referencedId == null) {
                    throw new IllegalStateException("Referenced entity " + referencedClass.getName() + " has no @Id field");
                }

                fields.add(new FieldMetadata(field, columnName, referencedId));
                continue;
            }

            String columnName = getColumnName(field);
            fields.add(new FieldMetadata(field, columnName, null, isJsonColumn(field)));
        }
        return fields;
    }

    private Field extractIdField(Class<?> entityClass) {
        for (Field field : getAllFields(entityClass)) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        return null;
    }

    private boolean shouldIncludeField(Field field) {
        if (field.isAnnotationPresent(Transient.class)) {
            return false;
        }

        // include ManyToOne relationships (we need to write FK column). Other relationship types are skipped.
        if (field.isAnnotationPresent(ElementCollection.class)) {
            return false;
        }

        if (isRelationshipField(field) && !field.isAnnotationPresent(ManyToOne.class)) {
            return false;
        }

        return true;
    }

    private boolean isRelationshipField(Field field) {
        return field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToMany.class);
    }

    private String getColumnName(Field field) {
        // If there is an explicit @JoinColumn (ManyToOne), prefer it
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.name().isEmpty()) {
            return joinColumn.name();
        }

        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }

        return convertCamelCaseToSnakeCase(field.getName());
    }

    private String getIdColumnName(Field idField) {
        if (idField == null) {
            return "id";
        }
        Column columnAnnotation = idField.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return convertCamelCaseToSnakeCase(idField.getName());
    }

    private String buildInsertSql(String tableName, List<FieldMetadata> fields) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");

        StringJoiner columns = new StringJoiner(", ");
        for (FieldMetadata field : fields) {
            columns.add(field.columnName);
        }
        sql.append(columns);

        sql.append(") VALUES (");

        StringJoiner placeholders = new StringJoiner(", ");
        for (FieldMetadata field : fields) {
            placeholders.add(field.isJson ? "?::jsonb" : "?");
        }
        sql.append(placeholders);

        sql.append(")");
        return sql.toString();
    }

    private boolean isJsonColumn(Field field) {
        Column col = field.getAnnotation(Column.class);
        if (col != null && col.columnDefinition() != null
                && col.columnDefinition().toLowerCase().contains("json")) {
            return true;
        }
        return false;
    }

    private List<Object> extractParameters(Object entity, List<FieldMetadata> fields) {
        List<Object> parameters = new ArrayList<>();
        for (FieldMetadata fieldMetadata : fields) {
            if (fieldMetadata.referencedIdField != null) {
                // ManyToOne: extract referenced entity id
                Object referencedEntity = getFieldValue(entity, fieldMetadata.field);
                Object idValue = null;
                if (referencedEntity != null) {
                    idValue = getFieldValue(referencedEntity, fieldMetadata.referencedIdField);
                }
                parameters.add(convertToSqlType(idValue));
            } else {
                Object value = getFieldValue(entity, fieldMetadata.field);
                parameters.add(convertToSqlType(value));
            }
        }
        return parameters;
    }

    private Object getFieldValue(Object entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field " + field.getName(), e);
        }
    }

    private void setFieldValue(Object entity, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field " + field.getName(), e);
        }
    }

    private Object convertToSqlType(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant instant) {
            return Timestamp.from(instant);
        }

        if (value instanceof LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }

        if (value instanceof LocalDate localDate) {
            return java.sql.Date.valueOf(localDate);
        }

        if (value instanceof UUID
                || value instanceof String
                || value instanceof Long
                || value instanceof Integer
                || value instanceof BigDecimal
                || value instanceof Boolean) {
            return value;
        }

        if (value instanceof Map<?, ?> mapValue) {
            try {
                return OBJECT_MAPPER.writeValueAsString(mapValue);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize Map to JSON", e);
            }
        }

        return value.toString();
    }

    private Object convertToFieldType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }

        if (targetType == UUID.class && value instanceof String) {
            return UUID.fromString((String) value);
        }

        return value;
    }

    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    private String convertCamelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private List<ElementCollectionMetadata> extractElementCollections(Class<?> entityClass) {
        List<ElementCollectionMetadata> collections = new ArrayList<>();
        for (Field field : getAllFields(entityClass)) {
            if (field.isAnnotationPresent(ElementCollection.class)) {
                CollectionTable collectionTable = field.getAnnotation(CollectionTable.class);
                if (collectionTable == null) {
                    log.warn("ElementCollection field {} has no @CollectionTable annotation, skipping", field.getName());
                    continue;
                }

                String tableName = collectionTable.name();
                String joinColumnName = null;
                String joinColumnReferencedColumnName = null;

                if (collectionTable.joinColumns().length > 0) {
                    JoinColumn joinColumn = collectionTable.joinColumns()[0];
                    joinColumnName = joinColumn.name();
                    joinColumnReferencedColumnName = joinColumn.referencedColumnName().isEmpty()
                            ? "id"
                            : joinColumn.referencedColumnName();
                }

                Column columnAnnotation = field.getAnnotation(Column.class);
                String valueColumnName = columnAnnotation != null && !columnAnnotation.name().isEmpty()
                        ? columnAnnotation.name()
                        : convertCamelCaseToSnakeCase(field.getName());

                collections.add(new ElementCollectionMetadata(
                        field,
                        tableName,
                        joinColumnName,
                        joinColumnReferencedColumnName,
                        valueColumnName
                ));
            }
        }
        return collections;
    }

    private void insertElementCollections(Object entity, List<ElementCollectionMetadata> elementCollections,
                                          Field idField, Object entityId) {
        for (ElementCollectionMetadata metadata : elementCollections) {
            Object collectionValue = getFieldValue(entity, metadata.field);
            if (collectionValue == null) {
                continue;
            }

            if (!(collectionValue instanceof Iterable)) {
                log.warn("ElementCollection field {} is not iterable, skipping", metadata.field.getName());
                continue;
            }

            String joinColumnValue = getJoinColumnValue(entity, idField, metadata.joinColumnReferencedColumnName);
            if (joinColumnValue == null) {
                log.warn("Join column value is null for {}, skipping collection", metadata.field.getName());
                continue;
            }

            for (Object item : (Iterable<?>) collectionValue) {
                if (item != null) {
                    String sql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                            metadata.tableName,
                            metadata.joinColumnName,
                            metadata.valueColumnName);

                    jdbcClient.sql(sql)
                            .params(List.of(joinColumnValue, convertToSqlType(item)))
                            .update();

                    log.debug("Inserted element collection item into {}: {} -> {}",
                            metadata.tableName, joinColumnValue, item);
                }
            }
        }
    }

    private String getJoinColumnValue(Object entity, Field idField, String referencedColumnName) {
        if (referencedColumnName.equals("id") || referencedColumnName.equals(getIdColumnName(idField))) {
            Object idValue = getFieldValue(entity, idField);
            return idValue != null ? idValue.toString() : null;
        }

        for (Field field : getAllFields(entity.getClass())) {
            String columnName = getColumnName(field);
            if (columnName.equals(referencedColumnName)) {
                Object value = getFieldValue(entity, field);
                return value != null ? value.toString() : null;
            }
        }

        throw new IllegalStateException("Could not find field for referenced column: " + referencedColumnName);
    }

    private static class FieldMetadata {
        final Field field;
        final String columnName;
        final Field referencedIdField; // non-null for ManyToOne fields: the id field on referenced entity
        final boolean isJson;

        FieldMetadata(Field field, String columnName) {
            this(field, columnName, null, false);
        }

        FieldMetadata(Field field, String columnName, Field referencedIdField) {
            this(field, columnName, referencedIdField, false);
        }

        FieldMetadata(Field field, String columnName, Field referencedIdField, boolean isJson) {
            this.field = field;
            this.columnName = columnName;
            this.referencedIdField = referencedIdField;
            this.isJson = isJson;
        }
    }

    private static class ElementCollectionMetadata {
        final Field field;
        final String tableName;
        final String joinColumnName;
        final String joinColumnReferencedColumnName;
        final String valueColumnName;

        ElementCollectionMetadata(Field field, String tableName, String joinColumnName,
                                  String joinColumnReferencedColumnName, String valueColumnName) {
            this.field = field;
            this.tableName = tableName;
            this.joinColumnName = joinColumnName;
            this.joinColumnReferencedColumnName = joinColumnReferencedColumnName;
            this.valueColumnName = valueColumnName;
        }
    }
}

