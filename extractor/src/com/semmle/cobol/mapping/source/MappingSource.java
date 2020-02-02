package com.semmle.cobol.mapping.source;

import static com.semmle.cobol.mapping.tags.MappingTokenTag.ALL_CHILD_NODES;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.ASSIGN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.ATTRIBUTE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.BEGIN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.CALL_BUILTIN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.COMMENT;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.CURRENT_NODE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.NODE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.NTH_CHILD_NODE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.QL_TYPE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.RETURN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.STATE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.STRING;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.TRAP;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.WHITESPACE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.YPATH;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLetterOrDigit;
import static koopa.core.data.tags.SyntacticTag.END_OF_LINE;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.Tokens;
import koopa.core.sources.BasicSource;
import koopa.core.sources.Source;

public class MappingSource extends BasicSource implements Source {

	private final Source source;

	private Token token = null;
	private int index = 0;

	public MappingSource(Source source) {
		this.source = source;
	}

	@Override
	protected Data nxt1() {
		// Grab a new token if needed.
		while (token == null || atEnd()) {
			token = null;
			index = 0;
			
			final Data d = source.next();
			if (d == null)
				return null;
			if (d instanceof Token)
				token = (Token) d;
		}
		
		// Line separators are forwarded as they are.
		if (token.hasTag(END_OF_LINE)) {
			index = token.getLength();
			return token;
		}

		if (isIndentation())
			return indent();
		else if (startsComment())
			return comment();
		else if (startsNodeName())
			return nodeName();
		else if (startsQLType())
			return qlType();
		else if (startsState())
			return state();
		else if (startsAttribute())
			return attribute();
		else if (startsXPath())
			return xpath();
		else if (startsCall())
			return call();
		else if (startsString())
			return string();
		else if (isWhitespace())
			return whitespace();
		else if (isTrapOp())
			return trapOp();
		else if (isParentOp())
			return parentOp();
		else if (isReturnOp())
			return returnOp();
		else if (isCurrentNodeOp())
			return currentNodeOp();
		else if (isAllChildNodesOp())
			return allChildNodes();
		else if (isNthChildNodeOp())
			return nthChildNode();

		char c = token.charAt(index);
		return unknown(c);
	}

	// ------------------------------------------------------------------------

	private boolean atStart() {
		return index == 0;
	}

	private boolean atEnd() {
		return index >= token.getLength();
	}

	private boolean atEnd(int offset) {
		return index + offset >= token.getLength();
	}

	private char c() {
		return token.charAt(index);
	}

	private char c(int offset) {
		if (index + offset < token.getLength())
			return token.charAt(index + offset);
		else
			return (char) 0;
	}

	private String cs(int count) {
		final String text = token.getText();

		if (index + count < text.length())
			return text.substring(index, index + count);
		else
			return text.substring(index);
	}

	private boolean at(String s) {
		return s.equals(cs(s.length()));
	}

	private int consume() {
		return index += 1;
	}

	private Token token(int start, Object... tags) {
		return Tokens.subtoken(token, start, index).withTags(tags);
	}

	// ------------------------------------------------------------------------

	private boolean isIndentation() {
		return atStart() && c() == ' ';
	}

	private Token indent() {
		while (!atEnd() && c() == ' ')
			consume();

		if (atEnd())
			return token(0, WHITESPACE);
		else
			return token(0, BEGIN);
	}

	// ------------------------------------------------------------------------

	private boolean startsComment() {
		return c() == '#';
	}

	private Token comment() {
		final int start = index;
		index = token.getLength();
		return token(start, COMMENT);
	}

	// ------------------------------------------------------------------------

	private boolean startsNodeName() {
		return Character.isLetter(c()) || c() == '_';
	}

	private void consumeNodeName() {
		while (!atEnd() && (isLetterOrDigit(c()) || c() == '_'))
			consume();
	}

	private void consumeQualifiedNodeName() {
		consumeNodeName();
		while (!atEnd() && c() == '/') {
			consume();
			consumeNodeName();
		}
	}

	private Token nodeName() {
		final int start = index;
		consumeQualifiedNodeName();
		return token(start, NODE_NAME);
	}

	// ------------------------------------------------------------------------

	private boolean isWhitespace() {
		return Character.isWhitespace(c());
	}

	private Token whitespace() {
		final int start = index;

		while (!atEnd() && isWhitespace())
			consume();

		return token(start, WHITESPACE);
	}

