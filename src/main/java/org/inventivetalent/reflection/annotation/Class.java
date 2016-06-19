package org.inventivetalent.reflection.annotation;

import org.inventivetalent.reflection.minecraft.Minecraft;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Class {

	/**
	 * Name of the class. Use <code>{nms}.MyClass</code> for NMS classes, or <code>{obc}.MyClass</code> for OBC classes
	 * @return the class name
	 */
	String[] value();

	Minecraft.Version[] versions() default {};

	boolean ignoreExceptions() default true;
}
