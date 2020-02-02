package com.semmle.cobol.generator;

import static com.semmle.cobol.extractor.CobolExtractor.getType;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.extractor.CobolExtractor;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.ListType;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.mapping.runtime.TrapFile;
import com.semmle.cobol.mapping.values.PartitionedValue;
import com.semmle.cobol.mapping.values.ReferenceValue;
import com.semmle.cobol.mapping.values.ReferencedByTuple;
import com.semmle.cobol.mapping.values.TrapFileValue;
import com.semmle.cobol.mapping.values.TrapKeyedValue;
import com.semmle.cobol.population.CommonPopulator.ErrorContext;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Position;
import koopa.core.data.Replaced;
import koopa.core.data.Token;

/**
 * Utility class for trapping {@link Tuple}s in a (hopefully) readable way.
 */
public class Trap {

	private static final Logger LOGGER = LoggerFactory.getLogger(Trap.class);

	/**
	 * Find the matching {@link Tuple} in the {@link TrapFile} and return it if
	 * it exists.
	 */
	public static Tuple getExistingTuple(String typeName, Object subject,
			String topic, RuleEngine engine) {
		final TrapFile trapFile = engine.getTrapFile();
		return trapFile.getExistingTuple(typeName, subject, topic);
	}

	/**
	 * Trap a {@link Tuple} for the given {@link Node}, or return the existing
	 * one.
	 */
	public static Tuple trapTuple(String typeName, Node node, String topic,
			RuleEngine engine) {
		return trapTuple_(typeName, node, topic, engine);
	}

	/**
	 * Trap a {@link Tuple} for the given {@link Token}, or return the existing
	 * one.
	 */
	public static Tuple trapTuple(String typeName, Token token, String topic,
			RuleEngine engine) {
		return trapTuple_(typeName, token, topic, engine);
	}

	private static Tuple trapTuple_(String typeName, Object subject,
			String topic, RuleEngine engine) {
		// We ask the runtime for a tuple:
		final TrapFile trapFile = engine.getTrapFile();
		final Tuple tuple = trapFile.getTuple(typeName, subject, topic);

		// If we're trapping a tuple for a Partition, then we're done.
		if (getType(typeName) instanceof Partition)
			return tuple;

		// For other types, however, we do some extra work. We add "id"
		// and "kind" values, which are required by default by most
		// types.
		tuple.addIdValue(engine);
		tuple.addKindValue(engine);

		return tuple;
	}

	/**
	 * Trap an attribute {@link Tuple} for the given {@link Node}, or return the
	 * existing one.
	 */
	public static Tuple trapTuple(Attribute attribute, Node node,
			RuleEngine engine) {
		return trapTuple_(attribute, node, engine);
	}

	/**
	 * Trap an attribute {@link Tuple} for the given {@link Token}, or return
	 * the existing one.
	 */
	public static Tuple trapTuple(Attribute attribute, Token token,
			RuleEngine engine) {
		return trapTuple_(attribute, token, engine);
	}

	private static Tuple trapTuple_(Attribute attribute, Object subject,
			RuleEngine engine) {

		final Type attributeType = getType(attribute);
		return trapTuple_(attributeType.getName(), subject,
				"." + attribute.getName(), engine);
	}

	/**
	 * Parent one {@link Tuple} to the attribute of another.
	 */
	public static void parentTupleToAttribute(Tuple parent, Attribute attribute,
			Tuple tuple) {
		parentTupleToAttribute(parent, attribute.getName(),
				CobolExtractor.getType(attribute), attribute.getIndex(), tuple);
	}

	/**
	 * Parent one {@link Tuple} to the attribute of another.
	 */
	private static void parentTupleToAttribute(Tuple parent,
			String attributeName, final Type attributeType, int index,
			Tuple tuple) {

		final Type parentType = CobolExtractor.getType(parent);
		final Type tupleType = CobolExtractor.getType(tuple);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace(". (assign '" + attributeName + " ...) - parent "
					+ tuple + " to " + parent + ".");

		// Because gentools.py partitions primitive values:
		if (tupleType instanceof Partition) {
			final String parentColumn = ((Partition) tupleType)
					.getParentColumn();
			tuple.addParentValue(parent, parentColumn);
			// We need to track these so we can update their type if the
			// parent's type is updated...
			parent.addValue(new PartitionedValue(attributeName, tuple));

		} else {
			tuple.addParentValue(parent);
			parent.addValue(new ReferencedByTuple(attributeName, tuple));
		}

		if (!"_".equals(attributeName)) {
			if (!attributeType.isAssignableFrom(tupleType))
				throw new CatastrophicError(
						"Type mismatch. Can not assign tuple " + tuple
								+ " to attribute '" + parentType + "."
								+ attributeName + "' of type '" + attributeType
								+ "'.");

			tuple.addIndexValue(index);
		}
	}

