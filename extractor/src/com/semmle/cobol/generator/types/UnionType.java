package com.semmle.cobol.generator.types;

import com.semmle.cobol.extractor.CobolExtractor;

public class UnionType extends NamedType implements Type {

	private final String[] types;

	public UnionType(String name, String... types) {
		super(name);
		this.types = types;
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		for (String t : types)
			if (CobolExtractor.getType(t).isAssignableFrom(type))
				return true;

		return false;
	}
}
