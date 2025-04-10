package com.alaikis.paymenthub.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DynamicSetter {
    public static void setProperty(Object obj, String propertyName, Object value) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            String setterName = "set" + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);

            MethodHandle setter = lookup.findVirtual(obj.getClass(), setterName,
                    MethodType.methodType(void.class, value.getClass()));

            setter.invoke(obj, value);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
