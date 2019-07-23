package org.inventivetalent.reflectionhelper.test;

import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.wrapper.ClassWrapper;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResolverTest {

	@Test
	public void basic() throws ReflectiveOperationException{
		ClassResolver classResolver = new ClassResolver();
		Class clazz = classResolver.resolve("org.inventivetalent.reflection.resolver.wrapper.ClassWrapper");
		assertNotNull(clazz);
		assertEquals(ClassWrapper.class, clazz);

		FieldResolver fieldResolver = new FieldResolver(clazz);
		Field field = fieldResolver.resolve("clazz");
		assertNotNull(field);

		ConstructorResolver constructorResolver = new ConstructorResolver(clazz);
		Constructor constructor = constructorResolver.resolveFirstConstructor();
		assertNotNull(constructor);

		MethodResolver methodResolver = new MethodResolver(clazz);
		Method method = methodResolver.resolve("newInstance");
		assertNotNull(method);
	}

}
