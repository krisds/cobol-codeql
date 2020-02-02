package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.extractor.CobolExtractor.getAttribute;
import static com.semmle.cobol.extractor.CobolExtractor.getType;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.triggers.Triggers.path;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.generator.triggers.TriggerDefinition;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.Type;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;

/**
 * An {@link Effect} which sets up the parent-child links between data records
 * based on their type and level number.
 */
class NestedRecordStructureEffect implements Effect {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NestedRecordStructureEffect.class);

	private final String attributeName;
	private final TriggerDefinition def;
	private final RuleSet rules;

	public NestedRecordStructureEffect(String attributeName,
			TriggerDefinition def, RuleSet rules) {
		this.attributeName = attributeName;
		this.def = def;
		this.rules = rules;
	}

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final RecordStructure structure = new RecordStructure();

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("nested record structure");

		all( //
				on(path("<>"), addNodeTo(structure)), //

				on(def, all( //
						rules.applyMatchingRule(), //
						atEnd(addTupleTo(structure)) //
				)), //

				atEnd(nest(structure, attributeName)) //
		).apply(event, frame, engine);
	}

	private static Effect addNodeTo(RecordStructure structure) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final Node node = event.getNode();
				structure.records.add(new Record(-1, null, node));
			}

			@Override
			public String toString() {
				return "collect nodes";
			}
		};
	}

	private Effect addTupleTo(RecordStructure structure) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final Tuple tuple = frame.tuple;
				structure.records.getLast().tuple = tuple;
			}

			@Override
			public String toString() {
				return "collect records";
			}
		};
	}

	private static Effect nest(RecordStructure structure,
			String rootAttributeName) {
		return new Effect() {
			private static final String DATA = "data_description_entry";
			private static final String CONSTANT = "constant_entry";
			private static final String FILE = "file_description_entry";
			private static final String SORT_MERGE_FILE = "sort_merge_file_description_entry";
			private static final String REPORT = "report_description_entry";
			private static final String REPORT_GROUP = "report_group_description_entry";
			private static final String SCREEN = "screen_description_entry";
			private static final String COPY = "copy";

			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("nesting {} records",
							structure.records.size());

				final Frame rootFrame = frame.findFrameWithTuple();

				// The root of the section we're working on. Every record will
				// become a descendant of this one.
				final Record root = new Record(-1, rootFrame.tuple,
						rootFrame.node);

				// The stack of records, maintained while processing each one in
				// turn.
				final Stack<Record> nested = new Stack<Record>();
				nested.push(root);

				for (Record r : structure.records) {
					if (r.tuple == null) {
						if (LOGGER.isTraceEnabled())
							LOGGER.trace("not an entry : " + r.node);

						collapse(nested);
						continue;
					}

					final Data d = r.node.data;
					r.level = getLevelNumber(r.tuple);

					LOGGER.trace("LEVEL {} {} {}", r.level, r.tuple, d);

					final String name = r.tuple.getName();
					final String attributeName = nested.size() == 1
							? rootAttributeName
							: "nested_entries";

					if (name.equals(DATA)) {
						if (r.level >= 1 && r.level <= 49) {
							collapse(nested, r.level);
							nested.peek().add(r, engine, attributeName);
							nested.push(r);

						} else if (r.level == 66) {
							// Level 66 is for renaming items.
							collapse(nested);
							nested.peek().add(r, engine, attributeName);
							// We do not nested.push(r);

						} else if (r.level == 77) {
							// Level 77 is for stand-alone item.
							collapse(nested);
							nested.peek().add(r, engine, attributeName);
							nested.push(r);

						} else if (r.level == 88) {
							// Level 88 represents conditions.
							// We do not "collapse(nested, r.level)".
							nested.peek().add(r, engine, attributeName);
							// We do not nested.push(r);

						} else
							throw new CatastrophicError(
									"Case not covered: nesting of level "
											+ r.level + " " + name);

					} else if (name.equals(CONSTANT)) {
						if (r.level == 1) {
							collapse(nested);
							nested.peek().add(r, engine, attributeName);
							// We do not nested.push(r);

						} else if (r.level == 78) {
							// We do not "collapse(nested, r.level)".
							nested.peek().add(r, engine, attributeName);
							// We do not nested.push(r);

						} else
							throw new CatastrophicError(
									"Case not covered: nesting of level "
											+ r.level + " " + name);

					} else if (name.equals(FILE) || name.equals(SORT_MERGE_FILE)
							|| name.equals(REPORT)) {
						collapse(nested);
						nested.peek().add(r, engine, attributeName);
						nested.push(r);

					} else if (name.equals(REPORT_GROUP)
							|| name.equals(SCREEN)) {
						collapse(nested, r.level);
						nested.peek().add(r, engine, attributeName);
						nested.push(r);

					} else if (name.equals(COPY)) {
						convertCopyIntoAnEntry(r, engine);
						// We do not "collapse(nested, r.level)".
						nested.peek().add(r, engine, attributeName);
						nested.push(r);

					} else
						throw new CatastrophicError(
								"Case not covered: nesting of level " + r.level
										+ " " + name);
				}
			}

			private int getLevelNumber(Tuple tuple) {
				final String name = tuple.getName();
				// These elements do not define a level number. But as they're
				// at the top level we can say that they have a level number 0.
				if (name.equals(FILE) || name.equals(SORT_MERGE_FILE)
						|| name.equals(REPORT))
					return 0;

				// Level numbers are partitioned...
				final Integer level = tuple.getIntegerAttribute("level_number");
				return level == null ? -1 : level.intValue();
			}

			private void convertCopyIntoAnEntry(Record r, RuleEngine engine) {
				final Tuple entry = Trap.trapTuple("copy_entry", r.node, null,
						engine);
				final Type entryType = getType(entry);
				final Attribute attribute = getAttribute(entryType, "copy");
				Trap.parentTupleToAttribute(entry, attribute, r.tuple);
				r.tuple = entry;
			}

			@Override
			public String toString() {
				return "nest record structure";
			}
		};
	}

	private static class RecordStructure {
		public final Deque<Record> records = new LinkedList<>();
	}

	@Override
	public String toString() {
		return "nested record structure on " + def;
	}

	private static class Record {
		public int level;
		public Tuple tuple;
		public Node node;
		public Tuple nestedList;

		private Attribute nestedEntries;

		public Record(int level, Tuple tuple, Node node) {
			this.level = level;
			this.tuple = tuple;
			this.node = node;
		}

		public void add(Record r, RuleEngine engine, String attributeName) {
			if (nestedList == null) {
				final Type type = getType(tuple);
				nestedEntries = getAttribute(type, attributeName);

				nestedList = Trap.trapList(nestedEntries, node, null, engine);

				Trap.parentTupleToAttribute(tuple, nestedEntries, nestedList);
			}

			if (LOGGER.isTraceEnabled()) {
				final int index = nestedList.getChildCount();
				LOGGER.trace("^ " + nestedList + "[" + index + "] == " + tuple);
			}

			Trap.parentListItem(nestedEntries, nestedList, r.tuple, r.node,
					engine);
		}

		@Override
		public String toString() {
			return "level " + level + " " + tuple;
		}
	}

	/**
	 * This reduces a stack until there is no more than a single element in it.
	 */
	private static void collapse(Stack<Record> records) {
		while (records.size() > 1)
			records.pop();
	}

	/**
	 * This reduces a stack until its top record has a level number which is
	 * below the given one.
	 * <p>
	 * Records with a negative level will get removed when encountered while
	 * collapsing.
	 * <p>
	 * The root record will never be removed.
	 */
	private static void collapse(Stack<Record> records, int level) {
		while (records.size() > 1
				&& (records.peek().level >= level || records.peek().level < 0))
			records.pop();
	}
}
