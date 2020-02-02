package com.semmle.cobol.generator.tables;

import com.semmle.cobol.generator.types.DBType;

public class Column {

	private final String name;
	private final DBType dbType;

	public Column(String name, DBType dbType) {
		this.name = name;
		this.dbType = dbType;
	}

	public String getName() {
		return name;
	}

	public DBType getDbType() {
		return dbType;
	}

	@Override
	public String toString() {
		return dbType + " " + name;
	}
}
