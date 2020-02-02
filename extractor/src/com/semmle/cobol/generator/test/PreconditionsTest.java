package com.semmle.cobol.generator.test;

import static com.semmle.cobol.generator.test.StreamUtil.token;
import static com.semmle.cobol.generator.test.StreamUtil.tree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.semmle.cobol.generator.preconditions.HasChild;
import com.semmle.cobol.generator.preconditions.IsLast;
import com.semmle.cobol.generator.preconditions.IsLastChild;
import com.semmle.cobol.generator.preconditions.Preconditions;

import koopa.core.data.markers.Start;
import koopa.core.targets.ListTarget;

/**
 * Unit tests which check the behavior of {@link Preconditions}.
 */
public class PreconditionsTest {

	@Test
	public void passThroughStreamingWorks() {
		final ListTarget list = new ListTarget();
		final Preconditions preconditions = new Preconditions(list);

		tree("a", //
				token("A"), //
				tree("b", token("B")), //
				token("C") //
		).streamInto(preconditions);

		// Without any preconditions set, we just get all the original events.
		// Which is 2 for every tree, and 1 for every token.
		assertEquals(2 * 2 + 3, list.size());
	}

	@Test
	public void hasChild() {
		final ListTarget list = new ListTarget();
		final Preconditions preconditions = new Preconditions(list);
		// We set up a (log of a) precondition which flags <a>s which have a <b>
		// child.
		final PreconditionLog log = new PreconditionLog(
				new HasChild(Start.on("cobol", "a"), Start.on("cobol", "b")));
		preconditions.add(log);

		tree("x", //
				tree("a"), //
				tree("a", tree("b")), //
				tree("a", tree("c")) //
		).streamInto(preconditions);
		preconditions.done();

		// We see 2 events for every tree, no events for nodes (there were
		// none), and one matching precondition.
		assertEquals(6 * 2 + 0 + 1, list.size());
		// There are three <a>s, but the precondition only applies to the second
		// one.
		assertLog(log, "FTF");
	}

	@Test
	public void isLastChild() {
		final ListTarget list = new ListTarget();
		final Preconditions preconditions = new Preconditions(list);

		// This sets up a (log of a) precondition which flags any <a> which is
		// the last child among its siblings.
		final PreconditionLog logA = new PreconditionLog(
				new IsLastChild(Start.on("cobol", "a")));
		preconditions.add(logA);

		// This sets up a (log of a) precondition which flags any node which is
		// the last child among its siblings.
		final PreconditionLog logAny = new PreconditionLog(
				new IsLastChild(null));
		preconditions.add(logAny);

		tree("x", //
				tree("a"), //
				tree("a", tree("a")), //
				tree("a", tree("a"), tree("a")), //
				tree("z") //
		).streamInto(preconditions);
		preconditions.done();

		// We see 2 events for every tree, no events for nodes (there are none),
		// 3 matching preconditions for last child <a>s, and 4 matching
		// preconditions for last child nodes.
		assertEquals(8 * 2 + 0 + 3 + 4, list.size());
		// There are 6 <a>s, of which 3 match the precondition.
		assertLog(logA, "FFTTFT");
		// There are 8 nodes, of which 4 match the precondition.
		assertLog(logAny, "TFFTFFTT");
	}

	@Test
	public void isLast() {
		final ListTarget list = new ListTarget();
		final Preconditions preconditions = new Preconditions(list);

		// This sets up a (log of a) precondition which flags the last <a>.
		final PreconditionLog logA = new PreconditionLog(
				new IsLast(Start.on("cobol", "a")));
		preconditions.add(logA);

		// This sets up a (log of a) precondition which flags the last node.
		final PreconditionLog logNull = new PreconditionLog(new IsLast(null));
		preconditions.add(logNull);

		tree("x", //
				tree("a"), //
				tree("a", tree("a")), //
				tree("a", tree("a")) //
		).streamInto(preconditions);
		preconditions.done();

		// We see 2 events for every tree, no events for nodes (there are none),
		// and 2 matching preconditions (one for each we defined).
		assertEquals(6 * 2 + 0 + 2, list.size());

		// 6 truth values, as there are 6 "a" nodes
		assertLog(logA, "FFFFT");

		// 7 truth values, as there are 7 nodes
		assertLog(logNull, "FFFFFT");
	}

	// ------------------------------------------------------------------------

	private void assertLog(PreconditionLog log, String resolutions) {
		assertTrue(log.isDone());
		assertTrue(log.allResolved());
		assertEquals(resolutions, log.resolutions());
	}
}
