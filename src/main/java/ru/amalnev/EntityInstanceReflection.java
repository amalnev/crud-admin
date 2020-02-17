package ru.amalnev;

import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntityInstanceReflection extends ArrayList<EntityFieldReflection> {
    public EntityInstanceReflection(Object entityInstance) {
        getFields(entityInstance.getClass()).forEach(field -> add(new EntityFieldReflection(entityInstance, field)));
    }

    public static List<EntityInstanceReflection> getAllInstances(CrudRepository repository) {
        List<EntityInstanceReflection> entityInstanceReflections = new ArrayList<>();
        repository.findAll().forEach(entityInstance -> entityInstanceReflections.add(new EntityInstanceReflection(entityInstance)));
        return entityInstanceReflections;
    }

    private static List<Field> getFields(List<Field> fields, Class<?> type) {
        fields.addAll(
                Arrays.stream(type.getDeclaredFields())
                      .filter(field -> !Modifier.isStatic(field.getModifiers()))
                      .collect(Collectors.toList()));

        if (type.getSuperclass() != null) {
            getFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private static List<Field> getFields(Class<?> type) {
        final List<Field> fields = new ArrayList<>();
        return getFields(fields, type);
    }

    public List<String> getFieldNames() {
        return stream().map(EntityFieldReflection::getName).collect(Collectors.toList());
    }

    public List<String> getFieldValues() {
        return stream()
                .map(fieldReflection -> fieldReflection.getValue() == null ? "null" : fieldReflection.getValue())
                .collect(Collectors.toList());
    }
}
