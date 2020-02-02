package com.semmle.cobol.generator.types;

import com.semmle.cobol.extractor.CobolExtractor;

/**
 * Rather than having a single table/relation holding all values of a certain
 * primitive type, we split those values up based on their use. Each of these
 * becomes a "partition" of that primitive type.
 */
public class Partition extends NamedType implements Type, TrappableType,
		PersistentType, DatabaseType {

	private final String typeName;

	private String relationName = null;

	private String valueColumn = null;

	private String parentColumn;

	public Partition(String name, String typeName) {
		super(name);
		this.typeName = typeName;
	}

	@Override
	public String getRelationName() {
		return relationName;
	}

	@Override
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return type == this;
	}

	public PrimitiveType getBaseType() {
		final Type base = CobolExtractor.getType(typeName);
		if (base != null && base instanceof PrimitiveType)
			return (PrimitiveType) base;
		else
			return null;
	}

	@Override
	public DBType getDbtype() {
		PrimitiveType base = getBaseType();
		return base == null ? null : base.getDbtype();
	}

	@Override
	public QLType getQltype() {
		PrimitiveType base = getBaseType();
		return base == null ? null : base.getQltype();
	}

	@Override
	public PersistentType getPersistentType() {
		return this;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setParentColumn(String parentColumn) {
		this.parentColumn = parentColumn;
	}

	public String getParentColumn() {
		return parentColumn;
	}
}
