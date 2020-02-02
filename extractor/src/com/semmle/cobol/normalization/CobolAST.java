package com.semmle.cobol.normalization;

import static com.semmle.cobol.normalization.NormalizationTag.COMPILER_GENERATED;
import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.CONTINUE;
import static com.semmle.cobol.util.Common.SEMMLE__HANDLED;
import static com.semmle.cobol.util.Common.SEMMLE__REPLACED;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Util.matching;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.util.Util;

import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.data.markers.Start;
import koopa.core.trees.Tree;

public class CobolAST {

	private static final String LAST_DOT_XPATH = "(.//text()[.=\".\"])[last()]";

	public static Tree normalize(Tree ast, List<Tree> replacedDirectives,
			List<Tree> handledDirectives) {

		ast = normalizeText(ast);

		addImplicitEmptySentenceToEmptyDeclarativeSections(ast);
		addImplicitEmptySentenceToEmptySections(ast);
		addImplicitEmptySentenceToEmptyParagraphs(ast);

		addImplicitContinueToEmptySentences(ast);
		addControlFlowExitNodes(ast);

		addAllDirectives(ast, replacedDirectives, SEMMLE__REPLACED);
		addAllDirectives(ast, handledDirectives, SEMMLE__HANDLED);

		return ast;
	}

	/**
	 * The root node of the AST is for the "text" node we defined in our
	 * CobolProjects. This has a side effect: any leading or trailing comments
	 * and/or directives will be placed under "text" rather than under
	 * "compilationGroup" or "copybook" as we would like. This method normalizes
	 * that.
	 */
	private static Tree normalizeText(Tree ast) {
		Tree t = null;
		for (Tree c : ast.getChildren())
			if (c.isNode()) {
				t = c;
				break;
			}

		if (t == null)
			return ast;

		int ti = t.getChildIndex();

		LinkedList<Tree> before = new LinkedList<>();
		for (int i = 0; i < ti; i++)
			before.add(ast.getChild(i));

		LinkedList<Tree> after = new LinkedList<>();
		for (int i = ti + 1; i < ast.getChildCount(); i++)
			after.add(ast.getChild(i));

		while (ast.getChildCount() > 0)
			ast.removeChild(0);

		ast.addChild(t);

		while (!before.isEmpty())
			t.insertChild(0, before.removeLast());

		while (!after.isEmpty())
			t.addChild(after.removeFirst());

		return ast;
	}

	/**
	 * This looks for declarative sections in the AST which have no sentences or
	 * paragraphs inside. To each of these it then adds an implicit empty
	 * sentence. Its position will match the end of the declarative section's
	 * closing dot. Its length will be zero. The token for this implicit
	 * sentence will be tagged as
	 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
	 */
	private static void addImplicitEmptySentenceToEmptyDeclarativeSections(
			Tree ast) {
		// TODO This is wrong. There will always be one sentence: the USE
		// statement.
		List<Tree> emptySections = matching(ast,
				"//declarativeSection[count(sentence|paragraph)=0]");

		for (Tree s : emptySections) {
			Tree closingDot = s.getChild(s.getChildCount() - 1);

			Tree sentence = new Tree(SENTENCE);
			Position pos = closingDot.getEndPosition();
			Tree text = new Tree(new Token(".", pos, pos, COMPILER_GENERATED));
			sentence.addChild(text);

			s.addChild(sentence);
		}
	}

	/**
	 * This looks for sections in the AST which have no sentences or paragraphs
	 * inside. To each of these it then adds an implicit empty sentence. Its
	 * position will match the end of the section's closing dot. Its length will
	 * be zero. The token for this implicit sentence will be tagged as
	 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
	 */
	private static void addImplicitEmptySentenceToEmptySections(Tree ast) {
		List<Tree> emptySections = matching(ast,
				"//section[count(sentence|paragraph)=0]");

		for (Tree s : emptySections) {
			Tree closingDot = Util.uniqueMatch(s, LAST_DOT_XPATH);
			Position pos = closingDot.getEndPosition();
			Token token = (Token) closingDot.getData();

			Tree fakeSentence = new Tree(SENTENCE);
			Token fakeClosingDot = new Token(".", pos, pos, COMPILER_GENERATED)
					.asReplacing(token.getReplaced());
			Tree text = new Tree(fakeClosingDot);
			fakeSentence.addChild(text);

			s.addChild(fakeSentence);
		}
	}

