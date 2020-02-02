package com.semmle.cobol.mapping.values;

import com.semmle.cobol.generator.tuples.Value;

public class ConstantValue extends Value {

	private final Object value;

	public ConstantValue(String name, Object value) {
		super(name);
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}
}
