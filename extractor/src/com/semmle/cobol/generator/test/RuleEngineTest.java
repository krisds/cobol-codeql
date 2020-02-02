package com.semmle.cobol.generator.test;

import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.closure;
import static com.semmle.cobol.generator.effects.Effects.collect;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.test.StreamUtil.token;
import static com.semmle.cobol.generator.test.StreamUtil.tree;
import static com.semmle.cobol.generator.triggers.Triggers.ANY;
import static com.semmle.cobol.generator.triggers.Triggers.END;
import static com.semmle.cobol.generator.triggers.Triggers.end;
import static com.semmle.cobol.generator.triggers.Triggers.isLast;
import static com.semmle.cobol.generator.triggers.Triggers.or;
import static com.semmle.cobol.generator.triggers.Triggers.path;
import static com.semmle.cobol.generator.triggers.Triggers.root;
import static com.semmle.cobol.generator.triggers.Triggers.start;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.generator.triggers.Triggers;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;

/**
 * Unit tests which check the behavior of {@link RuleEngine}s.
 */
public class RuleEngineTest {

	@Test
	public void basicTrackingWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// This logs all events, and should give a complete trace of the tree.
		engine.add(ANY, log);

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log, "((T)T((T)))", "12221233321", "xaaaxbaaabx");
	}

	@Test
	public void startTrigger() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// This logs only on <a> events.
		engine.add(start(Start.on("cobol", "a")), log);

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log, "((", "23", "aa");
	}

	@Test
	public void endTrigger() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// This logs only on </a> events.
		engine.add(end(End.on("cobol", "a")), log);

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log, "))", "23", "aa");
	}

	@Test
	public void basicAtEndWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		final Log log3 = new Log(3);

		// This logs at the end of any node.
		engine.add(path("**/<>"), atEnd(log1));
		// This logs at the end of any program text token.
		engine.add(path("**/@PROGRAM_TEXT"), atEnd(log2));
		// This logs at the end of any end.
		engine.add(END, atEnd(log3));

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "))))", "2321", "aabx");
		assertLog(log2, ")))", "231", "aax");
		assertLog(log3, "))))", "2321", "aabx");
	}

	@Test
	public void childPaths() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		// This logs child <x>'s.
		engine.add(path("<x>"), log1);
		// This logs child <a>'s.
		engine.add(path("<a>"), log2);
		// Note that in both cases "child" is relative to the current state of
		// the engine. Which means it will be equivalent to "root" at this
		// point.

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "(", "1", "x");
		assertLog(log2, "", "", "");
	}

	@Test
	public void descendantPaths() {
		final RuleEngine engine = new RuleEngine(null);

		Log log1 = new Log(1);
		Log log2 = new Log(2);
		// This logs descendant <x>'s.
		engine.add(path("**/<x>"), log1);
		// This logs descendant <a>'s.
		engine.add(path("**/<a>"), log2);
		// Note that in both cases "descendant" is relative to the current state
		// of the engine. Which means it will be equivalent to all start nodes
		// in the tree.

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "(", "1", "x");
		assertLog(log2, "((", "23", "aa");
	}

	@Test
	public void combinedPaths() {
		final RuleEngine engine = new RuleEngine(null);

		final Log logXA = new Log();
		// This logs any <a>, which is a child of <x>.
		engine.add(path("<x>/<a>"), closure(logXA));

		tree("x", //
				tree("a", token("t")), //
				token("t"), //
				tree("b", tree("a", token("t"))) //
		).streamInto(engine);
		engine.done();

		assertLog(logXA, "(", "2", "a");
	}

	@Test
	public void indexingWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		final Log log3 = new Log(3);

		// These log any child for the first, second, and third node
		// (respectively) which are themselves children of an <x>.
		engine.add(path("<x>/<>[1]/<>"), log1);
		engine.add(path("<x>/<>[2]/<>"), log2);
		engine.add(path("<x>/<>[3]/<>"), log3);

		tree("x", //
				tree("a", tree("A")), // < [1]
				tree("b", tree("B")), // < [2]
				tree("c", tree("C")), // < [3]
				tree("d", tree("D")) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "(", "3", "A");
		assertLog(log2, "(", "3", "B");
		assertLog(log3, "(", "3", "C");
	}

	@Test
	public void namedIndexingWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		final Log log3 = new Log(3);
		final Log log4 = new Log(4);
		final Log log5 = new Log(5);

		// These log any child for the first, second, and third <n>
		// (respectively) which are themselves children of an <x>.
		engine.add(path("<x>/<n>[1]/<>"), closure(log1));
		engine.add(path("<x>/<n>[2]/<>"), closure(log2));
		engine.add(path("<x>/<n>[3]/<>"), closure(log3));

		// The following depend on preconditions. The first logs any child for
		// the last <n> which is itself a child of <x>.
		engine.add(path("<x>/<n>[-1]/<>"), closure(log4));
		// The second logs any child for the last node which is itself a child
		// of <x>.
		engine.add(path("<x>/<>[-1]/<>"), closure(log5));

		tree("x", //
				tree("z", tree("Z")), //
				tree("n", tree("A")), // <n>[1]
				tree("z", tree("Z")), //
				tree("n", tree("B")), // <n>[2]
				tree("z", tree("Z")), //
				tree("n", tree("C")), // <n>[3]
				tree("z", tree("Z")), //
				tree("n", tree("D")), // <n>[-1]
				tree("z", tree("Z")) // <>[-1]
		).streamInto(engine);
		engine.done();

		assertLog(log1, "(", "3", "A");
		assertLog(log2, "(", "3", "B");
		assertLog(log3, "(", "3", "C");
		assertLog(log4, "(", "3", "D");
		assertLog(log5, "(", "3", "Z");
	}

	@Test
	public void textMatchingWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final List<String> textMatches = new LinkedList<>();
		// For any node which is a child of <x>, collect all program text and,
		// at the end of that node, concatenate the values and store for
		// testing.
		engine.add(path("<x>/<>"), closure(all( //
				collect(Triggers.PROGRAM_TEXT), //
				atEnd(new Effect() {
					@Override
					public void apply(Event event, Frame frame,
							RuleEngine engine) {
						StringBuilder b = new StringBuilder();
						for (Data d : frame.data)
							b.append(((Token) d).getText());
						textMatches.add(b.toString());
					}
				}))));

		tree("x", //
				tree("a", token("HELLO"), token("_"), token("WORLD")), //
				tree("b", token("COBOL"), token(" "), token("rules")) //
		).streamInto(engine);
		engine.done();

		assertEquals(2, textMatches.size());
		assertEquals("HELLO_WORLD", textMatches.get(0));
		assertEquals("COBOL rules", textMatches.get(1));
	}

	@Test
	public void ruleMatchingWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final RuleSet rules = new RuleSet();

		final Trace trace = new Trace();

		// Map an <a> by marking "a" in the trace.
		rules.define("<a>", trace.mark("a"));
		// Mark a "b" instead, if the <a> is a child of a <b>.
		rules.define("<b>/<a>", trace.mark("b"));
		// Mark a "c" instead, if the <a> is a child of a <b> which is itself a
		// child of <c>.
		rules.define("<c>/<b>/<a>", trace.mark("c"));

		// Apply the rule for any <a> seen in the tree.
		engine.add(path("**/<a>"), closure(rules.applyMatchingRule()));

		tree("x", //
				tree("a", tree("A")), //
				tree("b", tree("a", tree("B"))), //
				tree("c", tree("b", tree("a", tree("C")))) //
		).streamInto(engine);
		engine.done();

		assertEquals("abc", trace.getTrace());
	}

	@Test
	public void defaultRuleWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final RuleSet rules = new RuleSet();

		final Trace trace = new Trace();
		// Mark a "b" when mapping a <b>.
		rules.define("<b>", trace.mark("b"));
		// Mark a "z" when mapping a <z>.
		rules.define("<z>", trace.mark("z"));

		// Map all nodes, defaulting to the rule for <z> if no specific rule is
		// found.
		engine.add(path("**/<>"),
				closure(rules.applyMatchingRule(Start.on("cobol", "z"))));

		tree("x", //
				tree("a", token("A")), //
				tree("b", token("B")), //
				tree("c", token("C")) //
		).streamInto(engine);
		engine.done();

		assertEquals("zzbz", trace.getTrace());
	}

	@Test
	public void ruleOverrideWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final RuleSet rules = new RuleSet();

		final Trace trace = new Trace();
		// Mark a "a" when mapping a <a>.
		rules.define("<a>", trace.mark("a"));
		// Mark a "b" when mapping a <b>.
		rules.define("<b>", trace.mark("b"));
		// Mark a "c" when mapping a <c>.
		rules.define("<c>", trace.mark("c"));
		// Mark a "z" when mapping a <z>.
		rules.define("<z>", trace.mark("z"));

		// Map all nodes using the rule for <z>.
		engine.add(path("**/<>"),
				closure(rules.applyRule(Start.on("cobol", "z"))));

		tree("x", //
				tree("a", token("A")), //
				tree("b", token("B")), //
				tree("c", token("C")) //
		).streamInto(engine);
		engine.done();

		assertEquals("zzzz", trace.getTrace());
	}

	@Test
	public void hasChildWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		final Log log3 = new Log(3);

		// Log any event under a child <a>, but only for <a>s which have a child
		// <p>.
		engine.add(path("<a>[<p>]"), on(ANY, log1));
		// Log any event under an <a> which is a child of an <x>, but only for
		// <a>s which have a child <p>.
		engine.add(path("<x>/<a>[<p>]"), on(ANY, log2));
		// Log any event under any <a>, but only for <a>s which have a child
		// <p>.
		engine.add(path("**/<a>[<p>]"), on(ANY, log3));

		tree("x", //
				tree("a", token("A")), //
				tree("a", tree("p", token("P"))), //
				tree("a", tree("q", token("Q"))), //
				tree("z", tree("a", tree("p", token("P")))) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "", "", "");
		assertLog(log2, "(T)", "333", "ppp");
		assertLog(log3, "(T)(T)", "333444", "pppppp");
	}

	@Test
	public void isLastWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);
		final Log log3 = new Log(3);

		// Log any event under the last <a>.
		engine.add(isLast(Start.on("cobol", "a")), on(ANY, log1));
		// Log any event under the last <p>.
		engine.add(isLast(Start.on("cobol", "p")), on(ANY, log2));
		// Log any event under the last <q>.
		engine.add(isLast(Start.on("cobol", "q")), on(ANY, log3));

		tree("x", //
				tree("a", token("A")), //
				tree("a"), //
				tree("p", token("P")), //
				tree("a", tree("p", token("P"))), //
				tree("q", tree("q", token("Q"))), //
				tree("z", tree("a", tree("r", token("R")))) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "(T)", "444", "rrr");
		assertLog(log2, "T", "3", "p");
		assertLog(log3, "T", "3", "q");
	}

	@Test
	public void orWorks() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log1 = new Log(1);
		final Log log2 = new Log(2);

		// Log any event under an <a> or <c> which are themselves under an <x>.
		engine.add(path("<x>"), //
				on(or(path("<a>"), path("<c>")), //
						on(ANY, log1)));
		// Log any event under a <b> or <d> which are themselves children of an
		// <x>.
		engine.add(or(path("<x>/<b>"), path("<x>/<d>")), //
				on(ANY, log2));

		tree("x", //
				tree("a", token("A")), //
				tree("b", token("B")), //
				tree("c", token("C")), //
				tree("d", token("D")) //
		).streamInto(engine);
		engine.done();

		assertLog(log1, "TT", "22", "ac");
		assertLog(log2, "TT", "22", "bd");
	}

	@Test
	public void nextPath() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// Log any node which follows a <b> which is a child of an <a> which is
		// a child of an <x>.
		engine.add(path("<x>/<a>/<b>/../<>"), closure(log));

		tree("x", //
				tree("a", //
						tree("a")), //
				tree("a", //
						tree("b")), //
				tree("a", //
						tree("b"), //
						tree("1")), //
				tree("a", //
						tree("b"), //
						tree("2"), //
						tree("3")), //
				tree("a", //
						tree("4"), //
						tree("b")), //
				tree("a", //
						tree("b", //
								tree("5"))) //
		).streamInto(engine);
		engine.done();

		assertLog(log, "(((", "333", "123");
	}

	@Test
	public void rootPath() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// Log any <y> which appears as a root.
		engine.add(root(start(Start.on("cobol", "y"))), closure(log));

		tree("x", tree("y")).streamInto(engine);
		token("A").streamInto(engine);
		tree("y", tree("y")).streamInto(engine);
		engine.done();

		assertLog(log, "(", "1", "y");
	}

	@Test
	public void absolutePaths() {
		final RuleEngine engine = new RuleEngine(null);

		final Log log = new Log();
		// When we have seen an <a> under an <x>, log any node which is seen
		// under a root <y>.
		engine.add(path("<x>/<a>"), on(path("/<y>/<>"), closure(log)));

		tree("y", tree("1")).streamInto(engine);
		tree("x", tree("a")).streamInto(engine);
		tree("y", tree("2")).streamInto(engine);
		tree("y", tree("3")).streamInto(engine);
		engine.done();

		assertLog(log, "((", "22", "23");
	}

	// ------------------------------------------------------------------------

	private static void assertLog(Log log, String types, String depth,
			String nodes) {
		assertEquals(types, log.getTypes());
		assertEquals(depth, log.getDepth());
		assertEquals(nodes, log.getNodes());
	}
}
