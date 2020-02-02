package com.semmle.cobol.generator.tables;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseScheme {
	private final Map<String, Relation> tableDefinitions = new LinkedHashMap<String, Relation>();

	public void addRelation(Relation table) {
		tableDefinitions.put(table.getName(), table);
	}

	public Relation getRelation(String name) {
		return tableDefinitions.get(name);
	}
}
