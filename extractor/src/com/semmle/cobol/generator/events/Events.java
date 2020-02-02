package com.semmle.cobol.generator.events;

import static com.semmle.cobol.generator.events.Event.Type.END;
import static com.semmle.cobol.generator.events.Event.Type.HALSTEAD;
import static com.semmle.cobol.generator.events.Event.Type.PRECONDITION;
import static com.semmle.cobol.generator.events.Event.Type.START;
import static com.semmle.cobol.generator.events.Event.Type.TOKEN;
import static com.semmle.cobol.util.Common.SEMMLE__COMPGENERATED;
import static com.semmle.cobol.util.Common._SEMMLE__COMPGENERATED;

import java.util.Stack;

import com.semmle.cobol.generator.preconditions.Precondition.Result;
import com.semmle.cobol.halstead.HalsteadCount;
import com.semmle.cobol.util.Common;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;
import koopa.core.targets.Target;

/**
 * A {@link Target} which takes {@link Data} from the stream, builds up the
 * {@link Event} information from that, and invokes the {@link #callback} with
 * this event information.
 */
public class Events implements Target {

	public static interface Callback {
		void process(Event event);
	}

	private final Callback callback;

	private static class State {
		public Position lastPosition = Position.ZERO;
		public Token lastToken = null;
		public int compilerGenerated = 0;
	}

	private State state = new State();
	private Stack<State> dormant = new Stack<>();

	private final TreePath path = new TreePath();
	private final Event event = new Event();

	public Events(Callback callback) {
		this.callback = callback;
	}

	@Override
	public void push(Data d) {
		if (d == Common.SEMMLE__HANDLED) {
			// Anything under <semmle:handled> should be tracked with its own
			// state,
			// so we don't mess up the locations.
			this.dormant.push(state);
			this.state = new State();

		} else if (d == Common._SEMMLE__HANDLED) {
			this.state = this.dormant.pop();

		} else if (d == SEMMLE__COMPGENERATED) {
			// Certain logic needs to know whether the current subtree belongs
			// to something compiler generated.
			state.compilerGenerated += 1;
			return;

		} else if (d == _SEMMLE__COMPGENERATED) {
			if (state.compilerGenerated <= 0)
				throw new CatastrophicError(
						"Badly nested compiler generated markers.");

			state.compilerGenerated -= 1;
			return;
		}

		event.data = d;
		event.before = path.current();

		if (d instanceof Start) {
			event.type = START;
			event.after = path.enter((Start) d, state.lastPosition,
					state.compilerGenerated > 0);
			callback.process(event);

		} else if (d instanceof End) {
			event.type = END;

			path.setEnd(state.lastPosition, state.lastToken);

			event.after = path.leave();
			callback.process(event);

		} else if (d instanceof Token) {
			event.type = TOKEN;

			final Token t = (Token) d;

			path.setStart(t.getStart(), t);
			state.lastPosition = t.getEnd();
			state.lastToken = t;

			event.after = event.before;
			callback.process(event);

		} else if (d instanceof Result) {
			event.type = PRECONDITION;
			callback.process(event);

		} else if (d instanceof HalsteadCount) {
			event.type = HALSTEAD;
			callback.process(event);
		}
	}

	@Override
	public void done() {
	}
}
