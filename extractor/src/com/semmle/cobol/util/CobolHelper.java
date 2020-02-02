package com.semmle.cobol.util;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.semmle.util.exception.CatastrophicError;

import koopa.cobol.data.tags.CobolAreaTag;
import koopa.cobol.grammar.CobolGrammar;
import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.Tokens;
import koopa.core.data.tags.AreaTag;
import koopa.core.data.tags.SyntacticTag;
import koopa.core.trees.Tree;

/**
 * A variant of this class is also found in
 * com.semmle.util.syntaxhighlighting.cobol and net.sourceforge.pmd.cpd. This
 * one is different in that it works on the syntax tree rather than the source
 * of tokens.
 */
public class CobolHelper {

	private static final CobolGrammar COBOL = CobolGrammar.instance();
	private List<Tree> children;
	private int index = 0;

	public CobolHelper(Tree tree) {
		this.children = tree.getChildren();
	}

	public Token next() {
		if (!hasNextToken())
			return null;

		final Token next = nextToken();
		if (keepSeparate(next))
			return next;

		final Category category = getCategory(COBOL, next);

		final int line = next.getStart().getLinenumber();

		List<Token> partsToBeJoined = null;

		Data data;
		while ((data = nextData()) != null) {
			if (data == null || !(data instanceof Token)) {
				break;
			}

			Token follower = (Token) data;

			if (keepSeparate(follower)) {
				index -= 1;
				break;

			} else if (follower.getStart().getLinenumber() == line
					&& sameCategory(COBOL, category, follower)) {

				if (partsToBeJoined == null) {
					partsToBeJoined = new LinkedList<Token>();
					partsToBeJoined.add(next);
				}

				partsToBeJoined.add(follower);

			} else {
				index -= 1;
				break;
			}
		}

		if (partsToBeJoined == null)
			return next;

		Token joined = Tokens.join(partsToBeJoined, getTags(category));
		return joined;
	}

	private boolean hasNextToken() {
		while (true) {
			if (index >= children.size())
				return false;
			if (children.get(index).isToken())
				return true;
			index += 1;
		}
	}

	private Token nextToken() {
		if (!hasNextToken())
			return null;
		else
			return (Token) children.get(index++).getData();
	}

	private Data nextData() {
		if (index >= children.size())
			return null;
		else
			return children.get(index++).getData();
	}

	private static Object[] getTags(Category category) {
		switch (category) {
		case IDENTIFICATION_AREA:
			return new Object[] { CobolAreaTag.IDENTIFICATION_AREA };
		case INDICATOR_AREA:
			return new Object[] { CobolAreaTag.INDICATOR_AREA };
		case SEQUENCE_NUMBER_AREA:
			return new Object[] { CobolAreaTag.SEQUENCE_NUMBER_AREA };
		case COMMENT:
			return new Object[] { AreaTag.COMMENT };
		case WORD:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA };
		case WHITESPACE:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA,
					SyntacticTag.WHITESPACE };
		case EOLN:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA,
					SyntacticTag.END_OF_LINE };
		case STRING:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA,
					SyntacticTag.STRING };
		case DOT:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA };
		case SKIPPABLE:
			return new Object[] { AreaTag.PROGRAM_TEXT_AREA,
					SyntacticTag.SEPARATOR };
		default:
			throw new CatastrophicError("Unexpected category: " + category);
		}
	}

	private static final Set<String> SEPARATE = new LinkedHashSet<String>();
	static {
		SEPARATE.add("(");
		SEPARATE.add(")");
		SEPARATE.add("[");
		SEPARATE.add("]");
		SEPARATE.add(":");
		SEPARATE.add("\"");
		SEPARATE.add("'");
	}

	private static boolean keepSeparate(koopa.core.data.Token next) {
		boolean separate = SEPARATE.contains(next.getText());

		return separate;
	}

	private static boolean sameCategory(CobolGrammar grammar, Category category,
			koopa.core.data.Token follower) {

		return category.equals(getCategory(grammar, follower));
	}

	private static Category getCategory(CobolGrammar grammar,
			koopa.core.data.Token koopaToken) {
		if (koopaToken.hasTag(CobolAreaTag.SEQUENCE_NUMBER_AREA))
			return Category.SEQUENCE_NUMBER_AREA;
		else if (koopaToken.hasTag(CobolAreaTag.INDICATOR_AREA))
			return Category.INDICATOR_AREA;
		else if (koopaToken.hasTag(CobolAreaTag.IDENTIFICATION_AREA))
			return Category.IDENTIFICATION_AREA;
		else if (koopaToken.hasTag(AreaTag.COMMENT))
			return Category.COMMENT;
		else if (!koopaToken.hasTag(AreaTag.PROGRAM_TEXT_AREA))
			return Category.COMMENT;
		else if (koopaToken.hasTag(SyntacticTag.END_OF_LINE))
			return Category.EOLN;
		else if (koopaToken.hasTag(SyntacticTag.WHITESPACE))
			return Category.WHITESPACE;
		else if (koopaToken.hasTag(SyntacticTag.STRING))
			return Category.STRING;
		else if (".".equals(koopaToken.getText()))
			return Category.DOT;
		else if (grammar.canBeSkipped(koopaToken, null))
			return Category.SKIPPABLE;
		else
			return Category.WORD;
	}

	private static enum Category {
		IDENTIFICATION_AREA, INDICATOR_AREA, SEQUENCE_NUMBER_AREA, //
		COMMENT, WORD, WHITESPACE, STRING, DOT, SKIPPABLE, EOLN
	}
}
