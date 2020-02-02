package com.semmle.cobol.extractor;

import java.util.LinkedHashMap;
import java.util.Map;

import com.semmle.cobol.generator.tables.DatabaseScheme;
import com.semmle.cobol.generator.tables.Relation;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.BaseCaseType;
import com.semmle.cobol.generator.types.CaseType;
import com.semmle.cobol.generator.types.DatabaseType;
import com.semmle.cobol.generator.types.ListType;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.generator.types.TypeSystem;
import com.semmle.cobol.generator.types.TypeWithAttributes;
import com.semmle.cobol.population.CobolPopulator;
import com.semmle.cobol.population.CobolTypesFromSpec;
import com.semmle.cobol.population.CommonPopulator;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;

public class CobolExtractor {

	/**
	 * Maps type names to their corresponding {@linkplain Type} definition.
	 */
	private static final TypeSystem TYPE_SYSTEM = new TypeSystem();

	/**
	 * Maps table names to their corresponding {@linkplain Relation} definition.
	 * <p>
	 * Used to implement the {@linkplain DatabaseScheme} interface.
	 */
	private static final DatabaseScheme DATABASE_SCHEME = new DatabaseScheme();

	/**
	 * Maps nodes to their default type name.
	 */
	private static final Map<Start, String> DEFAULT_TYPES = new LinkedHashMap<>();

	static {
		CommonPopulator.populateTypeSystem(TYPE_SYSTEM);
		CommonPopulator.populateDatabaseScheme(DATABASE_SCHEME);
		CommonPopulator.populateDefaultTypes(DEFAULT_TYPES);

		// Generated data:
		CobolPopulator.populate(DATABASE_SCHEME, TYPE_SYSTEM);
		CobolTypesFromSpec.populateDefaultTypes(DEFAULT_TYPES);
	}

	public static TypeSystem getTypeSystem() {
		return TYPE_SYSTEM;
	}

	public static DatabaseScheme getDatabaseScheme() {
		return DATABASE_SCHEME;
	}

	public static Type getType(String typeName) {
		return TYPE_SYSTEM.getType(typeName);
	}

	public static Type getType(Tuple tuple) {
		return getType(tuple.getName());
	}

	public static Type getType(Attribute attribute) {
		return getType(attribute.getTypeName());
	}

	public static Relation getRelation(String name) {
		return DATABASE_SCHEME.getRelation(name);
	}

	public static Attribute getAttribute(Type type, String attributeName) {
		if (type instanceof CaseType)
			return ((CaseType) type).getAttribute(attributeName);
		else if (type instanceof BaseCaseType)
			return ((BaseCaseType) type).getAttribute(attributeName);
		else
			return null;
	}

	public static DatabaseType getDatabaseType(Type type) {
		if (type instanceof ListType)
			type = ((ListType) type).getItemType();

		if (type instanceof DatabaseType)
			return (DatabaseType) type;
		else
			return null;
	}

	public static boolean typeCanBeRecast(Type oldType, Type newType) {
		// Partitions can be recast if they partition the same DB type.
		if (oldType instanceof Partition && newType instanceof Partition) {
			final Partition oldPartitionType = (Partition) oldType;
			final Partition newPartitionType = (Partition) newType;
			return oldPartitionType.getDbtype() == newPartitionType.getDbtype();
		}

		// Types with attributes can be recast if their attributes are
		// compatible.
		if (oldType instanceof TypeWithAttributes
				&& newType instanceof TypeWithAttributes) {

			final TypeWithAttributes oldTypeWA = (TypeWithAttributes) oldType;
			final TypeWithAttributes newTypeWA = (TypeWithAttributes) newType;

			for (String name : oldTypeWA.attributeNames()) {
				final Attribute oldAttr = getAttribute(oldTypeWA, name);
				final Attribute newAttr = getAttribute(newTypeWA, name);

				if (newAttr == null)
					continue;

				if (newAttr.getIndex() != oldAttr.getIndex())
					return false;

				final Type oldAttrType = getType(oldAttr);
				final Type newAttrType = getType(newAttr);

				final DatabaseType oldDBType = getDatabaseType(oldAttrType);

				if (oldDBType != null) {
					final DatabaseType newDBType = getDatabaseType(newAttrType);
					if (newDBType == null)
						return false;
					if (oldDBType.getDbtype() != (newDBType.getDbtype()))
						return false;
				} else if (!newAttr.getTypeName().equals(oldAttr.getTypeName()))
					return false;
			}

			return true;
		}

		return false;
	}

	public static String getDefaultTypeName(Data d, String fallback) {
		String type = DEFAULT_TYPES.get(d);
		if (type == null)
			type = fallback;
		return type;
	}

	public static String getDefaultTypeName(Data d) {
		return getDefaultTypeName(d, null);
	}
}
