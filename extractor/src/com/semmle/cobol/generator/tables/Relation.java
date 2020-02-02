package com.semmle.cobol.generator.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.semmle.cobol.generator.types.DBType;

public class Relation {

	private final String name;
	private final List<Column> columns;

	public Relation(String name) {
		this.name = name;
		this.columns = new ArrayList<Column>();
	}

	public String getName() {
		return name;
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public void addColumn(String name, DBType dbType) {
		columns.add(new Column(name, dbType));
	}

	public List<Column> getColumns() {
		return Collections.unmodifiableList(columns);
	}
}
