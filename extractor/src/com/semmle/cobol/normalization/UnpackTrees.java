package com.semmle.cobol.normalization;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;
import koopa.core.trees.Tree;

/**
 * A step which looks for {@linkplain Tree}s in the stream, and unpacks them
 * back into stream form.
 */
public class UnpackTrees extends StreamProcessingStep {

	@Override
	public void push(Data d) {
		if (d instanceof Tree)
			passTree((Tree) d);
		else
			pass(d);
	}

	private void passTree(Tree t) {
		final Data d = t.getData();

		if (d instanceof Start) {
			final Start s = (Start) d;
			pass(s);

			for (Tree child : t.getChildren())
				passTree(child);

			pass(s.matchingEnd());

		} else
			pass(d);
	}
}
