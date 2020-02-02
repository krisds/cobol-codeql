package com.semmle.cobol.generator.tuples;

public abstract class Value {

	private final String name;

	public Value(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract Object getValue();
}
