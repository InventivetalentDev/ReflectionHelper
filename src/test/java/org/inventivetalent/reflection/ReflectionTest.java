package org.inventivetalent.reflection;

import org.inventivetalent.reflection.resolver.wrapper.MethodWrapper;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ReflectionTest {

	public double primitiveDummyMethod(String aString, double returnValue) {
		return returnValue;
	}

	public Method genericDummyMethod(Thread aThread, Exception aException) {
		return null;
	}

	public void voidDummyMethod(Character aCharacter, Class aClass, String aString) {
	}

	public int wildcardMethod1(String string) {
		return 0;
	}

	public double wildcardMethod2(String string) {
		return 0;
	}

	public double wildcardMethod3(boolean b) {
		return 0;
	}

	public int wildCardMethod4(String string) {
		return 0;
	}

	@Test
	public void primitiveSignatureTest() throws ReflectiveOperationException {
		String signature = MethodWrapper.getMethodSignature(ReflectionTest.class.getMethod("primitiveDummyMethod", String.class, double.class));
		assertEquals("double primitiveDummyMethod(String,double)", signature);
	}

	@Test
	public void genericSignatureTest() throws ReflectiveOperationException {
		String signature = MethodWrapper.getMethodSignature(ReflectionTest.class.getMethod("genericDummyMethod", Thread.class, Exception.class));
		assertEquals("Method genericDummyMethod(Thread,Exception)", signature);
	}

	@Test
	public void voidSignatureTest() throws ReflectiveOperationException {
		String signature = MethodWrapper.getMethodSignature(ReflectionTest.class.getMethod("voidDummyMethod", Character.class, Class.class, String.class));
		assertEquals("void voidDummyMethod(Character,Class,String)", signature);
	}

	@Test
	public void fullNameSignatureTest() throws ReflectiveOperationException {
		String signature = MethodWrapper.getMethodSignature(ReflectionTest.class.getMethod("genericDummyMethod", Thread.class, Exception.class), true);
		assertEquals("java.lang.reflect.Method genericDummyMethod(java.lang.Thread,java.lang.Exception)", signature);
	}

	@Test
	public void signatureObjectTest() throws ReflectiveOperationException {
		MethodWrapper.MethodSignature signature = MethodWrapper.MethodSignature.of(ReflectionTest.class.getMethod("genericDummyMethod", Thread.class, Exception.class), false);
		assertEquals("Method", signature.getReturnType());
		assertEquals("genericDummyMethod", signature.getName());
		assertArrayEquals(new String[] {
				"Thread",
				"Exception" }, signature.getParameterTypes());
	}

	@Test
	public void signatureFromStringTest() {
		MethodWrapper.MethodSignature signature = MethodWrapper.MethodSignature.fromString("java.lang.reflect.Method genericDummyMethod(java.lang.Thread,java.lang.Exception)");
		assertEquals("java.lang.reflect.Method", signature.getReturnType());
		assertEquals("genericDummyMethod", signature.getName());
		assertEquals("java.lang.Thread", signature.getParameterType(0));
		assertEquals("java.lang.Exception", signature.getParameterType(1));
	}

	@Test
	public void wildcardTest() throws ReflectiveOperationException {
		MethodWrapper.MethodSignature wildcardSignature = MethodWrapper.MethodSignature.fromString("* wildcardMethod*(String)");
		MethodWrapper.MethodSignature testSignature1 = MethodWrapper.MethodSignature.of(ReflectionTest.class.getMethod("wildcardMethod1", String.class), false);
		MethodWrapper.MethodSignature testSignature2 = MethodWrapper.MethodSignature.of(ReflectionTest.class.getMethod("wildcardMethod2", String.class), false);
		MethodWrapper.MethodSignature testSignature3 = MethodWrapper.MethodSignature.of(ReflectionTest.class.getMethod("wildcardMethod3", boolean.class), false);
		MethodWrapper.MethodSignature testSignature4 = MethodWrapper.MethodSignature.of(ReflectionTest.class.getMethod("wildCardMethod4", String.class), false);

		assertTrue(wildcardSignature.matches(testSignature1));
		assertTrue(wildcardSignature.matches(testSignature2));
		assertFalse(wildcardSignature.matches(testSignature3));
		assertFalse(wildcardSignature.matches(testSignature4));
	}

}
