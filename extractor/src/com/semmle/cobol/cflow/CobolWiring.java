package com.semmle.cobol.cflow;

import static com.semmle.cobol.cflow.Wiring.Tag.BRANCH;
import static com.semmle.cobol.cflow.Wiring.Tag.CFLOW;
import static com.semmle.cobol.util.Common.AT_END;
import static com.semmle.cobol.util.Common.AT_END_OF_PAGE;
import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.EVALUATE;
import static com.semmle.cobol.util.Common.EXIT;
import static com.semmle.cobol.util.Common.GOBACK;
import static com.semmle.cobol.util.Common.GOTO;
import static com.semmle.cobol.util.Common.IF;
import static com.semmle.cobol.util.Common.INVALID_KEY;
import static com.semmle.cobol.util.Common.NEXT_SENTENCE;
import static com.semmle.cobol.util.Common.NOT_AT_END;
import static com.semmle.cobol.util.Common.NOT_AT_END_OF_PAGE;
import static com.semmle.cobol.util.Common.NOT_INVALID_KEY;
import static com.semmle.cobol.util.Common.NOT_ON_EXCEPTION;
import static com.semmle.cobol.util.Common.NOT_ON_OVERFLOW;
import static com.semmle.cobol.util.Common.NOT_ON_SIZE_ERROR;
import static com.semmle.cobol.util.Common.NO_DATA;
import static com.semmle.cobol.util.Common.ON_EXCEPTION;
import static com.semmle.cobol.util.Common.ON_OVERFLOW;
import static com.semmle.cobol.util.Common.ON_SIZE_ERROR;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PERFORM;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.SEARCH;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Common.STOP;
import static com.semmle.cobol.util.Common.WITH_DATA;

import com.semmle.cobol.cflow.logic.BranchLogic;
import com.semmle.cobol.cflow.logic.DeclarativeSectionLogic;
import com.semmle.cobol.cflow.logic.EvaluateStatementLogic;
import com.semmle.cobol.cflow.logic.ExitNodeLogic;
import com.semmle.cobol.cflow.logic.ExitStatementLogic;
import com.semmle.cobol.cflow.logic.GoToStatementLogic;
import com.semmle.cobol.cflow.logic.GobackStatementLogic;
import com.semmle.cobol.cflow.logic.IfStatementLogic;
import com.semmle.cobol.cflow.logic.NextSentenceStatementLogic;
import com.semmle.cobol.cflow.logic.ParagraphLogic;
import com.semmle.cobol.cflow.logic.PerformStatementLogic;
import com.semmle.cobol.cflow.logic.ProcedureDivisionLogic;
import com.semmle.cobol.cflow.logic.SearchStatementLogic;
import com.semmle.cobol.cflow.logic.SectionLogic;
import com.semmle.cobol.cflow.logic.SentenceLogic;
import com.semmle.cobol.cflow.logic.StatementLogic;
import com.semmle.cobol.cflow.logic.StopStatementLogic;

public class CobolWiring extends Wiring {
	private CobolWiring() {
		final BranchLogic branchLogic = new BranchLogic(this);

		logic(PROCEDURE_DIVISION, new ProcedureDivisionLogic(this));
		tag(PROCEDURE_DIVISION, CFLOW);

		logic(DECLARATIVE_SECTION, new DeclarativeSectionLogic(this));
		tag(DECLARATIVE_SECTION, CFLOW);

		logic(SECTION, new SectionLogic(this));
		tag(SECTION, CFLOW);

		logic(PARAGRAPH, new ParagraphLogic(this));
		tag(PARAGRAPH, CFLOW);

		logic(SENTENCE, new SentenceLogic(this));
		tag(SENTENCE, CFLOW);

		logic(STATEMENT, new StatementLogic(this));
		tag(STATEMENT, CFLOW);

		logic(COMPILER_STATEMENT, new StatementLogic(this));
		tag(COMPILER_STATEMENT, CFLOW);

		logic(CFLOW__EXIT_NODE, new ExitNodeLogic(this));
		tag(CFLOW__EXIT_NODE, CFLOW);

		logic(IF, new IfStatementLogic(this));
		logic(EVALUATE, new EvaluateStatementLogic(this));
		logic(PERFORM, new PerformStatementLogic(this));
		logic(GOTO, new GoToStatementLogic(this));
		logic(EXIT, new ExitStatementLogic(this));
		logic(GOBACK, new GobackStatementLogic(this));
		logic(STOP, new StopStatementLogic(this));
		logic(NEXT_SENTENCE, new NextSentenceStatementLogic(this));
		logic(SEARCH, new SearchStatementLogic(this));

		logic(ON_EXCEPTION, branchLogic);
		tag(ON_EXCEPTION, CFLOW, BRANCH);

		logic(NOT_ON_EXCEPTION, branchLogic);
		tag(NOT_ON_EXCEPTION, CFLOW, BRANCH);

		logic(AT_END, branchLogic);
		tag(AT_END, CFLOW, BRANCH);

		logic(NOT_AT_END, branchLogic);
		tag(NOT_AT_END, CFLOW, BRANCH);

		logic(AT_END_OF_PAGE, branchLogic);
		tag(AT_END_OF_PAGE, CFLOW, BRANCH);

		logic(NOT_AT_END_OF_PAGE, branchLogic);
		tag(NOT_AT_END_OF_PAGE, CFLOW, BRANCH);

		logic(ON_OVERFLOW, branchLogic);
		tag(ON_OVERFLOW, CFLOW, BRANCH);

		logic(NOT_ON_OVERFLOW, branchLogic);
		tag(NOT_ON_OVERFLOW, CFLOW, BRANCH);

		logic(ON_SIZE_ERROR, branchLogic);
		tag(ON_SIZE_ERROR, CFLOW, BRANCH);

		logic(NOT_ON_SIZE_ERROR, branchLogic);
		tag(NOT_ON_SIZE_ERROR, CFLOW, BRANCH);

		logic(INVALID_KEY, branchLogic);
		tag(INVALID_KEY, CFLOW, BRANCH);

		logic(NOT_INVALID_KEY, branchLogic);
		tag(NOT_INVALID_KEY, CFLOW, BRANCH);

		logic(WITH_DATA, branchLogic);
		tag(WITH_DATA, CFLOW, BRANCH);

		logic(NO_DATA, branchLogic);
		tag(NO_DATA, CFLOW, BRANCH);
	}

	public static final CobolWiring INSTANCE = new CobolWiring();
}
