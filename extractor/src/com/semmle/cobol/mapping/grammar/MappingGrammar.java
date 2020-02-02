package com.semmle.cobol.mapping.grammar;

import static com.semmle.cobol.mapping.tags.MappingTokenTag.ALL_CHILD_NODES;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.ASSIGN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.ATTRIBUTE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.BEGIN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.CALL_BUILTIN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.COMMENT;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.CURRENT_NODE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.END;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.NODE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.NTH_CHILD_NODE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.QL_TYPE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.RETURN;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.STATE_NAME;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.STRING;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.TRAP;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.WHITESPACE;
import static com.semmle.cobol.mapping.tags.MappingTokenTag.YPATH;
import static koopa.core.data.tags.SyntacticTag.END_OF_LINE;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.grammars.fluent.FluentGrammar;
import koopa.core.parsers.ParserCombinator;

public class MappingGrammar extends FluentGrammar {

	public MappingGrammar() {
		// A mapping script consists of many rules.
		define("rules").as(many("rule"), eof());

		// The right rule gets matched by means of the node's name.
		// There may be an optional state-name preceding it, which will limit
		// the use of that rule to the mentioned state.
		define("rule").as(optional("state-name"), "node-name",
				oneOf( //
						with("block").as("trap-tuple"), //
						with("block").as("return"), //
						"block" //
				));

		// There are two options for trapping tuples.
		defineHelper("trap-tuple").as(oneOf( //
				"trap-ql-type", //
				"trap-error" //
		));

		// First option is for trapping any kind of QL type.
		define("trap-ql-type").as( //
				tagged(TRAP), any(), "ql-type", //
				optional("block") //
		);

		// Second option is for trapping errors.
		define("trap-error").as( //
				tagged(TRAP), any(), "==error==", //
				"path", "string");

		define("return").as(tagged(RETURN), any(), //
				with("value").as("expression"));

		// Fold-left operator.
		define("foldl").as("==foldl==", "path",
				oneOf( //
						with("block").as( //
								with("trap-ql-type").as("ql-type")), //
						"block"));

		// Change context.
		define("in-context-of").as("==in==", "path", "block");

		// Switch on a different set of mapping rules.
		define("using-state").as( //
				optional("==using=="), "state-name", "block");

		// Equivalent to a switch-case.
		define("switch-case").as(oneOrMore("when"), optional("otherwise"));
		define("when").as("==when==", oneOf("ypath", "node-name"), "block");
		define("otherwise").as("==otherwise==", "block");

		// A path is anything which yields tree nodes.
		define("path").as(oneOf( //
				"ypath", //
				"current-node", //
				"all-child-nodes", //
				"nth-child-node" //
		));
		define("ypath").as(tagged(YPATH), any());
		define("current-node").as(tagged(CURRENT_NODE), any());
		define("all-child-nodes").as(tagged(ALL_CHILD_NODES), any());
		define("nth-child-node").as(tagged(NTH_CHILD_NODE), any());

		// A block is a group of statements.
		define("block").as("nested");

		defineHelper("nested").as(//
				"begin", //
				many(oneOf("nested", "statement")), //
				"end");

		defineHelper("statement").as(oneOf( //
				"switch-case", //
				"trap-tuple", //
				"assign-attribute", //
				"in-context-of", //
				"using-state", //
				"expression" //
		));

		defineHelper("expression").as(oneOf( //
				"call-builtin", //
				"foldl", //
				"mapping" //
		));

		// Link tuples to attributes.
		define("assign-attribute").as( //
				"attribute-name", tagged(ASSIGN), any(), //
				with("rvalue").as(oneOf("expression", "block")));

		// Requests to map a (collection of) nodes using the rules.
		defineHelper("mapping").as(oneOf( //
				"map-with-default", //
				"map-as-node", //
				"map-path" //
		));

		define("map-with-default").as( //
				optional("==map=="), "path", //
				"==default==", "node-name");

		define("map-as-node").as( //
				optional("==map=="), "path", //
				"==as==", "node-name");

		define("map-path").as( //
				optional("==map=="), "path");

		// Some basic elements...
		define("call-builtin").as(tagged(CALL_BUILTIN), any());
		defineHelper("begin").as(tagged(BEGIN), any());
		defineHelper("end").as(tagged(END), any());
		define("node-name").as(tagged(NODE_NAME), any());
		define("state-name").as(tagged(STATE_NAME), any());
		define("attribute-name").as(tagged(ATTRIBUTE_NAME), any());
		define("ql-type").as(tagged(QL_TYPE), any());
		define("string").as(tagged(STRING), any());
	}

	public ParserCombinator rules() {
		return definitionOf("rules").asParser();
	}

	public ParserCombinator statement() {
		return definitionOf("statement").asParser();
	}

	// ------------------------------------------------------------------------

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	@Override
	public boolean isProgramText(Data d) {
		if (!(d instanceof Token))
			return false;
		final Token t = (Token) d;
		return !isComment(t) && !isSeparator(t);
	}

	public boolean isComment(Token token) {
		return token.hasTag(COMMENT);
	}

	public boolean isSeparator(Token token) {
		return token.hasTag(WHITESPACE) || token.hasTag(END_OF_LINE);
	}

	@Override
	public String getNamespace() {
		return "mapping";
	}
}
