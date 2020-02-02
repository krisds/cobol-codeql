package com.semmle.cobol.generator;

import static com.semmle.cobol.generator.effects.Effects.MAY_BE_OMITTED;
import static com.semmle.cobol.generator.effects.Effects.NUMLINES;
import static com.semmle.cobol.generator.effects.Effects.PICTURE;
import static com.semmle.cobol.generator.effects.Effects.RETURN;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.assignTo;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.createTuple;
import static com.semmle.cobol.generator.effects.Effects.foldl;
import static com.semmle.cobol.generator.effects.Effects.ifNotParented;
import static com.semmle.cobol.generator.effects.Effects.literalWithValue;
import static com.semmle.cobol.generator.effects.Effects.maybeReturnFirstChild;
import static com.semmle.cobol.generator.effects.Effects.nestedRecordStructure;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.effects.Effects.otherwise;
import static com.semmle.cobol.generator.effects.Effects.overrideTupleType;
import static com.semmle.cobol.generator.effects.Effects.returnChild;
import static com.semmle.cobol.generator.effects.Effects.returnFirstChild;
import static com.semmle.cobol.generator.effects.Effects.returnFirstChildWithThisNode;
import static com.semmle.cobol.generator.effects.Effects.setAttribute;
import static com.semmle.cobol.generator.effects.Effects.setAttributeAs;
import static com.semmle.cobol.generator.effects.Effects.setAttributeToProgramText;
import static com.semmle.cobol.generator.triggers.Triggers.or;
import static com.semmle.cobol.generator.triggers.Triggers.path;
import static com.semmle.cobol.util.Common.ADD_EXPR;
import static com.semmle.cobol.util.Common.DATA_REFERENCE;
import static com.semmle.cobol.util.Common.DIVIDE_EXPR;
import static com.semmle.cobol.util.Common.MULTIPLY_EXPR;
import static com.semmle.cobol.util.Common.OTHER_STMT;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SOURCE_UNIT;
import static com.semmle.cobol.util.Common.SQL_UNKNOWN_EXPR;
import static com.semmle.cobol.util.Common.SQL_UNKNOWN_SPEC;
import static com.semmle.cobol.util.Common.SUBTRACT_EXPR;
import static com.semmle.cobol.util.Common.UNKNOWN_DIRECTIVE;

import com.semmle.cobol.generator.functions.SwitchFn1;
import com.semmle.cobol.generator.rules.RuleSet;

import koopa.core.data.Data;

class CobolRules extends RuleSet {

