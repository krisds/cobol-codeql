package com.semmle.cobol.generator.types;

public interface Type {

	String getName();

	boolean isAssignableFrom(Type type);
}
