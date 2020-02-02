package com.semmle.cobol.generator.types;

public abstract class NamedType implements Type {

	private final String name;

	public NamedType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "@" + name;
	}
}