	public static void initialize(RuleSet rules) {
		// This combines compilationGroup and copybook into text.
		rules.define("<text>", all( //
				createTuple("compilation_group"), //

				on(path("<compilationGroup>"), all( //
						// .source_units <- {sourceUnit/$*} default source_unit
						setAttribute("source_units", path("<sourceUnit>/<>"),
								rules, SOURCE_UNIT) //
				)), //

				on(path("<copybook>"), all( //
						overrideTupleType("copybook"), //

						// .units <- {sourceUnit/$*} default source_unit
						setAttribute("units", path("<sourceUnit>/<>"), rules,
								SOURCE_UNIT), //

						// .statements <- {copybookHoldingBehaviour/statement}
						setAttribute("statements",
								path("<copybookHoldingBehaviour>/<statement>"),
								rules), //

						// .sentences <- {copybookHoldingBehaviour/sentence}
						setAttribute("sentences",
								path("<copybookHoldingBehaviour>/<sentence>"),
								rules), //

						// .paragraphs <- {copybookHoldingBehaviour/paragraph}
						setAttribute("paragraphs",
								path("<copybookHoldingBehaviour>/<paragraph>"),
								rules), //

						// .sections <- {copybookHoldingBehaviour/section}
						setAttribute("sections",
								path("<copybookHoldingBehaviour>/<section>"),
								rules), //

						// .entries <- in {copybookHoldingData} do
						// {constantEntry|dataDescriptionEntry|fileDescriptionEntry/$*|recordDescriptionEntry/$*}
						// [NESTED_RECORD_STRUCTURE]
						on(path("<copybookHoldingData>"), //
								nestedRecordStructure("entries", //
										or(path("<constantEntry>"), //
												path("<dataDescriptionEntry>"), //
												path("<fileDescriptionEntry>/<>"), //
												path("<recordDescriptionEntry>/<>")), //
										rules)) //
				)), //

				// .preprocessing_directives <-
				// {//copyStatement|//replaceStatement}
				on(or(path("**/<copyStatement>"),
						path("**/<replaceStatement>")), //
						all(rules.applyMatchingRule(), //
								atEnd(ifNotParented(assignTo(
										"preprocessing_directives"))))), //

				// .handled_directives <- {/handled/$*} default
				// unknown_directive
				setAttribute("handled_directives", path("<semmle:handled>/<>"),
						rules, UNKNOWN_DIRECTIVE), //

				// [NUMLINES]
				NUMLINES //
		));

		// unknown_directive => @unknown_directive
		rules.define("<unknown_directive>", createTuple("unknown_directive"));

		// handled/directive
		// when {iso/instruction/source} map {iso/instruction/source}
		// when {mf/set} map {mf/set}
		// otherwise map $. as unknown_directive
		rules.define("<semmle:handled>/<cobol-directives:directive>", all( //
				on(path("<cobol-directives:iso>/<cobol-directives:instruction>/<cobol-directives:source>"),
						all( //
								rules.applyMatchingRule(), //
								atEnd(RETURN) //
						)), //
				on(path("<cobol-directives:mf>/<cobol-directives:set>"), all( //
						rules.applyMatchingRule(), //
						atEnd(RETURN) //
				)), //
				atEnd(otherwise(createTuple("unknown_directive"))) //
		));

		// statement = $1 default other_stmt
		rules.define("<statement>", returnFirstChild(rules, OTHER_STMT));

		// NOTE. DO NOT DO nestedStatements == $* default other_stmt . We don't
		// return lists as the result of trapping a node. Lists are created and
		// updated when setting attributes.

		// compilerStatement == $1 default other_stmt
		rules.define("<compilerStatement>",
				returnFirstChild(rules, OTHER_STMT));

		// identifier == $1
		rules.define("<identifier>", returnFirstChild(rules));

		// identifier_format1 == $1
		rules.define("<identifier_format1>", returnFirstChild(rules));

		// identifier_format2 == {qualifiedDataName}
		rules.define("<identifier_format2>",
				returnChild(rules, "<qualifiedDataName>"));

		// literal == $1
		rules.define("<literal>", returnFirstChild(rules));

		// literalValue == $1
		rules.define("<literalValue>", returnFirstChild(rules));

		// numeric => @numeric_literal, .value <- $.
		rules.define("<numeric>", literalWithValue("numeric_literal"));

		// integer => @numeric_literal, .value <- $.
		rules.define("<integer>", literalWithValue("numeric_literal"));

		// alphanumericLiteral => @alphanumeric_literal, .value <- $.
		rules.define("<alphanumericLiteral>",
				literalWithValue("alphanumeric_literal"));

		// numericLiteral => @numeric_literal, .value <- $.
		rules.define("<numericLiteral>", literalWithValue("numeric_literal"));

		// literalValue/figurativeConstant => @figurative_constant_literal
		// .value <- $.
		// .literal <- {literal}
		rules.define("<literalValue>/<figurativeConstant>", all( //
				createTuple("figurative_constant_literal"), //
				setAttributeToProgramText("value"),
				setAttribute("literal", path("<literal>"), rules) //
		));

		// literalValue/true => @other_literal, .value <- $.
		rules.define("<literalValue>/<true>",
				literalWithValue("other_literal"));

		// literalValue/false => @other_literal, .value <- $.
		rules.define("<literalValue>/<false>",
				literalWithValue("other_literal"));

		// constant => @other_literal, .value <- $.
		rules.define("<constant>", literalWithValue("other_literal"));

		// declarativeSection == $. as section
		rules.define("<declarativeSection>", rules.applyRule(SECTION));

		// condition == $1
		rules.define("<condition>", returnFirstChild(rules));

		// End markers for statements. END-IF, END-READ, etc.
		// scope_terminator => @scope_terminator
		rules.define("<scope_terminator>", all( //
				createTuple("scope_terminator") //
		));

		// disjunction == foldl $* @log_or_expr
		rules.define("<disjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "log_or_expr") //
		);

		// conjunction == foldl $* @log_and_expr
		rules.define("<conjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "log_and_expr") //
		);

		// subscript == $1
		rules.define("<subscript>", returnFirstChild(rules));

		// directSubscript == $1
		rules.define("<directSubscript>", maybeReturnFirstChild(rules));

		// {directSubscript/identifier} => @relative_subscript
		// .reference <- {identifier_format2/qualifiedDataName} as
		// data_reference
		rules.define("<directSubscript>/<identifier>", all( //
				createTuple("relative_subscript"), //
				setAttributeAs("reference",
						path("<identifier_format2>/<qualifiedDataName>"), rules,
						DATA_REFERENCE) //
		));

		// classCondition
		// when {.[not]} => @not_class_condition_expr
		// .identifier <- {identifier}
		// .class_type <- {classType}
		// otherwise => @class_condition_expr
		// .identifier <- {identifier}
		// .class_type <- {classType}
		rules.define("<classCondition>", all( //
				createTuple("class_condition_expr"), //
				on(path("<not>"),
						overrideTupleType("not_class_condition_expr")), //
				setAttribute("identifier", path("<identifier>"), rules), //
				setAttribute("class_type", path("<classType>"), rules) //
		));

		// classType
		// when {name} => @user_defined_class_type, .name <- $.
		// otherwise => @predefined_class_type, .name <- $.
		rules.define("<classType>", all( //
				createTuple("predefined_class_type"), //
				on(path("<name>"), //
						overrideTupleType("user_defined_class_type") //
				), //
				setAttributeToProgramText("name") //
		));

		// signCondition
		// when {.[not]} => @not_sign_condition_expr
		// .expr <- {arithmeticExpression}
		// .sign_type <- {signType}
		// otherwise => @sign_condition_expr
		// .expr <- {arithmeticExpression}
		// .sign_type <- {signType}
		rules.define("<signCondition>", all( //
				createTuple("sign_condition_expr"), //
				on(path("<not>"), //
						overrideTupleType("not_sign_condition_expr")), //

				setAttribute("expr", path("<arithmeticExpression>"), rules), //
				setAttributeToProgramText("sign_type", path("<signType>")) //
		));

		// abbreviatedSignCondition
		// when {.[not]} => @abbr_not_sign_condition_expr
		// .sign_type <- {signType}
		// otherwise => @abbr_sign_condition_expr
		// .sign_type <- {signType}
		rules.define("<abbreviatedSignCondition>", all( //
				createTuple("abbr_sign_condition_expr"), //
				on(path("<not>"), //
						overrideTupleType("abbr_not_sign_condition_expr")), //
				setAttributeToProgramText("sign_type", path("<signType>")) //
		));

		// omittedArgumentCondition
		// when {.[not]} => @not_omitted_condition_expr
		// .data_name <- {dataName}
		// otherwise => @omitted_condition_expr
		// .data_name <- {dataName}
		rules.define("<omittedArgumentCondition>", all( //
				createTuple("omitted_condition_expr"), //
				on(path("<not>"), //
						overrideTupleType("not_omitted_condition_expr")), //
				setAttributeToProgramText("data_name", path("<dataName>")) //
		));

		// arithmeticExpression == $1
		rules.define("<arithmeticExpression>", returnFirstChild(rules));

		// expression == foldl $*
		// when add => @add_expr
		// when subtract => @sub_expr
		rules.define("<expression>",
				foldl(path("<>"), rules.applyMatchingRule(), //
						new SwitchFn1<Data, String>().put(ADD_EXPR, "add_expr")
								.put(SUBTRACT_EXPR, "sub_expr") //
				) //
		);

		// add == $1
		rules.define("<add>", returnFirstChild(rules));

		// subtract == $1
		rules.define("<subtract>", returnFirstChild(rules));

		// nested == $1
		rules.define("<nested>", returnFirstChildWithThisNode(rules));

		// term == foldl $*
		// when multiply => @mul_expr
		// when divide => @div_expr
		rules.define("<term>", //
				foldl(path("<>"), rules.applyMatchingRule(), //
						new SwitchFn1<Data, String>()
								.put(MULTIPLY_EXPR, "mul_expr")
								.put(DIVIDE_EXPR, "div_expr") //
				) //
		);

		// multiply == $1
		rules.define("<multiply>", returnFirstChild(rules));

		// divide == $1
		rules.define("<divide>", returnFirstChild(rules));

		// factor == foldl $*
		// when power => @pow_expr
		// otherwise => error $. "Unknown type of factor."
		// Redoing that as factor == foldl $* @pow_expr
		rules.define("<factor>", //
				foldl(path("<>"), rules.applyMatchingRule(), "pow_expr") //
		);

		// power == $1
		rules.define("<power>", returnFirstChild(rules));

		// bitwiseInclusiveDisjunction == foldl $* @bit_or_expr
		rules.define("<bitwiseInclusiveDisjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "bit_or_expr") //
		);

		// bitwiseExclusiveDisjunction == foldl $* @bit_x_or_expr
		rules.define("<bitwiseExclusiveDisjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "bit_x_or_expr") //
		);

		// bitwiseConjunction == foldl $* @bit_and_expr
		rules.define("<bitwiseConjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "bit_and_expr") //
		);

		// argument == $1
		rules.define("<argument>", returnFirstChild(rules));

		// parenthesizedCondition == $1
		rules.define("<parenthesizedCondition>", returnFirstChild(rules));

		// base
		// when {pos} => @plus_expr, .expression <- $2
		// when {neg} => @neg_expr, .expression <- $2
		// when {bNOT} => @bit_not_expr, .expression <- $2
		// otherwise => error $. "Unknown type of base."
		rules.define("<base>", all( //
				createTuple("plus_expr"), //
				on(path("<neg>"), overrideTupleType("neg_expr")), //
				on(path("<bNOT>"), overrideTupleType("bit_not_expr")), //
				setAttribute("expression", path("<>[2]"), rules) //
		));

		// abbreviatedDisjunction == foldl $* @abbr_log_or_expr
		rules.define("<abbreviatedDisjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "abbr_log_or_expr") //
		);

		// abbreviatedConjunction == foldl $* @abbr_log_and_expr
		rules.define("<abbreviatedConjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(),
						"abbr_log_and_expr") //
		);

