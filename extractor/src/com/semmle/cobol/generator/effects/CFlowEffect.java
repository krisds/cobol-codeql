package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.andFinally;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.triggers.Triggers.or;
import static com.semmle.cobol.generator.triggers.Triggers.path;
import static com.semmle.cobol.generator.triggers.Triggers.start;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;

import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * An {@link Effect} which constructs the {@link CFlowGraph} and traps all
 * successor relationships when it is complete.
 */
class CFlowEffect implements Effect {

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final CFlowGraph graph = new CFlowGraph();

		on(start(PROCEDURE_DIVISION), all( //
				graph.start(), //
				on(or(path("**/<statement>/<>"),
						path("**/<compilerStatement>/<>"),
						CFlowGraph.CFLOW_NODE),
						all( //
								graph.pushNode(), //
								// Finally, not just at end, so we can be sure
								// that the tuple has been fully constructed.
								andFinally(graph.popNode()) //
						)), //
				// Finally, not just at end, so we can be sure that the tuple
				// has been fully constructed.
				andFinally(graph.finish()) //
		)).apply(event, frame, engine);
	}

	@Override
	public String toString() {
		return "cflow";
	}
}
