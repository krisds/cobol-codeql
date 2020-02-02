package com.semmle.cobol.generator.types;

import com.semmle.cobol.extractor.CobolExtractor;

public class CaseType extends TypeWithAttributes
		implements Type, TrappableType {

	private final int kind;
	private final String topLevelType;

	public CaseType(String name, String topLevelType, int kind) {
		super(name);
		this.topLevelType = topLevelType;
		this.kind = kind;
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return type instanceof CaseType && ((CaseType) type).kind == kind;
	}

	public BaseCaseType getTopLevelType() {
		final Type type = CobolExtractor.getType(topLevelType);

		if (type instanceof BaseCaseType)
			return (BaseCaseType) type;
		else
			return null;
	}

	public int getKind() {
		return kind;
	}

	@Override
	public PersistentType getPersistentType() {
		Type type = CobolExtractor.getType(topLevelType);

		if (type instanceof PersistentType)
			return (PersistentType) type;
		else
			return null;
	}

	public Attribute getAttribute(String name) {
		if (hasAttribute(name))
			return super.getAttribute(name);
		else
			return getTopLevelType().getAttribute(name);
	}
}
