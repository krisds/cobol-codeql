package com.semmle.cobol.mapping.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.extractor.CobolExtractor;
import com.semmle.cobol.generator.tables.Column;
import com.semmle.cobol.generator.tables.Relation;
import com.semmle.cobol.generator.tuples.Key;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.tuples.Value;
import com.semmle.cobol.generator.types.PersistentType;
import com.semmle.cobol.generator.types.TrappableType;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.generator.types.TypeWithAttributes;
import com.semmle.cobol.mapping.values.PartitionedValue;
import com.semmle.cobol.timing.Timing;
import com.semmle.util.exception.CatastrophicError;
import com.semmle.util.exception.Exceptions;
import com.semmle.util.trap.DefaultTrapWriterFactory;
import com.semmle.util.trap.TrapWriter;
import com.semmle.util.trap.TrapWriter.Label;

/**
 * This class is used to generate, and track, all {@linkplain Tuple} instances.
 * It also checks the generated tuples at the end, and writes them out to a trap
 * file.
 */
public class TrapFile {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TrapFile.class);

	/**
	 * The file for which we're trapping tuples.
	 */
	private final File sourceFile;

	/**
	 * The trap writer to be used while generating the trap file.
	 */
	private TrapWriter trapWriter = null;

	/**
	 * Tracking some named values.
	 */
	private Map<String, Object> values = new LinkedHashMap<String, Object>();

	/**
	 * Tracking all generated tuples. Cfr {@linkplain Key} to see how tuples get
	 * indexed.
	 */
	private Map<Key, Tuple> tuples = new LinkedHashMap<Key, Tuple>();

	public TrapFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public Object populateFile() {
		return populateFile(sourceFile);
	}

	public Object populateFile(File file) {
		try {
			return trapWriter.populateFile(file.getCanonicalFile());

		} catch (IOException e) {
			Exceptions.ignore(e,
					"Canonical path failed. Trying absolute instead.");
			return trapWriter.populateFile(file.getAbsoluteFile());
		}
	}

	public Object getLocalId(Tuple tuple) {
		return trapWriter.localID(tuple, tuple.getName());
	}

	public Tuple getTuple(String typeName, Object subject) {
		return getTuple(typeName, subject, "default");
	}

	public Tuple getTuple(String typeName, Object subject, String topic) {
		if (topic == null)
			topic = "default";

		final TrappableType type = getKnownTrappableType(typeName);
		final Key key = new Key(type, subject, topic);

		if (!tuples.containsKey(key)) {
			final Tuple tuple = new Tuple(key, typeName);
			tuples.put(key, tuple);

			if (LOGGER.isTraceEnabled())
				LOGGER.trace(". tuple for " + key + " --> NEW " + tuple);

			return tuple;

		} else {
			final Tuple tuple = tuples.get(key);

			if (LOGGER.isTraceEnabled())
				LOGGER.trace(". tuple for " + key + " --> " + tuple);

			return tuple;
		}
	}

	public Tuple getExistingTuple(String typeName, Object subject,
			String topic) {

		if (topic == null)
			topic = "default";

		final Type type = CobolExtractor.getType(typeName);
		if (type instanceof TrappableType) {
			final Key key = new Key((TrappableType) type, subject, topic);
			return tuples.get(key);

		} else
			return null;
	}

	public int countTuples() {
		return tuples.size();
	}

	public List<Tuple> getTuplesOfType(String typeName) {
		Type type = CobolExtractor.getType(typeName);
		if (type == null)
			return Collections.emptyList();

		List<Tuple> matchingTuples = new LinkedList<Tuple>();
		for (Key key : tuples.keySet())
			if (type.isAssignableFrom(key.type))
				matchingTuples.add(tuples.get(key));

		return matchingTuples;
	}

	private TrappableType getKnownTrappableType(String typeName) {
		final Type type = CobolExtractor.getType(typeName);

		if (type == null)
			throw new IllegalArgumentException(
					"Type system does not know type: " + typeName);

		if (!(type instanceof TrappableType))
			throw new CatastrophicError(
					"Can only trap types which are actually trappable. "
							+ "Type " + typeName + " is not.");

		return (TrappableType) type;
	}

	public void clearNonTrappableTuples() {
		Timing.start("clearing non-trappable tuples");

		Iterator<Entry<Key, Tuple>> it = tuples.entrySet().iterator();

		while (it.hasNext()) {
			Entry<Key, Tuple> entry = it.next();

			Tuple tuple = entry.getValue();
			Type type = CobolExtractor.getType(tuple.getName());
			if (!(type instanceof TrappableType)) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Clearing non trappable: " + tuple);
				it.remove();
			}
		}

		Timing.end("clearing non-trappable tuples");
	}

	public boolean validateTuplesAgainstDatabaseScheme() {
		Timing.start("validating tuples against db");

		Iterator<Entry<Key, Tuple>> it = tuples.entrySet().iterator();

		boolean valid = true;
		while (it.hasNext()) {
			Entry<Key, Tuple> entry = it.next();

			TrappableType type = entry.getKey().type;
			Tuple tuple = entry.getValue();

			PersistentType persistentType = type.getPersistentType();
			Relation table = CobolExtractor
					.getRelation(persistentType.getRelationName());
			List<Column> columns = table.getColumns();

			for (Column column : columns) {
				if (!tuple.hasValue(column.getName())) {
					LOGGER.error("Tuple " + tuple + " missing value for column "
							+ column.getName() + "." + " Key was: "
							+ entry.getKey() + ".");
					valid = false;
				}
			}
		}

		Timing.end("validating tuples against db");
		return valid;
	}

	public void storeTuples(DefaultTrapWriterFactory trapWriterFactory) {
		Timing.start("storing tuples");

		try {
			File trapFile = trapWriterFactory.getTrapFileFor(sourceFile);
			trapWriter = new TrapWriter(trapFile);

			Label sourceFileLabel = trapWriter.populateFile(sourceFile);
			values.put("file", sourceFileLabel);

			Iterator<Entry<Key, Tuple>> it = tuples.entrySet().iterator();

			trappingTuples: while (it.hasNext()) {
				Entry<Key, Tuple> entry = it.next();

				TrappableType type = entry.getKey().type;
				Tuple tuple = entry.getValue();

				PersistentType persistentType = type.getPersistentType();
				Relation table = CobolExtractor
						.getRelation(persistentType.getRelationName());
				List<Column> columns = table.getColumns();
				Object[] values = new Object[columns.size()];

				for (int i = 0; i < columns.size(); i++) {
					Column column = columns.get(i);

					Value value = tuple.getValue(column.getName());
					if (value == null)
						continue trappingTuples;

					Object actualValue = value.getValue();
					if (actualValue == null)
						continue trappingTuples;

					values[i] = actualValue;
				}

				trapWriter.addTuple(table.getName(), values);
			}

		} finally {
			if (trapWriter != null)
				trapWriter.close();

			Timing.end("storing tuples");
		}
	}

	public Object getValue(String key) {
		return values.get(key);
	}

	public void overrideTupleType(Tuple tuple, String newTypeName) {
		// Don't override if already of the right type.
		final String oldTypeName = tuple.getName();
		if (newTypeName.equals(oldTypeName))
			return;

		// Validate the new type is compatible with the old one.
		final TrappableType oldType = getKnownTrappableType(oldTypeName);
		final TrappableType newType = getKnownTrappableType(newTypeName);
		if (!CobolExtractor.typeCanBeRecast(oldType, newType))
			throw new CatastrophicError(
					"Can't recast @" + oldTypeName + " to " + newType);

		// NOTE: attributes etc are expected to share details and
		// indexes, and so should not need an update.

		final Key oldKey = tuple.getKey();
		if (!tuples.containsKey(oldKey))
			throw new CatastrophicError("Old key not found: " + oldKey);

		final Key newKey = new Key(newType, oldKey.subject, oldKey.topic);
		if (tuples.containsKey(newKey))
			throw new CatastrophicError("New key already exists: " + newKey);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace(". converting " + tuple + " to " + newKey);

		tuple.setName(newTypeName);
		tuples.remove(oldKey);
		tuples.put(newKey, tuple);

		// The tuple's "kind" is late bound and should be fine.

		// Convert partitioned attributes...
		if (newType instanceof TypeWithAttributes) {
			final TypeWithAttributes twa = (TypeWithAttributes) newType;
			for (Value v : tuple.getValues()) {
				if (v instanceof PartitionedValue) {
					final PartitionedValue p = (PartitionedValue) v;

					final Tuple partitionedTuple = p.getTuple();
					final String partitionedAttributeName = p.getName();
					final String partitionedTypeName = twa
							.getAttribute(partitionedAttributeName)
							.getTypeName();

					if (LOGGER.isTraceEnabled())
						LOGGER.trace(
								". converting partitioned " + partitionedTuple
										+ " to " + partitionedTypeName);

					overrideTupleType(partitionedTuple, partitionedTypeName);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Trap<" + sourceFile + ">";
	}
}
