package com.semmle.cobol.util;

import static com.semmle.cobol.normalization.NormalizationTag.COMPILER_GENERATED;
import static koopa.core.data.tags.AreaTag.PROGRAM_TEXT_AREA;
import static koopa.core.data.tags.SyntacticTag.SEPARATOR;
import static koopa.core.data.tags.SyntacticTag.WORD;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Replaced;
import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;
import koopa.core.data.tags.SyntacticTag;

// TODO Generate from spec ?
public final class Common {

	private Common() {
	}

	public static final Start TEXT = cobol("text");
	public static final End _TEXT = _cobol("text");

	public static final Start SOURCE_UNIT = cobol("source_unit");

	public static final Start PROGRAM_DEFINITION = cobol("programDefinition");
	public static final End _PROGRAM_DEFINITION = _cobol("programDefinition");

	public static final Start DATA_DIVISION = cobol("dataDivision");

	public static final Start PROCEDURE_DIVISION = cobol("procedureDivision");
	public static final End _PROCEDURE_DIVISION = _cobol("procedureDivision");

	public static final Start DECLARATIVES = cobol("declaratives");
	public static final End _DECLARATIVES = _cobol("declaratives");

	public static final Start DECLARATIVE_SECTION = cobol("declarativeSection");
	public static final End _DECLARATIVE_SECTION = _cobol("declarativeSection");

	public static final Start SECTION = cobol("section");
	public static final End _SECTION = _cobol("section");

	public static final Start PARAGRAPH = cobol("paragraph");
	public static final End _PARAGRAPH = _cobol("paragraph");

	public static final Start SENTENCE = cobol("sentence");
	public static final End _SENTENCE = _cobol("sentence");

	public static final Start STATEMENT = cobol("statement");
	public static final End _STATEMENT = _cobol("statement");

	public static final Start NESTED_STATEMENTS = cobol("nestedStatements");

	public static final Start COMPILER_STATEMENT = cobol("compilerStatement");
	public static final End _COMPILER_STATEMENT = _cobol("compilerStatement");

	public static final Start CONTINUE = cobol("continueStatement");
	public static final End _CONTINUE = _cobol("continueStatement");

	public static final Start IF = cobol("ifStatement");
	public static final Start EVALUATE = cobol("evaluateStatement");
	public static final Start PERFORM = cobol("performStatement");
	public static final Start GOTO = cobol("goToStatement");
	public static final Start EXIT = cobol("exitStatement");
	public static final Start GOBACK = cobol("gobackStatement");
	public static final Start STOP = cobol("stopStatement");
	public static final Start NEXT_SENTENCE = cobol("nextSentenceStatement");
	public static final Start SEARCH = cobol("searchStatement");
	public static final Start ON_EXCEPTION = cobol("onException");
	public static final Start NOT_ON_EXCEPTION = cobol("notOnException");
	public static final Start AT_END = cobol("atEnd");
	public static final Start NOT_AT_END = cobol("notAtEnd");
	public static final Start AT_END_OF_PAGE = cobol("atEndOfPage");
	public static final Start NOT_AT_END_OF_PAGE = cobol("notAtEndOfPage");
	public static final Start ON_OVERFLOW = cobol("onOverflow");
	public static final Start NOT_ON_OVERFLOW = cobol("notOnOverflow");
	public static final Start ON_SIZE_ERROR = cobol("onSizeError");
	public static final Start NOT_ON_SIZE_ERROR = cobol("notOnSizeError");
	public static final Start INVALID_KEY = cobol("invalidKey");
	public static final Start NOT_INVALID_KEY = cobol("notInvalidKey");
	public static final Start WITH_DATA = cobol("withData");
	public static final Start NO_DATA = cobol("noData");
	public static final Start EXEC_CICS = cobol("execCICSStatement");
	public static final Start EXEC_SQL = cobol("execSQLStatement");
	public static final Start ENTRY = cobol("entryStatement");
	public static final Start USE = cobol("useStatement");
	public static final Start ALTER = cobol("alterStatement");
	public static final Start MOVE = cobol("moveStatement");
	public static final Start READ = cobol("readStatement");
	public static final Start ADD = cobol("addStatement");
	public static final Start SUBTRACT = cobol("subtractStatement");
	public static final Start MULTIPLY = cobol("multiplyStatement");
	public static final Start DIVIDE = cobol("divideStatement");
	public static final Start COMPUTE = cobol("computeStatement");
	public static final Start OPEN = cobol("openStatement");
	public static final Start CLOSE = cobol("closeStatement");
	public static final Start DELETE = cobol("deleteStatement");
	public static final Start RETURN = cobol("returnStatement");
	public static final Start SORT = cobol("sortStatement");
	public static final Start MERGE = cobol("mergeStatement");
	public static final Start START = cobol("startStatement");
	public static final Start WRITE = cobol("writeStatement");
	public static final Start REWRITE = cobol("rewriteStatement");
	public static final Start COPY = cobol("copyStatement");
	public static final Start REPLACE = cobol("replaceStatement");
	public static final Start CALL = cobol("callStatement");
	public static final Start ACCEPT = cobol("acceptStatement");
	public static final Start DISPLAY = cobol("displayStatement");
	public static final Start STRING = cobol("stringStatement");
	public static final Start UNSTRING = cobol("unstringStatement");

