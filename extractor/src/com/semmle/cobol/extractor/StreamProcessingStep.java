package com.semmle.cobol.extractor;

import java.util.LinkedList;

import koopa.core.data.Data;
import koopa.core.targets.Target;

/**
 * Something which applies some processing to the stream of data being output by
 * the Cobol parser.
 */
public abstract class StreamProcessingStep implements Target {

	private StreamProcessingStep next = null;

	private LinkedList<Data> delayed = new LinkedList<>();

	public StreamProcessingStep then(StreamProcessingStep next) {
		this.next = next;
		return next;
	}

	protected void pass(Data d) {
		next.push(d);
	}

	@Override
	public void done() {
		passAllDelayed();
		next.done();
	}

	protected void delay(Data d) {
		delayed.addLast(d);
	}

	protected void passAllDelayed() {
		while (!delayed.isEmpty())
			pass(delayed.removeFirst());
	}
}
