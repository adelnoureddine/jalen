/*
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 */

package jalen.agent.sockets;

import java.lang.reflect.*;

public class Delegator {
	private final Object source;
	private final Object delegate;
	private final Class<?> superclass;

	public Delegator(Object source, Class<?> superclass,
			Object delegate) {
		this.source = source;
		this.superclass = superclass;
		this.delegate = delegate;
	}

	public Delegator(Object source, Class<?> superclass,
			String delegateClassName) {
		try {
			this.source = source;
			this.superclass = superclass;
			Class<?> implCl = Class.forName(delegateClassName);
			Constructor<?> delegateConstructor =
					implCl.getDeclaredConstructor();
			delegateConstructor.setAccessible(true);
			this.delegate = delegateConstructor.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new DelegationException(
					"Could not make delegate object", e);
		}
	}

	public final <T> T invoke(Object... args) {
		try {
			String methodName = extractMethodName();
			Method method = findMethod(methodName, args);
			@SuppressWarnings("unchecked")
			T t = (T) invoke0(method, args);
			return t;
		} catch (NoSuchMethodException e) {
			throw new DelegationException(e);
		}
	}

	private Object invoke0(Method method, Object[] args) {
		try {
			writeFields(superclass, source, delegate);
			method.setAccessible(true);
			Object result = method.invoke(delegate, args);
			writeFields(superclass, delegate, source);
			return result;
		} catch (RuntimeException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new DelegationException(e.getCause());
		} catch (Exception e) {
			throw new DelegationException(e);
		}
	}

	private void writeFields(Class<?> clazz, Object from, Object to)
			throws Exception {
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			field.set(to, field.get(from));
		}
	}

	private String extractMethodName() {
		Throwable t = new Throwable();
		String methodName = t.getStackTrace()[2].getMethodName();
		return methodName;
	}

	private Method findMethod(String methodName, Object[] args)
			throws NoSuchMethodException {
		Class<?> clazz = superclass;
		if (args.length == 0) {
			return clazz.getDeclaredMethod(methodName);
		}
		Method match = null;
		next:
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.getName().equals(methodName)) {
					Class<?>[] classes = method.getParameterTypes();
					if (classes.length == args.length) {
						for (int i = 0; i < classes.length; i++) {
							Class<?> argType = classes[i];
							argType = convertPrimitiveClass(argType);
							if (!argType.isInstance(args[i])) continue next;
						}
						if (match == null) {
							match = method;
						} else {
							throw new DelegationException(
									"Duplicate matches");
						}
					}
				}
			}
		if (match != null) {
			return match;
		}
		else {
			next:
				for (Method method : clazz.getMethods()) {
					if (method.getName().equals(methodName)) {
						Class<?>[] classes = method.getParameterTypes();
						if (classes.length == args.length) {
							for (int i = 0; i < classes.length; i++) {
								Class<?> argType = classes[i];
								argType = convertPrimitiveClass(argType);
								if (!argType.isInstance(args[i])) continue next;
							}
							if (match == null) {
								match = method;
							} else {
								throw new DelegationException(
										"Duplicate matches");
							}
						}
					}
				}
		if (match != null) {
			return match;
		}
		}
		throw new DelegationException(
				"Could not find method: " + methodName);
	}

	private Class<?> convertPrimitiveClass(Class<?> primitive) {
		if (primitive.isPrimitive()) {
			if (primitive == int.class) {
				return Integer.class;
			}
			if (primitive == boolean.class) {
				return Boolean.class;
			}
			if (primitive == float.class) {
				return Float.class;
			}
			if (primitive == long.class) {
				return Long.class;
			}
			if (primitive == double.class) {
				return Double.class;
			}
			if (primitive == short.class) {
				return Short.class;
			}
			if (primitive == byte.class) {
				return Byte.class;
			}
			if (primitive == char.class) {
				return Character.class;
			}
		}
		return primitive;
	}

	public DelegatorMethodFinder delegateTo(String methodName,
			Class<?>... parameters) {
		return new DelegatorMethodFinder(methodName, parameters);
	}

	public class DelegatorMethodFinder {
		private final Method method;

		public DelegatorMethodFinder(String methodName,
				Class<?>... parameterTypes) {
			try {
				method = superclass.getDeclaredMethod(
						methodName, parameterTypes
						);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new DelegationException(e);
			}
		}

		public <T> T invoke(Object... parameters) {
			@SuppressWarnings("unchecked")
			T t = (T) Delegator.this.invoke0(method, parameters);
			return t;
		}
	}
}
