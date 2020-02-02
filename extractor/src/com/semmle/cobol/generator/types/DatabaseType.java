package com.semmle.cobol.generator.types;

public interface DatabaseType extends Type {
	DBType getDbtype();

	QLType getQltype();
}
