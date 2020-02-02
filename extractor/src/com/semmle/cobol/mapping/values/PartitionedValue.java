package com.semmle.cobol.mapping.values;

import com.semmle.cobol.generator.tuples.Tuple;

/**
 * A special subclass of {@link ReferencedByTuple} to flag values which are for
 * partitioned tuples.
 */
public class PartitionedValue extends ReferencedByTuple {

	public PartitionedValue(String columnName, Tuple tuple) {
		super(columnName, tuple);
	}
}