	public static final Start WHEN = cobol("when");
	public static final Start WHEN_OTHER = cobol("whenOther");

	public static final Start THEN = cobol("thenBranch");
	public static final Start ELSE = cobol("elseBranch");

	public static final Start COBOL_WORD = cobol("cobolWord");
	public static final Start PICTURE_STRING = cobol("pictureString");
	public static final Start INTEGER_LITERAL = cobol("integerLiteral");
	public static final Start ALPHANUMERIC_LITERAL = cobol(
			"alphanumericLiteral");
	public static final Start DECIMAL = cobol("decimal");
	public static final Start ZERO = cobol("zero");
	public static final Start SPACE = cobol("space");

	public static final Start COMMENT_ENTRY = cobol("commentEntry");
	public static final End _COMMENT_ENTRY = _cobol("commentEntry");

	public static final Start RELOP = cobol("relop");
	public static final End _RELOP = _cobol("relop");

	public static final Start NOT = cobol("not");

	public static final Start GREATER_OR_EQUAL_OP = cobol("greaterOrEqualOp");
	public static final Start LESS_OR_EQUAL_OP = cobol("lessOrEqualOp");
	public static final Start GREATER_THAN_OP = cobol("greaterThanOp");
	public static final Start LESS_THAN_OP = cobol("lessThanOp");
	public static final Start EQUAL_TO_OP = cobol("equalToOp");
	public static final Start EXCEEDS_OP = cobol("exceedsOp");
	public static final Start EQUALS_OP = cobol("equalsOp");
	public static final Start UNEQUALTO_OP = cobol("unequalToOp");

	public static final Start UNKNOWN_DIRECTIVE = cobol("unknown_directive");
	public static final Start OTHER_STMT = cobol("other_stmt");
	public static final Start DATA_REFERENCE = cobol("data_reference");
	public static final Start ADD_EXPR = cobol("add");
	public static final Start SUBTRACT_EXPR = cobol("subtract");
	public static final Start MULTIPLY_EXPR = cobol("multiply");
	public static final Start DIVIDE_EXPR = cobol("divide");

	public static final Start SQL_UNKNOWN_SPEC = sql("unknown_spec");
	public static final Start SQL_UNKNOWN_EXPR = sql("unknown_expr");

	public static final Start CFLOW__EXIT_NODE = cflow("exit_node");
	public static final End _CFLOW__EXIT_NODE = cflow_("exit_node");

	public static final Start SEMMLE__REPLACED = semmle("replaced");
	public static final End _SEMMLE__REPLACED = semmle_("replaced");

	public static final Start SEMMLE__HANDLED = semmle("handled");
	public static final End _SEMMLE__HANDLED = semmle_("handled");

	public static final Start SEMMLE__COMPGENERATED = semmle("compgenerated");
	public static final End _SEMMLE__COMPGENERATED = semmle_("compgenerated");

	private static Start cobol(String name) {
		return Start.on("cobol", name);
	}

	private static End _cobol(String name) {
		return End.on("cobol", name);
	}

	private static Start semmle(String name) {
		return Start.on("semmle", name);
	}

	private static End semmle_(String name) {
		return End.on("semmle", name);
	}

	private static Start cflow(String name) {
		return Start.on("cflow", name);
	}

	private static End cflow_(String name) {
		return End.on("cflow", name);
	}

	private static Start sql(String name) {
		return Start.on("sql", name);
	}

	public static boolean isStart(Data d) {
		return d instanceof Start;
	}

	public static Token newSeparator(String text, Position pos, Replaced replaced) {
		return new Token(text, pos, pos, COMPILER_GENERATED, SEPARATOR,
				PROGRAM_TEXT_AREA).asReplacing(replaced);
	}

	public static Token newWord(String text, Position pos, Replaced replaced) {
		return new Token(text, pos, pos, COMPILER_GENERATED, WORD,
				PROGRAM_TEXT_AREA).asReplacing(replaced);
	}

	public static boolean isDot(Data d) {
		if (!(d instanceof Token))
			return false;

		final Token t = (Token) d;
		return ".".equals(t.getText()) && t.hasTag(SyntacticTag.SEPARATOR);
	}
}
