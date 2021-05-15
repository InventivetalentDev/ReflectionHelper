package org.inventivetalent.reflection.accessor;

import org.inventivetalent.reflection.util.AccessUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class FieldAccessor {

    private final Field field;

    public FieldAccessor(Field field) {
        this.field = field;
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            if (AccessUtil.VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    public <T> T get(Object obj) {
        try {
            //noinspection unchecked
            return (T) field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void set(Object obj, T value) {
        setField(obj, value, field);
    }

    /*
     * https://github.com/powermock/powermock/blob/42c72daf9d8b04129178d1d3f1fb4e485d3c13dc/powermock-reflect/src/main/java/org/powermock/reflect/internal/WhiteboxImpl.java#L2298-L2403
     */

    private static void setField(Object object, Object value, Field foundField) {
        boolean isStatic = (foundField.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
        if (isStatic) {
            setStaticFieldUsingUnsafe(foundField, value);
        } else {
            setFieldUsingUnsafe(foundField, object, value);
        }
    }

    private static void setStaticFieldUsingUnsafe(final Field field, final Object newValue) {
        try {
            field.setAccessible(true);
            int fieldModifiersMask = field.getModifiers();
            boolean isFinalModifierPresent = (fieldModifiersMask & Modifier.FINAL) == Modifier.FINAL;
            if (isFinalModifierPresent) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        Unsafe unsafe = getUnsafe();
                        long offset = unsafe.staticFieldOffset(field);
                        Object base = unsafe.staticFieldBase(field);
                        setFieldUsingUnsafe(base, field.getType(), offset, newValue, unsafe);
                        return null;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
            } else {
                field.set(null, newValue);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void setFieldUsingUnsafe(final Field field, final Object object, final Object newValue) {
        try {
            field.setAccessible(true);
            int fieldModifiersMask = field.getModifiers();
            boolean isFinalModifierPresent = (fieldModifiersMask & Modifier.FINAL) == Modifier.FINAL;
            if (isFinalModifierPresent) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        Unsafe unsafe = getUnsafe();
                        long offset = unsafe.objectFieldOffset(field);
                        setFieldUsingUnsafe(object, field.getType(), offset, newValue, unsafe);
                        return null;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
            } else {
                try {
                    field.set(object, newValue);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Unsafe getUnsafe() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static void setFieldUsingUnsafe(Object base, Class type, long offset, Object newValue, Unsafe unsafe) {
        if (type == Integer.TYPE) {
            unsafe.putInt(base, offset, ((Integer) newValue));
        } else if (type == Short.TYPE) {
            unsafe.putShort(base, offset, ((Short) newValue));
        } else if (type == Long.TYPE) {
            unsafe.putLong(base, offset, ((Long) newValue));
        } else if (type == Byte.TYPE) {
            unsafe.putByte(base, offset, ((Byte) newValue));
        } else if (type == Boolean.TYPE) {
            unsafe.putBoolean(base, offset, ((Boolean) newValue));
        } else if (type == Float.TYPE) {
            unsafe.putFloat(base, offset, ((Float) newValue));
        } else if (type == Double.TYPE) {
            unsafe.putDouble(base, offset, ((Double) newValue));
        } else if (type == Character.TYPE) {
            unsafe.putChar(base, offset, ((Character) newValue));
        } else {
            unsafe.putObject(base, offset, newValue);
        }
    }

    /*
     * https://github.com/powermock/powermock/blob/42c72daf9d8b04129178d1d3f1fb4e485d3c13dc/powermock-reflect/src/main/java/org/powermock/reflect/internal/WhiteboxImpl.java#L2298-L2403
     */

}
