package com.semmle.cobol.generator.types;

import java.util.LinkedHashMap;
import java.util.Map;

public class TypeSystem {
	/**
	 * Maps type names to their corresponding {@linkplain Type} definition.
	 * <p>
	 * Used to implement the {@linkplain TypeSystem} interface.
	 */
	private final Map<String, Type> typeDefinitions = new LinkedHashMap<String, Type>();

	public void addType(Type type) {
		typeDefinitions.put(type.getName(), type);
	}

	public Type getType(String typeName) {
		return typeDefinitions.get(typeName);
	}
}
