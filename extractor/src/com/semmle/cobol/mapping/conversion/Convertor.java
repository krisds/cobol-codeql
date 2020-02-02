package com.semmle.cobol.mapping.conversion;

import java.util.List;

import koopa.core.trees.Tree;

public abstract class Convertor<T> {

	private String name;
	private TypeConversion<T> typeConversion;

	public Convertor(String name) {
		this.name = name;
	}

	public void registerWith(TypeConversion<T> config) {
		this.typeConversion = config;
		this.typeConversion.register(name, this);
	}

	protected List<T> conversionOf(List<Tree> nodes) {
		return typeConversion.loadAllFrom(nodes);
	}

	protected T conversionOf(Tree definition) {
		return typeConversion.loadFrom(definition);
	}

	protected <S> S conversionTo(Tree definition, Class<S> clazz) {
		return typeConversion.loadFrom(definition, clazz);
	}

	protected abstract T applyTo(Tree definition);

	protected String qlType(String encoded) {
		return encoded.substring(1);
	}

	protected String string(String encoded) {
		return encoded.substring(1, encoded.length() - 1);
	}

	protected String attributeName(String encoded) {
		return encoded.substring(1);
	}

	protected String builtin(String encoded) {
		return encoded.substring(1, encoded.length() - 1);
	}

	protected int nodeIndex(String encoded) {
		return Integer.parseInt(encoded.substring(1));
	}

	protected String ypath(String encoded) {
		return encoded.substring(1, encoded.length() - 1);
	}
}