	// ------------------------------------------------------------------------

	private static final String TRAP_OP = "=>";

	private boolean isTrapOp() {
		return at(TRAP_OP);
	}

	private Token trapOp() {
		final int start = index;
		index += TRAP_OP.length();
		return token(start, TRAP);
	}

	// ------------------------------------------------------------------------

	private boolean startsQLType() {
		return c() == '@';
	}

	private Token qlType() {
		final int start = index;
		consume();
		consumeNodeName();
		return token(start, QL_TYPE);
	}

	// ------------------------------------------------------------------------

	private boolean startsState() {
		return c() == '<' && isLetterOrDigit(c(1));
	}

	private Token state() {
		final int start = index;
		consume(); // '<'
		consume(); // letter or digit

		while (!atEnd()) {
			if (c() == '>') {
				consume();
				break;

			} else {
				// Should limit this to letters and digits
				consume();
			}
		}

		return token(start, STATE_NAME);
	}

	// ------------------------------------------------------------------------

	private boolean startsAttribute() {
		return c() == '.';
	}

	private Token attribute() {
		final int start = index;
		consume();
		consumeNodeName();
		return token(start, ATTRIBUTE_NAME);
	}

	// ------------------------------------------------------------------------

	private static final String PARENT_OP = "<-";

	private boolean isParentOp() {
		return at(PARENT_OP);
	}

	private Token parentOp() {
		final int start = index;
		index += PARENT_OP.length();
		return token(start, ASSIGN);
	}

	// ------------------------------------------------------------------------

	private static final String RETURN_OP = "==";

	private boolean isReturnOp() {
		return at(RETURN_OP);
	}

	private Token returnOp() {
		final int start = index;
		index += RETURN_OP.length();
		return token(start, RETURN);
	}

	// ------------------------------------------------------------------------

	private boolean startsXPath() {
		return c() == '{';
	}

	private Token xpath() {
		final int start = index;
		consume();

		int openBraces = 1;
		while (!atEnd() && openBraces > 0) {
			if (c() == '{')
				openBraces += 1;
			else if (c() == '}')
				openBraces -= 1;

			consume();
		}

		return token(start, YPATH);
	}

	// ------------------------------------------------------------------------

	private static final String CURRENT_NODE_OP = "$.";

	private boolean isCurrentNodeOp() {
		return at(CURRENT_NODE_OP);
	}

	private Token currentNodeOp() {
		final int start = index;
		index += CURRENT_NODE_OP.length();
		return token(start, CURRENT_NODE);
	}

	// ------------------------------------------------------------------------

	private static final String ALL_CHILD_NODES_OP = "$*";

	private boolean isAllChildNodesOp() {
		return at(ALL_CHILD_NODES_OP);
	}

	private Token allChildNodes() {
		final int start = index;
		index += ALL_CHILD_NODES_OP.length();
		return token(start, ALL_CHILD_NODES);
	}

	// ------------------------------------------------------------------------

	private boolean isNthChildNodeOp() {
		return c() == '$' && (isDigit(c(+1)) || c(+1) == '-' && isDigit(c(+2)));
	}

	private Token nthChildNode() {
		final int start = index;
		consume();
		consume();

		while (!atEnd() && isDigit(c()))
			consume();

		return token(start, NTH_CHILD_NODE);
	}

	// ------------------------------------------------------------------------

	private boolean startsCall() {
		return c() == '[';
	}

	private Token call() {
		final int start = index;
		consume();

		int openBrackets = 1;
		while (!atEnd() && openBrackets > 0) {
			if (c() == '[')
				openBrackets += 1;
			else if (c() == ']')
				openBrackets -= 1;

			consume();
		}

		return token(start, CALL_BUILTIN);
	}

	// ------------------------------------------------------------------------

	private boolean startsString() {
		return c() == '"';
	}

	private Token string() {
		final int start = index;
		consume();

		while (!atEnd()) {
			if (c() == '\\' && !atEnd(+1)) {
				consume();
				consume();

			} else if (c() == '"') {
				consume();
				break;

			} else
				consume();
		}

		return token(start, STRING);
	}

	// ------------------------------------------------------------------------

	private Token unknown(char c) {
		int start = index;
		index = token.getLength();
		System.err.println("Unexpected character at line "
				+ token.getStart().getLinenumber() + " column " + start + ": '"
				+ c + "'");
		return Tokens.subtoken(token, start);
	}

	public void close() {
		source.close();
	}
}
