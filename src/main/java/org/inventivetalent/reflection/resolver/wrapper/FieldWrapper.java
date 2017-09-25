package org.inventivetalent.reflection.resolver.wrapper;

import java.lang.reflect.Field;

@SuppressWarnings({"unused", "WeakerAccess"})
public class FieldWrapper<R> extends WrapperAbstract {

	private final Field field;

	public FieldWrapper(Field field) {
		this.field = field;
	}

	@Override
	public boolean exists() {
		return this.field != null;
	}

	public String getName() {
		return this.field.getName();
	}

	@SuppressWarnings("unchecked")
	public R get(Object object) {
		try {
			return (R) this.field.get(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public R getSilent(Object object) {
		try {
			return (R) this.field.get(object);
		} catch (Exception ignored) {
		}
		return null;
	}

	public void set(Object object, R value) {
		try {
			this.field.set(object, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSilent(Object object, R value) {
		try {
			this.field.set(object, value);
		} catch (Exception ignored) {
		}
	}

	public Field getField() {
		return field;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		if (object == null || getClass() != object.getClass()) { return false; }

		FieldWrapper<?> that = (FieldWrapper<?>) object;

		return field != null ? field.equals(that.field) : that.field == null;
	}

	@Override
	public int hashCode() {
		return field != null ? field.hashCode() : 0;
	}
}