	/**
	 * This looks for paragraphs in the AST which have no sentences inside. To
	 * each of these it then adds an implicit empty sentence. Its position will
	 * match the end of the paragraph's closing dot. Its length will be zero.
	 * The token for this implicit sentence will be tagged as
	 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
	 */
	private static void addImplicitEmptySentenceToEmptyParagraphs(Tree ast) {
		List<Tree> emptyParagraphs = matching(ast,
				"//paragraph[count(sentence)=0]");

		for (Tree p : emptyParagraphs) {
			Tree closingDot = Util.uniqueMatch(p, LAST_DOT_XPATH);
			Position pos = closingDot.getEndPosition();
			Token token = (Token) closingDot.getData();

			Tree fakeSentence = new Tree(SENTENCE);
			Token fakeClosingDot = new Token(".", pos, pos, COMPILER_GENERATED)
					.asReplacing(token.getReplaced());
			Tree text = new Tree(fakeClosingDot);
			fakeSentence.addChild(text);

			p.addChild(fakeSentence);
		}
	}

	/**
	 * This looks for sentences in the AST which have no statements inside. To
	 * each of these it then adds an implicit CONTINUE statement. Its position
	 * will match the start of the sentence's closing dot. Its length will be
	 * zero. The token for this implicit CONTINUE statement will be tagged as
	 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
	 */
	private static void addImplicitContinueToEmptySentences(Tree ast) {
		List<Tree> emptySentences = matching(ast,
				"//sentence[count(statement|compilerStatement)=0]");

		for (Tree s : emptySentences) {
			Tree closingDot = Util.uniqueMatch(s, LAST_DOT_XPATH);
			Position pos = closingDot.getStartPosition();
			Token token = (Token) closingDot.getData();

			Tree stmt = new Tree(STATEMENT);
			Tree cont = new Tree(CONTINUE);
			Token fakeContinue = new Token("CONTINUE", pos, pos,
					COMPILER_GENERATED).asReplacing(token.getReplaced());
			Tree text = new Tree(fakeContinue);
			cont.addChild(text);
			stmt.addChild(cont);

			s.insertChild(closingDot.getChildIndex(), stmt);
		}
	}

	/**
	 * The last dot found in any section, paragraph, declarative section or
	 * procedure division gets wrapped in a new "exit_node" tree node. This exit
	 * node will become part of the successor relationship, marking the end of
	 * any section, paragraph, declarative section and procedure division.
	 */
	private static void addControlFlowExitNodes(Tree ast) {
		for (Tree proc : matching(ast,
				"//paragraph|//section|//declarativeSection|//procedureDivision")) {
			Tree closingDot = Util.uniqueMatch(proc, LAST_DOT_XPATH);

			final Tree parent = closingDot.getParent();
			if (!"exit_node".equals(((Start) parent.getData()).getName())) {
				int index = closingDot.getChildIndex();
				parent.removeChild(index);

				Start s = CFLOW__EXIT_NODE;
				Tree t = new Tree(s);
				t.addChild(closingDot);

				parent.insertChild(index, t);
			}
		}
	}

	/**
	 * If there are directives which have been handled by the parser, we attach
	 * to a (newly created) "semmle:&lt;tag&gt;" node, which will be found at
	 * the root of the tree, and add all directives to that.
	 */
	private static void addAllDirectives(Tree ast, List<Tree> directives,
			Start tag) {

		if (directives == null || directives.size() == 0)
			return;

		final Tree forReplaced = new Tree(tag);
		ast.addChild(forReplaced);

		for (Tree directive : directives)
			forReplaced.addChild(directive);
	}
}
