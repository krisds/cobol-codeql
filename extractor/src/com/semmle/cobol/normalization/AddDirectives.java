package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.SEMMLE__HANDLED;
import static com.semmle.cobol.util.Common._SEMMLE__HANDLED;
import static com.semmle.cobol.util.Common._TEXT;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.cobol.sources.CompilerDirectives;
import koopa.core.data.Data;
import koopa.core.parsers.Parse;
import koopa.core.trees.Tree;

/**
 * While parsing with preprocessing enabled, any expanded compiler directives
 * will have been removed by the parser. They still exist on the
 * {@linkplain Parse} object, and we use that info to add them back in at the
 * end of the text, though wrapped in a &lt;semmle:handled&gt; node.
 */
public class AddDirectives extends StreamProcessingStep {

	private Parse parse;

	public AddDirectives(Parse parse) {
		this.parse = parse;
	}

	@Override
	public void push(Data d) {
		if (d == _TEXT) {
			final List<Tree> directives = getHandledDirectives(parse);

			if (directives != null && !directives.isEmpty())
				for (Tree t : directives) {
					pass(SEMMLE__HANDLED);
					// NOTE: the directives are Trees, but we pass them into the
					// stream directly. A later step will extract them again
					// into a streamed form.
					pass(t);
					pass(_SEMMLE__HANDLED);
				}
		}

		pass(d);
	}

	private static List<Tree> getHandledDirectives(Parse parse) {
		final List<Tree> directives = new LinkedList<Tree>();

		final CompilerDirectives compilerDirectives //
				= parse.getSource(CompilerDirectives.class);
		if (compilerDirectives != null)
			directives.addAll(compilerDirectives.getHandledDirectives());

		return directives;
	}

}
