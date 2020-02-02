package com.semmle.cobol.mapping.values;

import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.tuples.Value;

public class ReferenceValue extends Value {

	private final Tuple tuple;
	private final String referencedColumnName;

	public ReferenceValue(String columnName, Tuple tuple) {
		this(columnName, tuple, "id");
	}

	public ReferenceValue(String columnName, Tuple tuple,
			String referencedColumnName) {
		super(columnName);
		this.tuple = tuple;
		this.referencedColumnName = referencedColumnName;
	}

	public Tuple getTuple() {
		return tuple;
	}

	@Override
	public Object getValue() {
		Value value = tuple.getValue(referencedColumnName);

		if (value == null)
			return null;
		else
			return value.getValue();
	}
}
