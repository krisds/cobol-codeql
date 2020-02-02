package com.semmle.cobol.generator.types;

/**
 * A persistent type is an interface used to mark {@linkplain Type}s for which
 * we should generate {@linkplain com.semmle.cobol.generator.tuples.Tuple}s.
 */
public interface PersistentType extends Type {
	String getRelationName();

	void setRelationName(String tableName);
}
