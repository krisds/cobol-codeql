package com.semmle.cobol.generator.types;

public class PredefinedType extends NamedType implements Type, TrappableType,
		PersistentType {

	private String tableName;

	public PredefinedType(String name, String tableName) {
		super(name);
		this.tableName = tableName;
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return type instanceof PredefinedType
				&& getName().equals(type.getName());
	}

	@Override
	public String getRelationName() {
		return tableName;
	}

	@Override
	public void setRelationName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public PersistentType getPersistentType() {
		return this;
	}
}
