package com.semmle.cobol.mapping.source;

import static com.semmle.cobol.mapping.tags.MappingTokenTag.BEGIN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.COMMENT;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.WHITESPACE;
import static koopa.core.data.tags.SyntacticTag.END_OF_LINE;

import com.semmle.cobol.mapping.tags.MappingTokenTag;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.sources.BasicSource;
import koopa.core.sources.Source;

class IndentationToNesting extends BasicSource implements Source {

	private static final Token DEDENT_TOKEN = new Token("", Position.ZERO,
			Position.ZERO, MappingTokenTag.END);

	private static final Token INDENT_TOKEN = new Token("", Position.ZERO,
			Position.ZERO, MappingTokenTag.BEGIN);

	private final Source source;

	private int diff = 0;
	private int depth = 0;

	public IndentationToNesting(Source source) {
		this.source = source;
	}

	@Override
	protected Data nxt1() {
		while (true) {
			if (diff < 0) {
				diff += 1;
				return DEDENT_TOKEN;

			} else if (diff > 0) {
				diff -= 1;
				return INDENT_TOKEN;
			}

			Data d = source.next();

			if (d == null) {
				if (depth > 0) {
					diff = 0 - depth;
					depth = 0;
					continue;
				}

				return null;
			}
			
			if (!(d instanceof Token)) {
				return d;
			}

			final Token t = (Token) d;
			
			if (t.hasTag(BEGIN)) {
				int newDepth = t.getLength();
				diff = newDepth - depth;
				depth = newDepth;
				continue;
			}

			if (t.hasTag(END_OF_LINE))
				return t;

			if (t.getStart().getPositionInLine() == 1
					&& !t.hasTag(COMMENT) && !t.hasTag(WHITESPACE)) {

				if (depth == 0)
					return t;

				diff = 0 - depth;
				depth = 0;
				source.unshift(t);
				continue;
			}

			return t;
		}
	}

	public void close() {
		source.close();
	}
}