	/**
	 * Create a {@link Tuple} for a list attribute.
	 * <p>
	 * This also traps the location info for that tuple, setting to position to
	 * the start of the given {@link Node}.
	 */
	public static Tuple trapList(Attribute attribute, Node node, String topic,
			RuleEngine engine) {

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("Creating new list instance for " + attribute);

		final Type attributeType = getType(attribute);

		final Tuple listTuple = Trap.trapTuple(attributeType.getName(), node,
				topic, engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("List starts at {}", node.start);

		// The initial location of a list matches the starting location
		// of the first element to be added.
		Trap.trapLocation(listTuple, node.start, node.start, engine);

		return listTuple;
	}

	/**
	 * Add a {@link Tuple} to a list {@link Tuple}.
	 * <p>
	 * This also updates the end position of the list to the end of the given
	 * node.
	 */
	public static void parentListItem(Attribute attribute, Tuple list,
			Tuple item, Node node, RuleEngine engine) {

		final int index = list.getChildCount();
		final Type attributeType = CobolExtractor.getType(list);
		final Type listItemType = ((ListType) attributeType).getItemType();

		parentTupleToAttribute(list, attribute.getName() + "[" + index + "]",
				listItemType, index, item);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("List now ends at {}", node.end);

		Trap.updateLocationEnd(list, node.end, engine);
	}

	/**
	 * Trap the location info for a node's tuple.
	 * <p>
	 * If the node was marked as {@linkplain Node#compilerGenerated} we trap
	 * that fact as well.
	 */
	public static Tuple trapLocation(final Tuple tuple, final Node node,
			RuleEngine engine) {

		if (node.compilerGenerated)
			addCompilerGeneratedTuple(tuple, node, engine);

		if (node.startToken != null) {
			final LinkedList<Position> startPositions = getStartPositions(
					node.startToken);
			final LinkedList<Position> endPositions = getEndPositions(
					node.endToken);

			Position start = null;
			Position end = null;
			while (firstPositionsMatch(startPositions, endPositions)) {
				start = startPositions.removeFirst();
				end = endPositions.removeFirst();
			}

			return trapLocation(tuple, start, end, engine);

		} else
			return trapLocation(tuple, node.start, node.end, engine);
	}

	/**
	 * Trap the location info for a given {@link Tuple} to the provided start
	 * and and {@link Position}s.
	 */
	public static Tuple trapLocation(final Tuple tuple, Position start,
			Position end, RuleEngine engine) {

		final Tuple locationsDefault = trapLocationsDefault(tuple, start, end,
				engine);

		final Object subject = tuple.getKey().subject;
		final String topic = tuple.getName() + ":" + tuple.getKey().topic;

		final Tuple hasLocation = Trap.trapTuple_("hasLocation", subject, topic,
				engine);

		hasLocation.addValue(new ReferenceValue("locatable", tuple));
		hasLocation.addValue(new ReferenceValue("location", locationsDefault));

		return locationsDefault;
	}

	/**
	 * Trap the locations default info for a given {@link Tuple} to the provided
	 * start and and {@link Position}s.
	 */
	public static Tuple trapLocationsDefault(final Tuple tuple, Position start,
			Position end, RuleEngine engine) {

		final Object subject = tuple.getKey().subject;
		final String topic = tuple.getName() + ":" + tuple.getKey().topic;

		final Tuple locationsDefault = Trap.trapTuple_("locations_default",
				subject, topic, engine);

		final String path = start.getResourceName();

		final int beginLine = start.getLinenumber();
		final int beginColumn = start.getPositionInLine();
		final int endLine = end.getLinenumber();
		final int endColumn = end.getPositionInLine();

		if (path != null)
			locationsDefault.addValue( //
					new TrapFileValue("file", engine.getTrapFile(), path));
		else
			locationsDefault.addValue( //
					new TrapKeyedValue("file", engine.getTrapFile(), "file"));

		locationsDefault.addConstantValue("beginLine", beginLine);
		locationsDefault.addConstantValue("beginColumn", beginColumn);
		locationsDefault.addConstantValue("endLine", endLine);
		locationsDefault.addConstantValue("endColumn", endColumn);

		return locationsDefault;
	}

	/**
	 * Update the end of the locations default info to the given
	 * {@link Position}.
	 */
	public static void updateLocationEnd(final Tuple tuple, Position end,
			RuleEngine engine) {

		final Object subject = tuple.getKey().subject;
		final String topic = tuple.getName() + ":" + tuple.getKey().topic;

		final Tuple locationsDefault = Trap
				.getExistingTuple("locations_default", subject, topic, engine);

		final int endLine = end.getLinenumber();
		final int endColumn = end.getPositionInLine();

		locationsDefault.addConstantValue("endLine", endLine);
		locationsDefault.addConstantValue("endColumn", endColumn);
	}

	private static void addCompilerGeneratedTuple(Tuple tuple, Node node,
			RuleEngine engine) {

		final Tuple locationsDefault = Trap.trapTuple_("compgenerated", node,
				tuple.getName(), engine);

		locationsDefault.addValue(tuple.getValue("id"));
	}

	/**
	 * This lists all starting positions for the given token, including the
	 * replaced positions, in the order of least specific to most specific. So
	 * if a token came from an included file, the position of the token in the
	 * file which was included will come after the position of the whatever
	 * token included that file.
	 */
	private static LinkedList<Position> getStartPositions(Token start) {
		final LinkedList<Position> startPositions = new LinkedList<Position>();

		startPositions.addFirst(start.getStart());

		Replaced r = start.getReplaced();
		while (r != null) {
			startPositions.addFirst(r.getStart());
			r = r.getContext();
		}

		return startPositions;
	}

	/**
	 * This lists all ending positions for the given token, including the
	 * replaced positions, in the order of least specific to most specific. So
	 * if a token came from an included file, the position of the token in the
	 * file which was included will come after the position of the whatever
	 * token included that file.
	 */
	private static LinkedList<Position> getEndPositions(Token end) {
		LinkedList<Position> endPositions = new LinkedList<Position>();

		endPositions.addFirst(end.getEnd());

		Replaced r = end.getReplaced();
		while (r != null) {
			endPositions.addFirst(r.getEnd());
			r = r.getContext();
		}

		return endPositions;
	}

	private static boolean firstPositionsMatch(
			LinkedList<Position> startPositions,
			LinkedList<Position> endPositions) {
		return !startPositions.isEmpty() && !endPositions.isEmpty()
				&& resourcesMatch(startPositions.getFirst().getResourceName(),
						endPositions.getFirst().getResourceName());
	}

	private static boolean resourcesMatch(String a, String b) {
		if (a == null)
			return b == null;
		else
			return a.equals(b);
	}

	public static void trapNumLines(final Tuple tuple, final Object subject,
			Tally tally, RuleEngine engine) {
		final Tuple numlines = Trap.trapTuple_("numlines", subject, null,
				engine);

		if (tuple == null)
			numlines.addValue(new TrapKeyedValue("element_id",
					engine.getTrapFile(), "file"));
		else
			numlines.addValue(new ReferenceValue("element_id", tuple, "id"));

		numlines.addConstantValue("num_lines", tally.lines);
		numlines.addConstantValue("num_code", tally.code);
		numlines.addConstantValue("num_comment", tally.comments);
		numlines.addConstantValue("num_water", tally.water);
	}

	public static Tuple trapError(ErrorContext ec, RuleEngine engine,
			Token token, String message) {

		LOGGER.error(token + " - " + message);

		final String topic = "" + message.hashCode();
		final Tuple error = Trap.trapTuple("error", token, topic, engine);

		if (message.length() > 900)
			message = message.substring(0, 896) + " ...";

		error.addConstantValue("message", message);
		error.addConstantValue("context", ec.id);

		Trap.trapLocation(error, token.getStart(), token.getEnd(), engine);

		return error;
	}
}
