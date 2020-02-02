package com.semmle.cobol.generator.types;

import com.semmle.cobol.extractor.CobolExtractor;

public class ListType extends NamedType
		implements Type, TrappableType, PersistentType {

	private String itemTypeName;
	private String tableName;

	public ListType(String name, String itemType) {
		super(name);
		this.itemTypeName = itemType;
		this.tableName = name + "s";
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return this == type || type instanceof ListType && getItemType()
				.isAssignableFrom(((ListType) type).getItemType());
	}

	public Type getItemType() {
		return CobolExtractor.getType(itemTypeName);
	}

	public String getItemTypeName() {
		return itemTypeName;
	}

	public void setItemTypeName(String itemTypeName) {
		this.itemTypeName = itemTypeName;
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
