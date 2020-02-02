package com.semmle.cobol.mapping.values;

import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.tuples.Value;

/**
 * This is a dummy value, used for tracking attributes whose value is linked in
 * reverse. I.e. where the value has a reference back to the parent tuple,
 * rather than the other way around.
 */
public class ReferencedByTuple extends Value {

	/**
	 * The {@link Tuple} referencing the one which has this as a {@link Value}.
	 */
	private final Tuple tuple;

	public ReferencedByTuple(String columnName, Tuple tuple) {
		super(columnName);
		this.tuple = tuple;
	}

	public Tuple getTuple() {
		return tuple;
	}

	@Override
	public Object getValue() {
		throw new UnsupportedOperationException("Not expected to be trapped.");
	}
}