		// relationObject => @relation_object_expr
		// .operator <- {relop}
		// .operand <- $-1
		rules.define("<relationObject>", all( //
				createTuple("relation_object_expr"), //
				setAttribute("operator", path("<relop>"), rules), //
				setAttribute("operand", path("<>[-1]"), rules) //
		));

		// relationObject/operand == $1
		rules.define("<relationObject>/<operand>", returnFirstChild(rules));

		// relop == $1
		// greaterOrEqualOp => @g_e_op
		// lessOrEqualOp => @l_e_op
		// greaterThanOp => @g_t_op
		// lessThanOp => @l_t_op
		// equalToOp => @eq_op
		// exceedsOp => @g_t_op
		// equalsOp => @eq_op
		// unequalToOp => @n_eq_op
		// Note: negation (<not>) has been normalized into the actual relop.
		// Cfr. NormalizeRelationOperators.
		rules.define("<relop>", returnFirstChild(rules));
		rules.define("<greaterOrEqualOp>", createTuple("g_e_op"));
		rules.define("<lessOrEqualOp>", createTuple("l_e_op"));
		rules.define("<greaterThanOp>", createTuple("g_t_op"));
		rules.define("<lessThanOp>", createTuple("l_t_op"));
		rules.define("<equalToOp>", createTuple("eq_op"));
		rules.define("<exceedsOp>", createTuple("g_t_op"));
		rules.define("<equalsOp>", createTuple("eq_op"));
		rules.define("<unequalToOp>", createTuple("n_eq_op"));

