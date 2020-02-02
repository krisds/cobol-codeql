package com.semmle.cobol.generator.preconditions;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.preconditions.Precondition.Result;

import koopa.core.data.Data;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;
import koopa.core.targets.Target;

/**
 * This {@link Target} resolves {@link Precondition}s against the stream,
 * holding back {@link Data} from downstream {@link Target}s until the required
 * {@link Precondition}s have been resolved.
 */
public class Preconditions implements Target {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Preconditions.class);

	/**
	 * A {@link Precondition} which is being resolved.
	 */
	private final static class ActivePrecondition {
		/**
		 * The depth at/under which the precondition is active.
		 */
		public final int scope;

		/**
		 * The {@link Precondition} being resolved.
		 */
		public final Precondition precondition;

		public ActivePrecondition(int scope, Precondition precondition) {
			this.scope = scope;
			this.precondition = precondition;
		}

		@Override
		public String toString() {
			return "ACTIVE PRECON #" + scope + " " + precondition.toString();
		}
	}

	private final Target next;

	/**
	 * All {@link Precondition}s being resolved.
	 */
	private LinkedList<ActivePrecondition> preconditions = new LinkedList<>();

	/**
	 * The current depth in the tree, based on all {@link Data} we have seen.
	 */
	private int depth = 0;

	/**
	 * The depth of the tree, seen from the point of view of the {@link Data} we
	 * have passed on downstream.
	 */
	private int delayedDepth = 0;

	/**
	 * All {@link Data} which is being delayed while we resolve the
	 * {@link #preconditions}.
	 */
	private Queue<Data> delayed = new LinkedList<>();

	/**
	 * Used while processing {@link #delayed}.
	 */
	private Queue<Data> delayed2 = new LinkedList<>();

	public Preconditions(Target next) {
		this.next = next;
	}

	/**
	 * Adds a new {@linkplain Precondition} to be resolved.
	 * <p>
	 * This will immediately have the given precondition process all delayed
	 * {@linkplain Data}, so that it is up to date when processing new incoming
	 * data.
	 */
	public void add(Precondition p) {
		final ActivePrecondition activePrecondition = new ActivePrecondition(
				delayedDepth, p);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("added {}", activePrecondition);

		preconditions.add(activePrecondition);

		// We process all delayed by "streaming" it into delayed2. At the end we
		// will swap these around again, to restore things.

		while (!delayed.isEmpty()) {
			final Data h = delayed.poll();
			if (h instanceof Result)
				delayed2.add(h);
			else {
				final Result r = p.process(h);
				if (r != null)
					delayed2.add(r);
				delayed2.add(h);
			}
		}

		final Queue<Data> tmp = delayed;
		delayed = delayed2;
		delayed2 = tmp;
	}

	@Override
	public void push(Data d) {
		// Bring the current depth up to date with the newly seen data.
		if (d instanceof Start)
			depth += 1;
		else if (d instanceof End)
			depth -= 1;

		// Remove preconditions which have gone out of scope.
		while (!preconditions.isEmpty()
				&& preconditions.getLast().scope > depth) {
			final ActivePrecondition removed = preconditions.removeLast();
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("{} {} : removed {}", depth, d, removed);
			removed.precondition.done();
		}

		// Present the new data to the preconditions.
		for (ActivePrecondition active : preconditions) {
			final Result r = active.precondition.process(d);
			// If the precondition returns a result we add it to the delayed
			// data.
			if (r != null)
				delayed.add(r);
		}

		// Delay the new data until we are ready to forward it.
		delayed.add(d);

		// Forward any leading delayed data which is not unresolved.
		while (!delayed.isEmpty() && !isAnUnresolvedResult(delayed.peek())) {
			final Data h = delayed.poll();

			// When we forward delayed data we need to update the delayed depth
			// as well.
			if (h instanceof Start)
				delayedDepth += 1;
			else if (h instanceof End)
				delayedDepth -= 1;

			// We don't forward failed precondition results though.
			if (!isFailedResult(h))
				next.push(h);
		}
	}

	@Override
	public void done() {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("{} is done", depth);

		// When the stream is done, we need to finalize all waiting
		// preconditions.
		while (!preconditions.isEmpty()) {
			final ActivePrecondition removed = preconditions.removeLast();
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("{} DONE : removed {}", depth, removed);
			removed.precondition.done();
		}

		// Forward all delayed data.
		while (!delayed.isEmpty()) {
			final Data h = delayed.poll();
			if (!isAnUnresolvedResult(h) && !isFailedResult(h))
				next.push(h);
		}

		next.done();
	}

	private boolean isAnUnresolvedResult(Data d) {
		return d instanceof Result && !((Result) d).isResolved();
	}

	private boolean isFailedResult(Data d) {
		return d instanceof Result && ((Result) d).failed();
	}
}
