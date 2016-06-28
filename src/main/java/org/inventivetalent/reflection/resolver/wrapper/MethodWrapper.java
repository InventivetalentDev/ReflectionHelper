/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.reflection.resolver.wrapper;

import java.lang.reflect.Method;

public class MethodWrapper<R> extends WrapperAbstract {

	private final Method method;

	public MethodWrapper(Method method) {
		this.method = method;
	}

	@Override
	public boolean exists() {
		return this.method != null;
	}

	public String getName() {
		return this.method.getName();
	}

	public R invoke(Object object, Object... args) {
		try {
			return (R) this.method.invoke(object, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public R invokeSilent(Object object, Object... args) {
		try {
			return (R) this.method.invoke(object, args);
		} catch (Exception e) {
		}
		return null;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		if (object == null || getClass() != object.getClass()) { return false; }

		MethodWrapper<?> that = (MethodWrapper<?>) object;

		return method != null ? method.equals(that.method) : that.method == null;

	}

	@Override
	public int hashCode() {
		return method != null ? method.hashCode() : 0;
	}

	/**
	 * Generates a method's signature.
	 *
	 * @param method         the method to get the signature for
	 * @param fullClassNames whether to use the full class name
	 * @return the method's signature
	 */
	public static String getMethodSignature(Method method, boolean fullClassNames) {
//		StringBuilder stringBuilder = new StringBuilder();
//
//		Class<?> returnType = method.getReturnType();
//		if (returnType.isPrimitive()) {
//			stringBuilder.append(returnType);
//		} else {
//			stringBuilder.append(fullClassNames ? returnType.getName() : returnType.getSimpleName());
//		}
//		stringBuilder.append(" ");
//		stringBuilder.append(method.getName());
//
//		stringBuilder.append("(");
//
//		boolean first = true;
//		for (Class clazz : method.getParameterTypes()) {
//			if (!first) { stringBuilder.append(","); }
//			stringBuilder.append(fullClassNames ? clazz.getName() : clazz.getSimpleName());
//			first = false;
//		}
//		return stringBuilder.append(")").toString();

		return MethodSignature.of(method, fullClassNames).getSignature();
	}

	/**
	 * @param method Method to get the signature for
	 * @return the signature
	 * @see #getMethodSignature(Method, boolean)
	 */
	public static String getMethodSignature(Method method) {
		return getMethodSignature(method, false);
	}

	public static class MethodSignature {
		private final String   returnType;
		private final String   name;
		private final String[] parameterTypes;
		private final String   signature;

		public MethodSignature(String returnType, String name, String[] parameterTypes) {
			this.returnType = returnType;
			this.name = name;
			this.parameterTypes = parameterTypes;

			StringBuilder builder = new StringBuilder();
			builder.append(returnType).append(" ").append(name).append("(");
			boolean first = true;
			for (String parameterType : parameterTypes) {
				if (!first) {
					builder.append(",");
				}
				builder.append(parameterType);
				first = false;
			}
			this.signature = builder.append(")").toString();
		}

		public static MethodSignature of(Method method, boolean fullClassNames) {
			Class<?> returnType = method.getReturnType();
			Class<?>[] parameterTypes = method.getParameterTypes();

			String returnTypeString;
			if (returnType.isPrimitive()) {
				returnTypeString = returnType.toString();
			} else {
				returnTypeString = fullClassNames ? returnType.getName() : returnType.getSimpleName();
			}
			String methodName = method.getName();
			String[] parameterTypeStrings = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypeStrings.length; i++) {
				if (parameterTypes[i].isPrimitive()) {
					parameterTypeStrings[i] = parameterTypes[i].toString();
				} else {
					parameterTypeStrings[i] = fullClassNames ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName();
				}
			}

			return new MethodSignature(returnTypeString, methodName, parameterTypeStrings);
		}

		public String getReturnType() {
			return returnType;
		}

		public String getName() {
			return name;
		}

		public String[] getParameterTypes() {
			return parameterTypes;
		}

		public String getSignature() {
			return signature;
		}

		@Override
		public String toString() {
			return getSignature();
		}
	}

}
