package com.semmle.cobol.generator.test;

import static koopa.core.data.Position.ZERO;
import static koopa.core.data.tags.AreaTag.PROGRAM_TEXT_AREA;

import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;
import koopa.core.targets.Target;
import koopa.core.trees.Tree;

class StreamUtil {
	/**
	 * A {@link Stream} which pushes the equivalent of a {@link Tree} into a
	 * {@link Target}.
	 */
	public static Stream tree(String name, Stream... nested) {
		return new Stream() {
			@Override
			public void streamInto(Target target) {
				target.push(Start.on("cobol", name));
				for (Stream n : nested)
					n.streamInto(target);
				target.push(End.on("cobol", name));
			}
		};
	}

	/**
	 * A {@link Stream} which pushes a {@link Token} with the given text into a
	 * {@link Target}.
	 */
	public static Stream token(String text) {
		return new Stream() {
			@Override
			public void streamInto(Target target) {
				target.push(new Token(text, ZERO, ZERO, PROGRAM_TEXT_AREA));
			}
		};
	}
}
