package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.extractor.CobolExtractor.getAttribute;
import static com.semmle.cobol.extractor.CobolExtractor.getDatabaseType;
import static com.semmle.cobol.extractor.CobolExtractor.getType;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.tuples.Value;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.DBType;
import com.semmle.cobol.generator.types.DatabaseType;
import com.semmle.cobol.generator.types.ListType;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.mapping.values.ReferenceValue;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;
import koopa.core.data.Token;

/**
 * An {@link Effect} which assigns a {@link Tuple} to a given attribute in
 * another {@link Tuple}.
 */
class AssignToEffect implements Effect {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AssignToEffect.class);

	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	/**
	 * What attribute are we assigning to ?
	 * <p>
	 * This attribute will be looked up in the owner.
	 */
	private final String attributeName;

	AssignToEffect(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		// We expect to find a Tuple to assign, unless the mayBeOmitted flag has
		// been set, in which case this becomes a no-op.
		if (frame.tuple == null && frame.mayBeOmitted) {
			if (TRACE_ENABLED)
				LOGGER.trace("tuple was omitted for: " + frame.node);
			return;
		}

		final Node node = event.getNode();
		final Frame current = frame;
		final Frame parent = current.findAncestorWithTuple();
		final Type ownerType = getType(parent.tuple.getName());
		final Attribute attribute = getAttribute(ownerType, attributeName);
		final Type attributeType = getType(attribute);
		final DatabaseType attributeDatabaseType = getDatabaseType(
				attributeType);

		Tuple tuple = null;
		if (attributeDatabaseType != null) {
			// If the attribute is a raw database type then we first need to
			// create a Tuple for the collected Data.
			if (TRACE_ENABLED)
				LOGGER.trace("rvalue has DB type " + attributeDatabaseType);

			final Object value = mapToDBType(frame.data,
					attributeDatabaseType.getDbtype());

			if (TRACE_ENABLED)
				LOGGER.trace("^ converts to a(n) "
						+ value.getClass().getSimpleName() + " : " + value);

			if (attributeDatabaseType instanceof Partition) {
				if (TRACE_ENABLED)
					LOGGER.trace("^ and stored in a partition ...");

				final Partition p = (Partition) attributeDatabaseType;
				tuple = Trap.trapTuple(attributeDatabaseType.getName(), node,
						p.getValueColumn(), engine);
				tuple.addConstantValue(p.getValueColumn(), value);

			} else {
				tuple = Trap.trapTuple(attributeDatabaseType.getName(), node,
						"value", engine);
				tuple.addConstantValue("value", value);
			}

		} else {
			tuple = current.tuple;
		}

		if (attributeType instanceof ListType) {
			Tuple listTuple = null;

			if (TRACE_ENABLED)
				LOGGER.trace("" + parent.tuple + "." + attributeName + " += "
						+ tuple);

			final Value listValue = parent.tuple.getValue(attributeName);
			if (listValue == null) {
				// The attribute is for a list, but we don't have a list yet, so
				// we now create it.
				listTuple = Trap.trapList(attribute, node, "." + attributeName,
						engine);

				Trap.parentTupleToAttribute(parent.tuple, attribute, listTuple);

				// Keep track of the list on the tuple for future use.
				parent.tuple
						.addValue(new ReferenceValue(attributeName, listTuple));
			} else {
				listTuple = ((ReferenceValue) listValue).getTuple();
			}

			if (TRACE_ENABLED) {
				LOGGER.trace("^ " + parent.tuple + "." + attributeName + " == "
						+ listTuple);

				final int index = listTuple.getChildCount();
				LOGGER.trace("^ " + listTuple + "[" + index + "] == " + tuple);
			}

			Trap.parentListItem(attribute, listTuple, tuple, node, engine);

		} else {
			if (TRACE_ENABLED)
				LOGGER.trace("" + parent.tuple + "." + attributeName + " = "
						+ tuple);
			Trap.parentTupleToAttribute(parent.tuple, attribute, tuple);
		}
	}

	private Object mapToDBType(List<Data> data, DBType dbType) {
		final StringBuilder text = new StringBuilder();
		for (Data d : data)
			if (d instanceof Token)
				text.append(((Token) d).getText());

		switch (dbType) {
		case VARCHAR:
			return text.toString();
		case INT:
			try {
				return Integer.parseInt(text.toString());
			} catch (NumberFormatException e) {
				throw new CatastrophicError("Expected a number.", e);
			}
		default:
			throw new CatastrophicError(
					"Don't know how to map primitives with DB type '" + dbType
							+ "'.");
		}
	}

	@Override
	public String toString() {
		return "assign to ." + attributeName;
	}
}