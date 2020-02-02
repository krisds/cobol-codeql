package com.semmle.cobol.generator.tuples;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.semmle.cobol.extractor.CobolExtractor;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.types.CaseType;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.mapping.values.ConstantValue;
import com.semmle.cobol.mapping.values.PartitionedValue;
import com.semmle.cobol.mapping.values.ReferenceValue;

public class Tuple {

	private Key key;
	private String name;
	private final Map<String, Value> values;

	/** How many tuples are parented to this one ? */
	private int childCount = 0;

	public Tuple(Key key, String name) {
		this.key = key;
		this.name = name;
		this.values = new LinkedHashMap<String, Value>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addValue(Value value) {
		values.put(value.getName(), value);
	}

	public Collection<Value> getValues() {
		return Collections.unmodifiableCollection(values.values());
	}

	public boolean hasValue(String name) {
		return values.containsKey(name);
	}

	public Value getValue(String name) {
		return values.get(name);
	}

	@Override
	public String toString() {
		return "@" + name + "#" + hashCode();
	}

	public void addIndexValue(int index) {
		addConstantValue("index", index);
		// Because gentools.py uses this shorthand...
		addConstantValue("idx", index);
	}

	public void addParentValue(Tuple parentTuple) {
		addParentValue(parentTuple, "parent");
	}

	public void addParentValue(Tuple parentTuple, String parentColumn) {
		addValue(new ReferenceValue(parentColumn, parentTuple));
		parentTuple.childCount += 1;
	}

	public void addIdValue(final RuleEngine engine) {
		addValue(new Value("id") {
			@Override
			public Object getValue() {
				final Object localId = engine.getTrapFile()
						.getLocalId(Tuple.this);

				return localId;
			}
		});
	}

	public void addKindValue(final RuleEngine engine) {
		addValue(new Value("kind") {
			@Override
			public Object getValue() {
				final Type type = CobolExtractor.getType(Tuple.this.getName());
				final CaseType kindType = (CaseType) type;
				return kindType.getKind();
			}
		});
	}

	public void addConstantValue(String name, Object value) {
		addValue(new ConstantValue(name, value));
	}

	public int getChildCount() {
		return childCount;
	}

	public String getStringAttribute(String attributeName) {
		if (hasValue(attributeName)) {
			final Value value = getValue(attributeName);

			if (value instanceof PartitionedValue) {
				final Tuple partitionedAttribute = ((PartitionedValue) value)
						.getTuple();

				return (String) partitionedAttribute.getValue(attributeName)
						.getValue();
			} else
				return (String) value.getValue();
		}

		return null;
	}

	public Integer getIntegerAttribute(String attributeName) {
		if (hasValue(attributeName)) {
			final Value value = getValue(attributeName);

			if (value instanceof PartitionedValue) {
				final Tuple partitionedAttribute = ((PartitionedValue) value)
						.getTuple();

				return (Integer) partitionedAttribute.getValue(attributeName)
						.getValue();
			} else
				return (Integer) value.getValue();
		}

		return null;
	}

	public Key getKey() {
		return key;
	}

	// ------------------------------------------------------------------------

	private static int NEXT_ID = 0;
	private final int ID = NEXT_ID++;

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
