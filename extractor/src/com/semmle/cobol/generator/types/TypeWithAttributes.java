package com.semmle.cobol.generator.types;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TypeWithAttributes extends NamedType implements Type {

	private final Map<String, Attribute> attributes = new LinkedHashMap<String, Attribute>();

	public TypeWithAttributes(String name) {
		super(name);
	}

	public void addAttribute(String name, String typeName) {
		attributes.put(name, new Attribute(name, typeName));
	}

	public void addAttribute(String name, String typeName, int index) {
		attributes.put(name, new Attribute(name, typeName, index));
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	public Collection<String> attributeNames() {
		return attributes.keySet();
	}
}
