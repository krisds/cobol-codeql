package com.semmle.cobol.generator.types;

public class PrimitiveType extends NamedType implements Type, TrappableType,
		PersistentType, DatabaseType {

	private final DBType dbtype;
	private final QLType qltype;
	private String tableName;

	public PrimitiveType(String name, DBType dbtype, QLType qltype) {
		super(name);
		this.tableName = name + "s";
		this.dbtype = dbtype;
		this.qltype = qltype;
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return type instanceof PrimitiveType
				&& getName().equals(type.getName());
	}

	@Override
	public DBType getDbtype() {
		return dbtype;
	}

	@Override
	public QLType getQltype() {
		return qltype;
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