		// qualifiedReportCounter
		// when {lineCounter} => @qualified_line_counter, .qualification <-
		// {reportName/name}
		// when {pageCounter} => @qualified_page_counter, .qualification <-
		// {reportName/name}
		rules.define("<qualifiedReportCounter>", all( //
				createTuple("qualified_line_counter"), //
				on(path("<pageCounter>"),
						overrideTupleType("qualified_page_counter")), //
				setAttributeToProgramText("qualification",
						path("<reportName>/<name>")) //
		// on(path("<reportName>/<name>"), all( //
		// sub(all(collect(programText()), //
		// atEnd(assignTo("qualification")) //
		// )) //
		// )) //
		));

		// pictureClause => @picture_clause
		// .picture_string <- {pictureString}
		// in {pictureString} [PICTURE]
		rules.define("<pictureClause>", all( //
				createTuple("picture_clause"), //
				setAttributeToProgramText("picture_string",
						path("<pictureString>")), //
				// on(path("<pictureString>"), all( //
				// sub(all(collect(programText()), //
				// atEnd(assignTo("picture_string")) //
				// )) //
				// )), //
				atEnd(PICTURE) //
		));

		// rangeExpression : omitted
		rules.define("<rangeExpression>", MAY_BE_OMITTED);

		// lengthOf : omitted
		rules.define("<lengthOf>", MAY_BE_OMITTED);

