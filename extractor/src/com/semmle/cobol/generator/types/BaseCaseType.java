package com.semmle.cobol.generator.types;

public class BaseCaseType extends TypeWithAttributes
		implements Type, PersistentType {

	private String tableName;

	public BaseCaseType(String name) {
		super(name);
		tableName = name + "s";
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		if (type instanceof CaseType)
			type = ((CaseType) type).getTopLevelType();

		if (type instanceof BaseCaseType)
			return getName().equals(((BaseCaseType) type).getName());
		else
			return false;
	}

	@Override
	public String getRelationName() {
		return tableName;
	}

	@Override
	public void setRelationName(String tableName) {
		this.tableName = tableName;
	}
}
