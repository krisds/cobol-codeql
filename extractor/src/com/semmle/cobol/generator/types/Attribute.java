package com.semmle.cobol.generator.types;

public class Attribute {

	public static final int NO_INDEX = -1;

	private final String name;
	private String typeName;
	private final int index;

	public Attribute(String name, String typeName, int index) {
		super();
		this.name = name;
		this.typeName = typeName;
		this.index = index;
	}

	public Attribute(String name, String typeName) {
		this(name, typeName, NO_INDEX);
	}

	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return name;
	}
}