		// performStatement
		// # Trap out-of-line perform statements.
		// when {.[procedureName]} => @perform_outofline
		// .procedure_name_1 <- {procedureName[1]}
		// procedure_name_2 <- {procedureName[2]}
		// .loop_form <- {until|varying|times}
		//
		// # Trap inline perform statements.
		// when {.[nestedStatements]} => @perform_inline
		// .statements <- {statement|nestedStatements|compilerStatement}
		// .loop_form <- {until|varying|times}
		//
		// # Trap empty inline perform statements.
		// otherwise => @perform_inline
		// .loop_form <- {until|varying|times}
		rules.define("<performStatement>", all( //
				createTuple("perform_inline"), //
				on(path("<procedureName>"),
						overrideTupleType("perform_outofline")), //
				setAttribute("procedure_name_1", path("<procedureName>[1]"),
						rules), //
				setAttribute("procedure_name_2", path("<procedureName>[2]"),
						rules), //
				setAttribute("statements",
						or(path("<statement>"),
								path("<nestedStatements>/<statement>"),
								path("<compilerStatement>")),
						rules), //
				setAttribute("loop_form",
						or(path("<until>"), path("<varying>"), path("<times>")),
						rules) //
		));

		// <SQL> searchCondition == $1
		rules.define("<sql:searchCondition>", returnFirstChild(rules));

		// <SQL> disjunction == foldl $* @sql_or_expr
		rules.define("<sql:disjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "sql_or_expr") //
		);

		// <SQL> conjunction == foldl $* @sql_and_expr
		rules.define("<sql:conjunction>", //
				foldl(path("<>"), rules.applyMatchingRule(), "sql_and_expr") //
		);

		// <SQL> term == $1
		rules.define("<sql:term>", returnFirstChildWithThisNode(rules));

		// <SQL> aggregateFunction == $1
		rules.define("<sql:aggregateFunction>", returnFirstChild(rules));

		// How to handle 'unknown's in SQL.
		// INTO unknown: unknown_spec
		// <SQL> into/unknown == $. as unknown_spec
		rules.define("<sql:into>/<sql:unknown>",
				rules.applyRule(SQL_UNKNOWN_SPEC));

		// unknown as part of a condition: unknown_expr
		// <SQL> term/unknown == $. as unknown_expr
		rules.define("<sql:term>/<sql:unknown>",
				rules.applyRule(SQL_UNKNOWN_EXPR));

		// unknown in general: unknown_expr
		// <SQL> unknown == $. as unknown_expr
		rules.define("<sql:unknown>", rules.applyRule(SQL_UNKNOWN_EXPR));

		// <SQL> comparisonOp == $1
		rules.define("<sql:comparisonOp>", returnFirstChild(rules));
	}
}
