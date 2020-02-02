package com.semmle.cobol.mapping.conversion;

import java.util.LinkedHashMap;
import java.util.Map;

public class Conversion {
	private Map<Class<?>, TypeConversion<?>> typeConversions //
	= new LinkedHashMap<Class<?>, TypeConversion<?>>();

	public <T> void register(Class<T> clazz, TypeConversion<T> loader) {
		typeConversions.put(clazz, loader);
	}

	@SuppressWarnings("unchecked")
	public <S> TypeConversion<S> getTypeConversion(Class<S> clazz) {
		if (typeConversions.containsKey(clazz))
			return (TypeConversion<S>) typeConversions.get(clazz);
		else
			return null;
	}
}
