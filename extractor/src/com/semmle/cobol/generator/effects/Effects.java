package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.generator.triggers.Triggers.PROGRAM_TEXT;
import static com.semmle.cobol.generator.triggers.Triggers.path;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.functions.ConstFn1;
import com.semmle.cobol.generator.functions.Fn1;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.generator.triggers.TriggerDefinition;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.mapping.runtime.TrapFile;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;

/**
 * Facade class for defining {@link Effect}s.
 */
public class Effects {

	public static final Effect MAY_BE_OMITTED = new MayBeOmittedEffect();
	public static final Effect RETURN = new ReturnEffect();
	public static final Effect ASSIGN_EVENT_NODE_TO_FRAME = new AssignEventNodeToFrameEffect();
	public static final Effect LOCATION = new LocationEffect();
	public static final Effect NUMLINES = new NumLinesEffect();
	public static final Effect PICTURE = new PictureEffect();
	public static final Effect CFLOW = new CFlowEffect();

	/**
	 * An {@link Effect} which applies the given {@link Effect} in a
	 * sub-{@link Frame} of the active one.
	 */
	public static Effect sub(Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				effect.apply(event, frame.push(), engine);
			}

			@Override
			public String toString() {
				return "sub";
			}
		};
	}

	/**
	 * An {@link Effect} which applies the given {@link Effect} in a
	 * sub-{@link Frame} of the given one.
	 * <p>
	 * If the given frame is <code>null</code> then the given effect is not
	 * given a {@link Frame}.
	 */
	private static Effect sub(Frame frame, Effect effect) {
		if (frame == null)
			return closure(null, effect);
		else
			return closure(frame.push(), effect);
	}

	/**
	 * An {@link Effect} which applies the given {@link Effect} in a
	 * {@link Frame} of its own.
	 */
	public static Effect closure(Effect effect) {
		return closure(new Frame(), effect);
	}

	/**
	 * An {@link Effect} which applies the given {@link Effect} in the given
	 * {@link Frame}.
	 */
	public static Effect closure(Frame closure, Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				effect.apply(event, closure, engine);
			}

			@Override
			public String toString() {
				return "{| " + effect + " |}";
			}
		};
	}

	/**
	 * An {@link Effect} which adds the given {@link TriggerDefinition} and
	 * given {@link Effect} to the {@link RuleEngine}.
	 * <p>
	 * This lets active rules add more rules when certain conditions have been
	 * met.
	 */
	public static Effect on(TriggerDefinition def, Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				engine.add(def.getTriggerFor(event, engine),
						sub(frame, effect));
			}

			@Override
			public String toString() {
				return "on " + def + " do " + effect;
			}
		};
	}

	/**
	 * An {@link Effect} which will tell the {@link RuleEngine} to run the given
	 * {@link Effect} at the end of the subtree which is active when this effect
	 * was run.
	 * 
	 * @see RuleEngine#atEnd(Effect)
	 */
	public static Effect atEnd(Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				engine.atEnd(closure(frame, effect));
			}

			@Override
			public String toString() {
				return "at end do " + effect;
			}
		};
	}

	/**
	 * An {@link Effect} which will tell the {@link RuleEngine} to run the given
	 * {@link Effect} at the end of the subtree which is active when this effect
	 * was run, after all {@link #atEnd(Effect)}s have run.
	 * 
	 * @see RuleEngine#andFinally(Effect)
	 */
	public static Effect andFinally(Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				engine.andFinally(closure(frame, effect));
			}

			@Override
			public String toString() {
				return "and finally do " + effect;
			}
		};
	}

	/**
	 * An {@link Effect} which will collect all {@link Data} matching the given
	 * {@link TriggerDefinition} into the {@link Frame} on which it was
	 * activated.
	 */
	public static Effect collect(TriggerDefinition def) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final List<Data> data = new LinkedList<>();
				frame.data = data;

				on(def, new Effect() {
					@Override
					public void apply(Event e, Frame frame, RuleEngine engine) {
						data.add(e.data);
					}

					@Override
					public String toString() {
						return "collect " + def + " into " + data;
					}
				}).apply(event, frame, engine);
			}

			@Override
			public String toString() {
				return "collect " + def;
			}
		};
	}

	/**
	 * An {@link Effect} which applies all given effects in order.
	 * <p>
	 * If only a single {@link Effect} was given, this will return that effect.
	 * <p>
	 * If no {@link Effect} was given this will throw a
	 * {@link IllegalArgumentException}.
	 */
	public static Effect all(final Effect... effects) {
		if (effects == null || effects.length == 0)
			throw new IllegalArgumentException(
					"At least one Effect is needed.");
		else if (effects.length == 1)
			return effects[0];
		else
			return new Effect() {
				@Override
				public void apply(Event event, Frame frame, RuleEngine engine) {
					for (Effect effect : effects)
						effect.apply(event, frame, engine);
				}

				@Override
				public String toString() {
					return effects[0] + " and ...";
				}
			};
	}

	/**
	 * An {@link Effect} which creates a {@link Tuple} of the given type.
	 * <p>
	 * This also sets up the {@link #LOCATION} effect at the end, so all tuples
	 * get the required location information.
	 */
	public static Effect createTuple(String typeName) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				frame.node = event.getNode();
				frame.tuple = Trap.trapTuple(typeName, event.getNode(), null,
						engine);

				engine.atEnd(closure(frame, LOCATION));
			}

			@Override
			public String toString() {
				return "map @" + typeName;
			}
		};
	}

	/**
	 * An {@link Effect} which overrides the type of the {@link Tuple} found on
	 * the {@link Frame}. The new type must be compatible with the old one for
	 * this to work.
	 */
	public static Effect overrideTupleType(String typeName) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final Frame oldFrame = frame.findAncestorWithTuple();
				final TrapFile trapFile = engine.getTrapFile();
				trapFile.overrideTupleType(oldFrame.tuple, typeName);
			}

			@Override
			public String toString() {
				return "set tuple type to @" + typeName;
			}
		};
	}

	/**
	 * An {@link Effect} which assigns the result of the current frame to the
	 * given attribute of the ancestor {@link Tuple}.
	 */
	public static Effect assignTo(String attributeName) {
		return new AssignToEffect(attributeName);
	}

	/**
	 * An {@link Effect} which creates a binary tree of tuples for matching
	 * nodes, where the type for all binary tuples is the given one.
	 */
	public static Effect foldl(TriggerDefinition def, Effect createTuple,
			String typeName) {
		return foldl(def, createTuple, new ConstFn1<Data, String>(typeName));
	}

	/**
	 * {@link Effect} which creates a binary tree of tuples for matching nodes,
	 * where the type for all binary tuples is decided by the given {@link Fn1}.
	 */
	public static Effect foldl(TriggerDefinition def, Effect createTuple,
			Fn1<Data, String> ntt) {
		return new FoldLEffect(def, createTuple, ntt);
	}

	/**
	 * An {@link Effect} which sets up the parent-child links between data
	 * records based on their type and level number.
	 */
	public static Effect nestedRecordStructure(String attributeName,
			TriggerDefinition def, RuleSet rules) {
		return new NestedRecordStructureEffect(attributeName, def, rules);
	}

	/**
	 * An {@link Effect} which applies the given {@link Effect} when no
	 * {@link Tuple} is found on the {@link Frame}.
	 */
	public static Effect otherwise(Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				if (frame.tuple == null)
					effect.apply(event, frame, engine);
			}

			@Override
			public String toString() {
				return "otherwise " + effect;
			}
		};
	}

	/**
	 * An {@link Effect} which applies the given {@link Effect} when the
	 * {@link Tuple} found on the {@link Frame} has no parent.
	 */
	public static Effect ifNotParented(Effect effect) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				if (frame.tuple != null && !frame.tuple.hasValue("parent"))
					effect.apply(event, frame, engine);
			}

			@Override
			public String toString() {
				return "if not parented then " + effect;
			}
		};
	}

	/**
	 * An {@link Effect} which maps the first child node, and {@link #RETURN}s
	 * the value at the end.
	 */
	public static Effect returnFirstChild(RuleSet rules) {
		return on(path("<>[1]"), all( //
				rules.applyMatchingRule(), //
				atEnd(RETURN) //
		));
	}

	/**
	 * Same as {@link #returnFirstChild(RuleSet)}, but overrides the node
	 * returned to the one on the activated {@link Frame}.
	 */
	public static Effect returnFirstChildWithThisNode(RuleSet rules) {
		return all( //
				on(path("<>[1]"), all( //
						rules.applyMatchingRule(), //
						atEnd(RETURN) //
				)), //
				atEnd(ASSIGN_EVENT_NODE_TO_FRAME) //
		);
	}

	/**
	 * Same as {@link #returnFirstChild(RuleSet)}, but sets the
	 * {@link Frame#mayBeOmitted} flag.
	 */
	public static Effect maybeReturnFirstChild(RuleSet rules) {
		return all(MAY_BE_OMITTED, returnFirstChild(rules));
	}

	/**
	 * An {@link Effect} which maps the given path, and {@link #RETURN}s the
	 * value at the end.
	 */
	public static Effect returnChild(RuleSet rules, String path) {
		return on(path(path), all( //
				rules.applyMatchingRule(), //
				atEnd(RETURN) //
		));
	}

	/**
	 * An {@link Effect} which maps the first child node, and {@link #RETURN}s
	 * the value at the end.
	 * <p>
	 * If no mapping for the node is available it will be mapped using the given
	 * rule instead.
	 */
	public static Effect returnFirstChild(RuleSet rules,
			Start defaultRuleName) {
		return on(path("<>[1]"), all( //
				rules.applyMatchingRule(defaultRuleName), //
				atEnd(RETURN) //
		));
	}

	/**
	 * An {@link Effect} which creates a {@link Tuple} for a given literal type,
	 * captures all program text, and assigns it to the "value" attribute of the
	 * literal tuple.
	 */
	public static Effect literalWithValue(String typeName) {
		return all( //
				createTuple(typeName), //
				sub(all(collect(PROGRAM_TEXT), //
						atEnd(assignTo("value")))) //
		);
	}

	/**
	 * An {@link Effect} which maps any matches for the given
	 * {@link TriggerDefinition} and at the end assigns it to the given
	 * attribute.
	 * <p>
	 * If no mapping for the node is available it will be mapped using the given
	 * rule instead.
	 */
	public static Effect setAttribute(String attributeName,
			TriggerDefinition triggerDefinition, RuleSet rules,
			Start defaultRule) {
		return on(triggerDefinition, //
				all(rules.applyMatchingRule(defaultRule), //
						atEnd(assignTo(attributeName))));
	}

	/**
	 * An {@link Effect} which maps any matches for the given
	 * {@link TriggerDefinition} and at the end assigns it to the given
	 * attribute.
	 */
	public static Effect setAttribute(String attributeName,
			TriggerDefinition triggerDefinition, RuleSet rules) {
		return on(triggerDefinition, //
				all(rules.applyMatchingRule(), //
						atEnd(assignTo(attributeName))));
	}

	/**
	 * An {@link Effect} which maps any matches for the given
	 * {@link TriggerDefinition} using the given rule, and at the end assigns it
	 * to the given attribute.
	 */
	public static Effect setAttributeAs(String attributeName,
			TriggerDefinition triggerDefinition, RuleSet rules,
			Start ruleName) {
		return on(triggerDefinition, //
				all(rules.applyRule(ruleName), //
						atEnd(assignTo(attributeName))));
	}

	/**
	 * An {@link Effect} which collects all program text on matching
	 * {@link TriggerDefinition}s, and at the end assigns it to the given
	 * attribute.
	 */
	public static Effect setAttributeToProgramText(String attributeName,
			TriggerDefinition triggerDefinition) {
		return on(triggerDefinition, //
				sub(all(collect(PROGRAM_TEXT), //
						atEnd(assignTo(attributeName)))));
	}

	/**
	 * An {@link Effect} which collects all program text, and at the end assigns
	 * it to the given attribute.
	 */
	public static Effect setAttributeToProgramText(String attributeName) {
		return sub(all(collect(PROGRAM_TEXT), //
				atEnd(assignTo(attributeName))));
	}

	/**
	 * An {@link Effect} which will print out a given message, useful for
	 * testing.
	 * <p>
	 * Any occurence of <code>%</code> will be replaced with info from the
	 * event. Any occurence of <code>$</code> will be replaced with info from
	 * the frame.
	 */
	public static final Effect print(final String message) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final String actualMessage = message
						.replaceAll("%", event.toString())
						.replaceAll("$", frame.toString());
				System.out.println("} " + actualMessage);
			}

			@Override
			public String toString() {
				return "print " + message;
			}
		};
	}
}
