package ru.amalnev;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
public class EntityFieldReflection {

    private String name;

    private String value;

    @SneakyThrows
    public EntityFieldReflection(Object entityInstance, Field field) {
        name = field.getName();
        Object fieldValue = runGetter(entityInstance, field);
        try {
            value = fieldValue == null ? "" : fieldValue.toString();
        }
        catch (Throwable e) {
            value = "";
        }
    }

    @Override
    public String toString() {
        return value;
    }

    private Object runGetter(Object target, Field field) {
        for (Method method : target.getClass().getMethods()) {
            if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    try {
                        return method.invoke(target);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

}
