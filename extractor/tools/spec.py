'''The Cobol data model.'''

import sys
import os

from AST import AstType, PrimitiveType, ClassType, UnionType, ListType
# from AST import INT, BOOL, STRING
from ASTExt import NodeType
AstType.prefix = ""
AstType.ast_node_name = "AstNode"

STRING = PrimitiveType("string", "varchar(1)")
#STRING_LIST = ListType(STRING)
INT = PrimitiveType("int", "int")
BOOL = PrimitiveType("bool", "boolean")

meta = NodeType("meta")
exit_node = NodeType("exit_node", meta, node_name="exit_node", node_ns="cflow")

literal = NodeType("literal")

literal.attribute("value", STRING)
alphanumeric_literal = NodeType("alphanumeric_literal", literal)
numeric_literal = NodeType("numeric_literal", literal)
figurative_constant_literal = NodeType("figurative_constant_literal", literal)
figurative_constant_literal.attribute("literal", literal)
other_literal = NodeType("other_literal", literal)

# These Kinds are a compromise between Cobol Standard 8.4.2.1
# and the current Koopa grammar.
identifier = NodeType("identifier")
identifier_list = ListType(identifier, private=False)

special_register = NodeType("special_register")

address_of_register = NodeType("address_of_register", special_register, node_name="addressOf")
address_of_register.attribute("data_item", identifier, parser_paths="{identifier}")

# EXPRESSIONS -----------------------------------------------------------------

expr = NodeType("expr")

# TODO Rethink where to fit this into the hierarchy...
figurative_constant_expr = NodeType("figurative_constant_expr", expr, node_name="figurativeConstant")
figurative_constant_expr.attribute("value", STRING, parser_paths="$.")

# Arithmetic expressions ... --------------------------------------------------

bit_or_expr   = NodeType("bit_or_expr",   expr)
bit_x_or_expr = NodeType("bit_x_or_expr", expr)
bit_and_expr  = NodeType("bit_and_expr",  expr)
add_expr      = NodeType("add_expr",      expr)
sub_expr      = NodeType("sub_expr",      expr)
mul_expr      = NodeType("mul_expr",      expr)
div_expr      = NodeType("div_expr",      expr)
pow_expr      = NodeType("pow_expr",      expr)
plus_expr     = NodeType("plus_expr",     expr)
neg_expr      = NodeType("neg_expr",      expr)
bit_not_expr  = NodeType("bit_not_expr",  expr)

identifier_expr = NodeType("identifier_expr", expr, node_name="identifierAtom")
identifier_expr.attribute("identifier", identifier, parser_paths="{identifier}")

zero_expr = NodeType("zero_expr", expr, node_name="zeroAtom")

numeric_expr = NodeType("numeric_expr", expr, node_name="numericAtom")
numeric_expr.attribute("literal", literal, parser_paths="{numeric}")

arithmetic_expr = UnionType("arithmetic_expr",
  bit_or_expr, bit_x_or_expr,
  bit_and_expr, bit_not_expr,
  add_expr, sub_expr,
  mul_expr, div_expr,
  pow_expr,
  plus_expr, neg_expr,
  identifier_expr, zero_expr, numeric_expr
)

bit_or_expr.attribute("left_operand", arithmetic_expr)
bit_or_expr.attribute("right_operand", arithmetic_expr)
bit_x_or_expr.attribute("left_operand", arithmetic_expr)
bit_x_or_expr.attribute("right_operand", arithmetic_expr)
bit_and_expr.attribute("left_operand", arithmetic_expr)
bit_and_expr.attribute("right_operand", arithmetic_expr)
add_expr.attribute("left_operand", arithmetic_expr)
add_expr.attribute("right_operand", arithmetic_expr)
sub_expr.attribute("left_operand", arithmetic_expr)
sub_expr.attribute("right_operand", arithmetic_expr)
mul_expr.attribute("left_operand", arithmetic_expr)
mul_expr.attribute("right_operand", arithmetic_expr)
div_expr.attribute("left_operand", arithmetic_expr)
div_expr.attribute("right_operand", arithmetic_expr)
pow_expr.attribute("left_operand", arithmetic_expr)
pow_expr.attribute("right_operand", arithmetic_expr)
plus_expr.attribute("expression", arithmetic_expr)
neg_expr.attribute("expression", arithmetic_expr)
bit_not_expr.attribute("expression", arithmetic_expr)

# Condition expressions ... ---------------------------------------------------

true_expr = NodeType("true_expr", expr, node_name="true")
false_expr = NodeType("false_expr", expr, node_name="false")

log_or_expr = NodeType("log_or_expr", expr)
log_and_expr = NodeType("log_and_expr", expr)
log_not_expr = NodeType("log_not_expr", expr, node_name="negation")

class_type = NodeType("class_type")
class_type.attribute("name", STRING)

predefined_class_type = NodeType("predefined_class_type", class_type)
user_defined_class_type = NodeType("user_defined_class_type", class_type)

class_condition_expr = NodeType("class_condition_expr", expr)
class_condition_expr.attribute("identifier", identifier)
class_condition_expr.attribute("class_type", class_type)

not_class_condition_expr = NodeType("not_class_condition_expr", expr)
not_class_condition_expr.attribute("identifier", identifier)
not_class_condition_expr.attribute("class_type", class_type)

sign_condition_expr = NodeType("sign_condition_expr", expr)
sign_condition_expr.attribute("expr", arithmetic_expr)
sign_condition_expr.attribute("sign_type", STRING)

abbr_sign_condition_expr = NodeType("abbr_sign_condition_expr", expr)
abbr_sign_condition_expr.attribute("sign_type", STRING)

not_sign_condition_expr = NodeType("not_sign_condition_expr", expr)
not_sign_condition_expr.attribute("expr", arithmetic_expr)
not_sign_condition_expr.attribute("sign_type", STRING)

abbr_not_sign_condition_expr = NodeType("abbr_not_sign_condition_expr", expr)
abbr_not_sign_condition_expr.attribute("sign_type", STRING)

condition_name_condition_expr = NodeType("condition_name_condition_expr", expr, node_name="conditionNameCondition")
condition_name_condition_expr.attribute("name", identifier, parser_paths="{conditionName/identifier}")

omitted_condition_expr = NodeType("omitted_condition_expr", expr)
omitted_condition_expr.attribute("data_name", STRING)

not_omitted_condition_expr = NodeType("not_omitted_condition_expr", expr)
not_omitted_condition_expr.attribute("data_name", STRING)

relation_operator = NodeType("relation_operator")

g_e_op = NodeType("g_e_op", relation_operator)
l_t_op = NodeType("l_t_op", relation_operator)
l_e_op = NodeType("l_e_op", relation_operator)
g_t_op = NodeType("g_t_op", relation_operator)
eq_op = NodeType("eq_op", relation_operator)
n_eq_op = NodeType("n_eq_op", relation_operator)

relation_operand = UnionType("relation_operand",
  literal, 
  identifier,
  figurative_constant_expr,
  address_of_register, 
  arithmetic_expr
)

abbr_log_or_expr = NodeType("abbr_log_or_expr", expr)
abbr_log_and_expr = NodeType("abbr_log_and_expr", expr)
abbr_log_not_expr = NodeType("abbr_log_not_expr", expr, node_name="abbreviatedNegation")
relation_object_expr = NodeType("relation_object_expr", expr)

abbr_log_expr = UnionType("abbr_log_expr",
  abbr_log_or_expr, 
  abbr_log_and_expr,
  abbr_log_not_expr, 
  relation_object_expr
)

abbr_log_or_expr.attribute("left_operand", abbr_log_expr)
abbr_log_or_expr.attribute("right_operand", abbr_log_expr)

abbr_log_and_expr.attribute("left_operand", abbr_log_expr)
abbr_log_and_expr.attribute("right_operand", abbr_log_expr)

abbr_log_not_expr.attribute("expression", abbr_log_expr, parser_paths="$1")

relation_object = UnionType("relation_object", 
  relation_operand, 
  abbr_log_expr
)

relation_object_expr.attribute("operator", relation_operator)
relation_object_expr.attribute("operand", relation_object)

comparison_expr = NodeType("comparison_expr", expr, node_name="relationCondition")
comparison_expr.attribute("subject", relation_operand, parser_paths="{relationSubject/$1}")
comparison_expr.attribute("object",  abbr_log_expr,    parser_paths="$-1")

condition_expr = UnionType("condition_expr",
  true_expr, false_expr,
  log_or_expr, log_and_expr, log_not_expr,
  class_condition_expr, not_class_condition_expr,
  sign_condition_expr, not_sign_condition_expr,
  abbr_sign_condition_expr, abbr_not_sign_condition_expr,
  condition_name_condition_expr,
  omitted_condition_expr, not_omitted_condition_expr,
  comparison_expr
)

log_or_expr.attribute("left_operand", condition_expr)
log_or_expr.attribute("right_operand", condition_expr)
log_and_expr.attribute("left_operand", condition_expr)
log_and_expr.attribute("right_operand", condition_expr)
log_not_expr.attribute("expression", condition_expr, parser_paths="$1")

# Binary expressions ... ------------------------------------------------------

binary_expr = UnionType("binary_expr",
  bit_or_expr, 
  bit_x_or_expr,
  bit_and_expr,
  add_expr, 
  sub_expr,
  mul_expr, 
  div_expr,
  pow_expr,
  log_or_expr, 
  log_and_expr,
  abbr_log_or_expr, 
  abbr_log_and_expr
)
binary_expr.attribute("left_operand", expr)
binary_expr.attribute("right_operand", expr)


# STATEMENTS ------------------------------------------------------------------

stmt = NodeType("stmt")
stmt_list = ListType(stmt, private=False)

clause = NodeType("clause")

phrase = NodeType("phrase")

until = NodeType("until", phrase, node_name="until")
until.attribute("condition", condition_expr, parser_paths="{condition}")

after = NodeType("after", phrase, node_name="after")
after.attribute("operand", identifier, parser_paths="{identifier[1]}")
after.attribute("until", until, parser_paths="{until}")

after_list = ListType(after, private=False)

varying = NodeType("varying", phrase, node_name="varying")
varying.attribute("operand", identifier, parser_paths="{identifier[1]}")
varying.attribute("until", until, parser_paths="{until}")
varying.attribute("after_list", after_list, parser_paths="{after}")

times = NodeType("times", phrase, node_name="times")

loop_form = UnionType("loop_form", 
  times, 
  until, 
  varying
)

scope_terminator = NodeType("scope_terminator", phrase)


branch = NodeType("branch")
branch.attribute("statements", stmt_list, parser_paths="{statement|nestedStatements|compilerStatement}")

branch_list = ListType(branch, private=False)

then_branch = NodeType("then_branch", branch, node_name="thenBranch")
#then_branch.attribute("statements", stmt_list, parser_paths="{statement|nestedStatements|compilerStatement}")

else_branch = NodeType("else_branch", branch, node_name="elseBranch")
#else_branch.attribute("statements", stmt_list, parser_paths="{statement|nestedStatements|compilerStatement}")

when_object = UnionType("when_object",
  literal,
  identifier, 
  condition_expr, 
  arithmetic_expr,
  relation_object_expr
)

when_object_list = ListType(when_object, private=False)

when_branch = NodeType("when_branch", branch, node_name="when")
when_branch.attribute("objects", when_object_list, parser_paths="{object/$*}")

when_other_branch = NodeType("when_other_branch", branch, node_name="whenOther")

on_exception_branch = NodeType("on_exception_branch", branch, node_name="onException")
not_on_exception_branch = NodeType("not_on_exception_branch", branch, node_name="notOnException")

on_size_error_branch = NodeType("on_size_error_branch", branch, node_name="onSizeError")
not_on_size_error_branch = NodeType("not_on_size_error_branch", branch, node_name="notOnSizeError")

on_overflow_branch = NodeType("on_overflow_branch", branch, node_name="onOverflow")
not_on_overflow_branch = NodeType("not_on_overflow_branch", branch, node_name="notOnOverflow")

invalid_key_branch = NodeType("invalid_key_branch", branch, node_name="invalidKey")
not_invalid_key_branch = NodeType("not_invalid_key_branch", branch, node_name="notInvalidKey")

at_end_branch = NodeType("at_end_branch", branch, node_name="atEnd")
not_at_end_branch = NodeType("not_at_end_branch", branch, node_name="notAtEnd")

at_end_of_page_branch = NodeType("at_end_of_page_branch", branch, node_name="atEndOfPage")
not_at_end_of_page_branch = NodeType("not_at_end_of_page_branch", branch, node_name="notAtEndOfPage")

with_data_branch = NodeType("with_data_branch", branch, node_name="withData")
no_data_branch = NodeType("no_data_branch", branch, node_name="noData")

on_escape_branch = NodeType("on_escape_branch", branch, node_name="onEscape")
not_on_escape_branch = NodeType("not_on_escape_branch", branch, node_name="notOnEscape")

other_stmt = NodeType("other_stmt", stmt, node_name="other_stmt")
other_stmt.attribute("on_exception",       on_exception_branch,       parser_paths="{onException}")
other_stmt.attribute("not_on_exception",   not_on_exception_branch,   parser_paths="{notOnException}")
other_stmt.attribute("on_size_error",      on_size_error_branch,      parser_paths="{onSizeError}")
other_stmt.attribute("not_on_size_error",  not_on_size_error_branch,  parser_paths="{notOnSizeError}")
other_stmt.attribute("on_overflow",        on_overflow_branch,        parser_paths="{onOverflow}")
other_stmt.attribute("not_on_overflow",    not_on_overflow_branch,    parser_paths="{notOnOverflow}")
other_stmt.attribute("invalid_key",        invalid_key_branch,        parser_paths="{invalidKey}")
other_stmt.attribute("not_invalid_key",    not_invalid_key_branch,    parser_paths="{notInvalidKey}")
other_stmt.attribute("at_end",             at_end_branch,             parser_paths="{atEnd}")
other_stmt.attribute("not_at_end",         not_at_end_branch,         parser_paths="{notAtEnd}")
other_stmt.attribute("at_end_of_page",     at_end_of_page_branch,     parser_paths="{atEndOfPage}")
other_stmt.attribute("not_at_end_of_page", not_at_end_of_page_branch, parser_paths="{notAtEndOfPage}")
other_stmt.attribute("with_data",          with_data_branch,          parser_paths="{withData}")
other_stmt.attribute("no_data",            no_data_branch,            parser_paths="{noData}")

if_then_else = NodeType("if_then_else", stmt, node_name="ifStatement")
if_then_else.attribute("condition",        condition_expr,   parser_paths="{condition}")
if_then_else.attribute("then",             then_branch,      parser_paths="{thenBranch}")
if_then_else.attribute("else",             else_branch,      parser_paths="{elseBranch}")
if_then_else.attribute("scope_terminator", scope_terminator, parser_paths="{end} as scope_terminator")

evaluate_subject = UnionType("evaluate_subject",
  literal, 
  identifier, 
  condition_expr, 
  arithmetic_expr
)

evaluate_subject_list = ListType(evaluate_subject, private=False)

evaluate = NodeType("evaluate", stmt, node_name="evaluateStatement")
evaluate.attribute("subjects",         evaluate_subject_list, parser_paths="{subject/$*}")
evaluate.attribute("branches",         branch_list,           parser_paths="{when|whenOther}")
evaluate.attribute("scope_terminator", scope_terminator,      parser_paths="{end} as scope_terminator")

perform_inline = NodeType("perform_inline", stmt)
perform_outofline = NodeType("perform_outofline", stmt)

perform = UnionType("perform",
  perform_inline, 
  perform_outofline
)
perform.attribute("loop_form", loop_form)


# Cobol standard 8.4.1.1.1
reference = NodeType("reference")
# We don't want this in the dbscheme, as each case type will track this on its own.
reference.attribute("name", STRING, ql_ignore=True, dbscheme_ignore=True)

# Format 4 (qualified-procedure-name)
procedure_reference = NodeType("procedure_reference", reference, node_name="procedureName")
procedure_reference.attribute("name",          STRING, parser_paths="{name}")
procedure_reference.attribute("qualification", STRING, parser_paths="{sectionName}")

procedure_reference_list = ListType(procedure_reference, private=False)

# Format 1 (qualified-data-name)
qualifiers_list = ListType(STRING, "qualifiers_list", private=False)
data_reference = NodeType("data_reference", reference, node_name="data_reference")
data_reference.attribute("name",       STRING,      parser_paths="{dataName}")
data_reference.attribute("qualifiers", qualifiers_list, parser_paths="{qualifier/dataName}")

# File reference don't appear as such in the spec, but still are a useful abstraction.
file_reference = NodeType("file_reference", reference, node_name="fileName")
file_reference.attribute("name", STRING, parser_paths="$.")

file_reference_list = ListType(file_reference, private=False)

# Format 1 (function-identifier)
argument = UnionType("argument",
  identifier, 
  literal, 
  arithmetic_expr
)

argument_list = ListType(argument, private=False)

function_identifier = NodeType("function_identifier", identifier, node_name="function")
function_identifier.attribute("name",      STRING,  parser_paths="{functionName}")
function_identifier.attribute("arguments", argument_list, parser_paths="{argument}")

# Format 2 (qualified-data-name-with-subscripts)
relative_subscript = NodeType("relative_subscript", expr, node_name="relativeSubscript")
relative_subscript.attribute("reference", reference, parser_paths="{identifier/identifier_format2/qualifiedDataName} as data_reference")
relative_subscript.attribute("modifier",  INT, parser_paths="{integer}")

subscript = UnionType("subscript", 
  relative_subscript, 
  literal
)

subscript_list = ListType(subscript, private=False)

reference_modifier_t = NodeType("reference_modifier_t")
reference_modifier = NodeType("reference_modifier", reference_modifier_t, node_name="referenceModifier")
reference_modifier.attribute("start", arithmetic_expr, parser_paths="{arithmeticExpression[1]}")
reference_modifier.attribute("length", arithmetic_expr, parser_paths="{arithmeticExpression[2]}")

qualified_data_name_with_subscripts = NodeType("qualified_data_name_with_subscripts", identifier, node_name="qualifiedDataName")
qualified_data_name_with_subscripts.attribute("reference",  data_reference, parser_paths="$. as data_reference")
qualified_data_name_with_subscripts.attribute("subscripts", subscript_list, parser_paths="{subscript}")
qualified_data_name_with_subscripts.attribute("reference_modifier", reference_modifier, parser_paths="{../referenceModifier}")

# Format 6 (predefined-object)
predefined_object = NodeType("predefined_object", identifier, node_name="identifier_format6")
predefined_object.attribute("value", STRING, parser_paths="$.")

# Format 9 part 1 (data-address-identifier)
data_address_identifier = NodeType("data_address_identifier", identifier, node_name="dataAddressIdentifier")
data_address_identifier.attribute("identifier", identifier, parser_paths="{identifier}")

# Format 10 (qualified-linage-counter)
qualified_linage_counter = NodeType("qualified_linage_counter", identifier, node_name="qualifiedLinageCounter")
qualified_linage_counter.attribute("qualification", STRING, parser_paths="{fileName/name}")

# Format 11 (qualified-report-counter)
qualified_line_counter = NodeType("qualified_line_counter", identifier)
qualified_line_counter.attribute("qualification", STRING)
qualified_page_counter = NodeType("qualified_page_counter", identifier)
qualified_page_counter.attribute("qualification", STRING)

qualified_report_counter = UnionType("qualified_report_counter", 
  qualified_line_counter, 
  qualified_page_counter
)

perform_inline.attribute("loop_form", loop_form)
perform_inline.attribute("statements", stmt_list)

perform_outofline.attribute("procedure_name_1", procedure_reference)
perform_outofline.attribute("procedure_name_2", procedure_reference)
perform_outofline.attribute("loop_form", loop_form)


cics_option_reference = NodeType("cics_option_reference", reference, node_name="cics_option_ref")
cics_option_reference.attribute("value", STRING, parser_paths="$.")

cics = NodeType("cics", stmt, node_name="execCICSStatement")
cics.attribute("command", STRING, parser_paths="cics::{cicsStatement/command}")
cics.attribute("map",   cics_option_reference, parser_paths="cics::{cicsStatement/option/map/value}   as cics_option_ref")
cics.attribute("queue", cics_option_reference, parser_paths="cics::{cicsStatement/option/queue/value} as cics_option_ref")
cics.attribute("file",  cics_option_reference, parser_paths="cics::{cicsStatement/option/file/value}  as cics_option_ref")
cics.attribute("resp",  cics_option_reference, parser_paths="cics::{cicsStatement/option/resp/value}  as cics_option_ref")

entry = NodeType("entry", stmt, node_name="entryStatement")

depending_on_clause = NodeType("depending_on_clause", clause, node_name="dependingOn")

go_to = NodeType("go_to", stmt, node_name="goToStatement")
go_to.attribute("targets", procedure_reference_list, parser_paths="{procedureName}")
go_to.attribute("depending_on", depending_on_clause, parser_paths="{dependingOn}")

goback = NodeType("goback", stmt, node_name="gobackStatement")

stop = NodeType("stop", stmt, node_name="stopStatement")
stop.attribute("endpoint", STRING, parser_paths="{endpoint}")
stop.attribute("literal", STRING, parser_paths="{literal}")

exit = NodeType("exit", stmt, node_name="exitStatement")
exit.attribute("endpoint", STRING, parser_paths="{endpoint}")

alteration_clause = NodeType("alteration_clause", clause, node_name="alterationClause")
alteration_clause.attribute("from", procedure_reference, parser_paths="{procedureName[1]}")
alteration_clause.attribute("to", procedure_reference, parser_paths="{procedureName[2]}")

alteration_clause_list = ListType(alteration_clause, private=False)

alter = NodeType("alter", stmt, node_name="alterStatement")
alter.attribute("alterations", alteration_clause_list, parser_paths="{alterationClause}")

use = NodeType("use", stmt, node_name="useStatement")

search = NodeType("search", stmt, node_name="searchStatement")
search.attribute("branches",         branch_list,      parser_paths="{atEnd|when}")
search.attribute("scope_terminator", scope_terminator, parser_paths="{end} as scope_terminator")

next_sentence = NodeType("next_sentence", stmt, node_name="nextSentenceStatement")

continue_stmt = NodeType("continue_stmt", stmt, node_name="continueStatement")

sort = NodeType("sort", stmt, node_name="sortStatement")
sort.attribute("file",     file_reference,      parser_paths="{fileName}")
sort.attribute("using",    file_reference_list, parser_paths="{using/fileName}")
sort.attribute("giving",   file_reference_list, parser_paths="{giving/fileName}")

merge = NodeType("merge", stmt, node_name="mergeStatement")
merge.attribute("file",     file_reference,      parser_paths="{fileName}")
merge.attribute("using",    file_reference_list, parser_paths="{using/fileName}")
merge.attribute("giving",   file_reference_list, parser_paths="{giving/fileName}")

read = NodeType("read", stmt, node_name="readStatement")
read.attribute("file",             file_reference,         parser_paths="{fileName}")
read.attribute("invalid_key",      invalid_key_branch,     parser_paths="{invalidKey}")
read.attribute("not_invalid_key",  not_invalid_key_branch, parser_paths="{notInvalidKey}")
read.attribute("at_end",           at_end_branch,          parser_paths="{atEnd}")
read.attribute("not_at_end",       not_at_end_branch,      parser_paths="{notAtEnd}")
read.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")

delete = NodeType("delete", stmt, node_name="deleteStatement")
delete.attribute("file",             file_reference,         parser_paths="{fileName}")
delete.attribute("invalid_key",      invalid_key_branch,     parser_paths="{invalidKey}")
delete.attribute("not_invalid_key",  not_invalid_key_branch, parser_paths="{notInvalidKey}")
delete.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")

return_stmt = NodeType("return_stmt", stmt, node_name="returnStatement")
return_stmt.attribute("file",             file_reference,    parser_paths="{fileName}")
return_stmt.attribute("at_end",           at_end_branch,     parser_paths="{atEnd}")
return_stmt.attribute("not_at_end",       not_at_end_branch, parser_paths="{notAtEnd}")
return_stmt.attribute("scope_terminator", scope_terminator,  parser_paths="{end} as scope_terminator")

start = NodeType("start", stmt, node_name="startStatement")
start.attribute("file",             file_reference,         parser_paths="{fileName}")
start.attribute("invalid_key",      invalid_key_branch,     parser_paths="{invalidKey}")
start.attribute("not_invalid_key",  not_invalid_key_branch, parser_paths="{notInvalidKey}")
start.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")

write = NodeType("write", stmt, node_name="writeStatement")
write.attribute("file",               file_reference,            parser_paths="{fileName}")
write.attribute("record",             identifier,                parser_paths="{recordName/identifier}")
write.attribute("invalid_key",        invalid_key_branch,        parser_paths="{invalidKey}")
write.attribute("not_invalid_key",    not_invalid_key_branch,    parser_paths="{notInvalidKey}")
write.attribute("at_end",             at_end_branch,             parser_paths="{atEnd}")
write.attribute("not_at_end",         not_at_end_branch,         parser_paths="{notAtEnd}")
write.attribute("at_end_of_page",     at_end_of_page_branch,     parser_paths="{atEndOfPage}")
write.attribute("not_at_end_of_page", not_at_end_of_page_branch, parser_paths="{notAtEndOfPage}")
write.attribute("scope_terminator",   scope_terminator,          parser_paths="{end} as scope_terminator")

rewrite = NodeType("rewrite", stmt, node_name="rewriteStatement")
rewrite.attribute("file",             file_reference,         parser_paths="{fileName}")
rewrite.attribute("record",           identifier,             parser_paths="{recordName/identifier}")
rewrite.attribute("invalid_key",      invalid_key_branch,     parser_paths="{invalidKey}")
rewrite.attribute("not_invalid_key",  not_invalid_key_branch, parser_paths="{notInvalidKey}")
rewrite.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")


identifier_or_literal = UnionType("identifier_or_literal", 
  identifier, 
  literal
)

copy = NodeType("copy", stmt, node_name="copyStatement")
copy.attribute("text_name",    STRING, parser_paths="{textName}")
copy.attribute("library_name", STRING, parser_paths="{libraryName}")

replace = NodeType("replace", stmt, node_name="replaceStatement")


corresponding_clause = NodeType("corresponding_clause", clause, node_name="corresponding")
corresponding_clause.attribute("identifier", identifier, parser_paths="{identifier}")

move = NodeType("move", stmt, node_name="moveStatement")
move.attribute("corresponding",   corresponding_clause, parser_paths="{corresponding}")
move.attribute("initial_operand", identifier,           parser_paths="{sending/identifier}")
move.attribute("to_operands",     identifier_list,      parser_paths="{identifier}")

add = NodeType("add", stmt, node_name="addStatement")
add.attribute("corresponding",     corresponding_clause,     parser_paths="{corresponding}")
add.attribute("initial_operands",  identifier_list,          parser_paths="{identifier}")
add.attribute("to_operands",       identifier_list,          parser_paths="{to/identifier|to/qualifiedDataName}")
add.attribute("giving_operands",   identifier_list,          parser_paths="{giving/identifier}")
add.attribute("on_size_error",     on_size_error_branch,     parser_paths="{onSizeError}")
add.attribute("not_on_size_error", not_on_size_error_branch, parser_paths="{notOnSizeError}")
add.attribute("scope_terminator",  scope_terminator,         parser_paths="{end} as scope_terminator")

subtract = NodeType("subtract", stmt, node_name="subtractStatement")
subtract.attribute("corresponding",     corresponding_clause,     parser_paths="{corresponding}")
subtract.attribute("initial_operands",  identifier_list,          parser_paths="{identifier}")
subtract.attribute("from_operands",     identifier_list,          parser_paths="{from/identifier}")
subtract.attribute("giving_operands",   identifier_list,          parser_paths="{giving/identifier}")
subtract.attribute("on_size_error",     on_size_error_branch,     parser_paths="{onSizeError}")
subtract.attribute("not_on_size_error", not_on_size_error_branch, parser_paths="{notOnSizeError}")
subtract.attribute("scope_terminator",  scope_terminator,         parser_paths="{end} as scope_terminator")

multiply = NodeType("multiply", stmt, node_name="multiplyStatement")
multiply.attribute("initial_operand",   identifier,               parser_paths="{identifier}")
multiply.attribute("by_operands",       identifier_list,          parser_paths="{by/identifier|by/qualifiedDataName}")
multiply.attribute("giving_operands",   identifier_list,          parser_paths="{giving/identifier}")
multiply.attribute("on_size_error",     on_size_error_branch,     parser_paths="{onSizeError}")
multiply.attribute("not_on_size_error", not_on_size_error_branch, parser_paths="{notOnSizeError}")
multiply.attribute("scope_terminator",  scope_terminator,         parser_paths="{end} as scope_terminator")

divide = NodeType("divide", stmt, node_name="divideStatement")
divide.attribute("initial_operand",   identifier,               parser_paths="{identifier}")
divide.attribute("into_operands",     identifier_list,          parser_paths="{into/identifier|into/qualifiedDataName}")
divide.attribute("giving_operands",   identifier_list,          parser_paths="{giving/identifier}")
divide.attribute("remainder_operand", identifier,               parser_paths="{remainder/identifier}")
divide.attribute("on_size_error",     on_size_error_branch,     parser_paths="{onSizeError}")
divide.attribute("not_on_size_error", not_on_size_error_branch, parser_paths="{notOnSizeError}")
divide.attribute("scope_terminator",  scope_terminator,         parser_paths="{end} as scope_terminator")

compute = NodeType("compute", stmt, node_name="computeStatement")
compute.attribute("result_operands",   identifier_list,          parser_paths="{qualifiedDataName}")
compute.attribute("expr",              arithmetic_expr,          parser_paths="{arithmeticExpression}")
compute.attribute("on_size_error",     on_size_error_branch,     parser_paths="{onSizeError}")
compute.attribute("not_on_size_error", not_on_size_error_branch, parser_paths="{notOnSizeError}")
compute.attribute("scope_terminator",  scope_terminator,         parser_paths="{end} as scope_terminator")

open = NodeType("open", stmt, node_name="openStatement")
open.attribute("files", file_reference_list, parser_paths="{fileName}")

close = NodeType("close", stmt, node_name="closeStatement")
close.attribute("files", file_reference_list, parser_paths="{fileName}")


omitted = NodeType("omitted", phrase, node_name="omitted")

program_name = UnionType("program_name",
  literal, 
  identifier
)

call_arg = NodeType("call_arg")

call_arg_value = UnionType("call_arg_value",
  literal,
  identifier,
  arithmetic_expr,
  address_of_register,
  omitted
  # TODO LENGTH OF...
)

call_arg_by_reference = NodeType("call_arg_by_reference", call_arg, node_name="callStatement/using/byReference/arg")
call_arg_by_reference.attribute("value", call_arg_value, parser_paths="{$1}")

call_arg_by_content = NodeType("call_arg_by_content", call_arg, node_name="callStatement/using/byContent/arg")
call_arg_by_content.attribute("value", call_arg_value, parser_paths="{$1}")

call_arg_by_value = NodeType("call_arg_by_value", call_arg, node_name="callStatement/using/byValue/arg")
call_arg_by_value.attribute("value", call_arg_value, parser_paths="{$1}")

call_arg_copied = NodeType("call_arg_copied", call_arg, node_name="callStatement/using/copyStatement")
call_arg_copied.attribute("copy", copy, parser_paths="$. as copyStatement")

call_arg_list = ListType(call_arg, private=False)

call_arg_with_value = UnionType("call_arg_with_value",
  call_arg_by_reference,
  call_arg_by_content,
  call_arg_by_value
)
call_arg_with_value.attribute("value", call_arg_value)

call_giving = UnionType("call_giving",
  identifier,
  address_of_register
)

call = NodeType("call", stmt, node_name="callStatement")
call.attribute("program_name",     program_name,            parser_paths="{programName/$1}")
call.attribute("using",            call_arg_list,           parser_paths="{using/$*/arg|using/copyStatement}")
call.attribute("giving",           call_giving,             parser_paths="{giving/addressOf|giving/identifier}")
call.attribute("on_overflow",      on_overflow_branch,      parser_paths="{onOverflow}")
call.attribute("on_exception",     on_exception_branch,     parser_paths="{onException}")
call.attribute("not_on_exception", not_on_exception_branch, parser_paths="{notOnException}")
call.attribute("scope_terminator", scope_terminator,        parser_paths="{end} as scope_terminator")

accept = NodeType("accept", stmt, node_name="acceptStatement")
accept.attribute("on_exception",     on_exception_branch,     parser_paths="{acceptFromMnemonic/onException|acceptScreenFormat/onException}")
accept.attribute("not_on_exception", not_on_exception_branch, parser_paths="{acceptFromMnemonic/notOnException|acceptScreenFormat/notOnException}")
accept.attribute("on_escape",        on_escape_branch,        parser_paths="{acceptFromMnemonic/onEscape|acceptScreenFormat/onEscape}")
accept.attribute("not_on_escape",    not_on_escape_branch,    parser_paths="{acceptFromMnemonic/notOnEscape|acceptScreenFormat/notOnEscape}")
accept.attribute("scope_terminator", scope_terminator,        parser_paths="{end} as scope_terminator")

display = NodeType("display", stmt, node_name="displayStatement")
display.attribute("scope_terminator", scope_terminator, parser_paths="{end} as scope_terminator")
display.attribute("on_exception",     on_exception_branch,     parser_paths="{onException}")
display.attribute("not_on_exception", not_on_exception_branch, parser_paths="{notOnException}")

# Added _stmt postfix to prefect clash with string type.
string_stmt = NodeType("string_stmt", stmt, node_name="stringStatement")
string_stmt.attribute("on_overflow",      on_overflow_branch,     parser_paths="{onOverflow}")
string_stmt.attribute("not_on_overflow",  not_on_overflow_branch, parser_paths="{notOnOverflow}")
string_stmt.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")

unstring = NodeType("unstring", stmt, node_name="unstringStatement")
unstring.attribute("on_overflow",      on_overflow_branch,     parser_paths="{onOverflow}")
unstring.attribute("not_on_overflow",  not_on_overflow_branch, parser_paths="{notOnOverflow}")
unstring.attribute("scope_terminator", scope_terminator,       parser_paths="{end} as scope_terminator")

computational_stmt = UnionType("computational_stmt",
  add,
  subtract,
  multiply,
  divide,
  compute
)
computational_stmt.attribute("on_size_error",     on_size_error_branch)
computational_stmt.attribute("not_on_size_error", not_on_size_error_branch)


header = NodeType("header")
other_header = NodeType("other_header", header, node_name="header")

unit = NodeType("unit")
unit.attribute("header", header, parser_paths="{header}")
unit.also_trap("NUMLINES")

unit_list = ListType(unit, private=False)

description_entry = NodeType("description_entry")
description_entry_list = ListType(description_entry, private=False)
description_entry.attribute("name", STRING)
description_entry.attribute("nested_entries", description_entry_list, artificial=True)

identification_division = NodeType("identification_division", unit, node_name="identificationDivision")
identification_division.attribute("name", STRING, parser_paths="{.//programName}")

numeric_sign_clause = NodeType("numeric_sign_clause", clause, node_name="specialNameStatement/numericSignIs")
numeric_sign_clause.attribute("position", STRING, parser_paths="{leading|trailing}")
numeric_sign_clause.attribute("separate", STRING, parser_paths="{separate}")

special_names_paragraph = NodeType("special_names_paragraph", unit, node_name="specialNamesParagraph")
special_names_paragraph.attribute("numeric_sign", numeric_sign_clause, parser_paths="{specialNameStatement/numericSignIs}")

configuration_section = NodeType("configuration_section", unit, node_name="configurationSection")
configuration_section.attribute("special_names", special_names_paragraph, parser_paths="{specialNamesParagraph}")

file_control_entry = NodeType("file_control_entry", description_entry, node_name="selectStatement")
file_control_entry.attribute("name", STRING, parser_paths="{selectClause/fileName}")
file_control_entry.attribute("file_status", data_reference, parser_paths="{fileStatusClause/qualifiedDataName} as data_reference")

file_control_entry_list = ListType(file_control_entry, private=False)

file_control_paragraph = NodeType("file_control_paragraph", unit, node_name="fileControlParagraph")
file_control_paragraph.attribute("entries", file_control_entry_list, parser_paths="{fileControlEntry/selectStatement}")

io_section = NodeType("io_section", unit, node_name="ioSection")
io_section.attribute("file_control_paragraph", file_control_paragraph, parser_paths="{fileControlParagraph}")


object_section = NodeType("object_section", unit, node_name="objectSection")

environment_division = NodeType("environment_division", unit, node_name="environmentDivision")
environment_division.attribute("configuration_section", configuration_section, parser_paths="{configurationSection}")
environment_division.attribute("io_section",            io_section,            parser_paths="{ioSection}")
environment_division.attribute("object_section",        object_section,        parser_paths="{objectSection}")


picture_clause = NodeType("picture_clause", clause)
picture_clause.attribute("picture_string", STRING)
picture_clause.attribute("normalized_picture_string", STRING)
picture_clause.attribute("category", STRING)

usage_clause = NodeType("usage_clause", clause, node_name="usageClause")
usage_clause.attribute("operand", STRING, parser_paths="{usageOperand}")

value_clause = NodeType("value_clause", clause, node_name="valueClause")
value_clause.attribute("literal", literal, parser_paths="{literal[1]}")
value_clause.attribute("through", literal, parser_paths="{literal[2]}")

indices_list = ListType(STRING, "indices_list", private=False)
indexed_by = NodeType("indexed_by", phrase, node_name="indexedBy")
indexed_by.attribute("indices", indices_list, parser_paths="{indexName}")

occurs_clause = NodeType("occurs_clause", clause, node_name="occursClause")
# TODO Can have references on min and max.
occurs_clause.attribute("minimum",    INT,      parser_paths="{fixed/min/integer[integerLiteral]}")
occurs_clause.attribute("maximum",    INT,      parser_paths="{fixed/max/integer[integerLiteral]}")
occurs_clause.attribute("object",     data_reference, parser_paths="{dependingOn/qualifiedDataName} as data_reference")
occurs_clause.attribute("indexed_by", indexed_by,     parser_paths="{indexedBy}")

sign_clause = NodeType("sign_clause", clause, node_name="signClause")
sign_clause.attribute("position", STRING, parser_paths="{leading|trailing}")
sign_clause.attribute("separate", STRING, parser_paths="{separate}")

data_description_entry = NodeType("data_description_entry", description_entry, node_name="dataDescriptionEntry")
data_description_entry.attribute("name",         STRING,         parser_paths="{entryName}")
data_description_entry.attribute("level_number", INT,            parser_paths="{levelNumber}")
data_description_entry.attribute("picture",      picture_clause, parser_paths="{pictureClause}")
data_description_entry.attribute("usage",        usage_clause,   parser_paths="{usageClause}")
data_description_entry.attribute("occurs",       occurs_clause,  parser_paths="{occursClause}")
data_description_entry.attribute("value",        value_clause,   parser_paths="{valueClause}")
data_description_entry.attribute("sign",         sign_clause,    parser_paths="{signClause}")
data_description_entry.attribute("redefines",    data_reference, parser_paths="{redefinesClause} as data_reference")

constant_entry = NodeType("constant_entry", description_entry, node_name="constantEntry")
constant_entry.attribute("name",         STRING, parser_paths="{entryName}")
constant_entry.attribute("level_number", INT,    parser_paths="{levelNumber}")

copy_entry = NodeType("copy_entry", description_entry)
copy_entry.attribute("copy", copy)

block_contains_clause = NodeType("block_contains_clause", clause, node_name="blockContainsClause")
block_contains_clause.attribute("minimum_size", INT, parser_paths="{integer[1]}")
block_contains_clause.attribute("maximum_size", INT, parser_paths="{integer[2]}")

file_description_entry = NodeType("file_description_entry", description_entry, node_name="fdFileDescriptionEntry")
file_description_entry.attribute("name", STRING, parser_paths="{fileName}")
file_description_entry.attribute("block_contains", block_contains_clause, parser_paths="{blockContainsClause}")

sort_merge_file_description_entry = NodeType("sort_merge_file_description_entry", description_entry, node_name="sdFileDescriptionEntry")
sort_merge_file_description_entry.attribute("name", STRING, parser_paths="{fileName}")
sort_merge_file_description_entry.attribute("block_contains", block_contains_clause, parser_paths="{blockContainsClause}")

file_description = UnionType("file_description",
  file_description_entry,
  sort_merge_file_description_entry
)

report_description_entry = NodeType("report_description_entry", description_entry, node_name="reportDescriptionEntry")
report_description_entry.attribute("name", STRING, parser_paths="{reportName}")

report_group_description_entry = NodeType("report_group_description_entry", description_entry, node_name="reportGroupDescriptionEntry")
report_group_description_entry.attribute("name",         STRING,   parser_paths="{dataName}")
report_group_description_entry.attribute("level_number", INT,      parser_paths="{levelNumber}")
report_group_description_entry.attribute("picture",      picture_clause, parser_paths="{pictureClause}")
report_group_description_entry.attribute("occurs",       occurs_clause,  parser_paths="{occursClause}")

screen_description_entry = NodeType("screen_description_entry", description_entry, node_name="screenDescriptionEntry")
screen_description_entry.attribute("name",         STRING,   parser_paths="{screenName}")
screen_description_entry.attribute("level_number", INT,      parser_paths="{levelNumber}")
screen_description_entry.attribute("picture",      picture_clause, parser_paths="{pictureClause}")
screen_description_entry.attribute("usage",        usage_clause,   parser_paths="{usageClause}")
screen_description_entry.attribute("occurs",       occurs_clause,  parser_paths="{occursClause}")

file_section = NodeType("file_section", unit, node_name="fileSection")
file_section.attribute("entries", description_entry_list, parser_paths=[
        "map {fileDescriptionEntry/$* | recordDescriptionEntry/$*}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

working_storage_section = NodeType("working_storage_section", unit, node_name="workingStorageSection")
working_storage_section.attribute("entries", description_entry_list, parser_paths=[
        "map {recordDescriptionEntry/$*}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

thread_local_storage_section = NodeType("thread_local_storage_section", unit, node_name="threadLocalStorageSection")
thread_local_storage_section.attribute("entries", description_entry_list, parser_paths=[
        "map {recordDescriptionEntry/$*}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

object_storage_section = NodeType("object_storage_section", unit, node_name="objectStorageSection")

local_storage_section = NodeType("local_storage_section", unit, node_name="localStorageSection")
local_storage_section.attribute("entries", description_entry_list, parser_paths=[
        "map {recordDescriptionEntry/$*}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

linkage_section = NodeType("linkage_section", unit, node_name="linkageSection")
linkage_section.attribute("entries", description_entry_list, parser_paths=[
        "map {recordDescriptionEntry/$*}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

communication_section = NodeType("communication_section", unit, node_name="communicationSection")

report_section = NodeType("report_section", unit, node_name="reportSection")
report_section.attribute("entries", description_entry_list, parser_paths=[
        "map {reportDescriptionEntry | reportGroupDescriptionEntry}",
        "[NESTED_RECORD_STRUCTURE]"
    ])

screen_section = NodeType("screen_section", unit, node_name="screenSection")
screen_section.attribute("entries", description_entry_list, parser_paths=[
        "map {screenDescriptionEntry}",
        "[NESTED_RECORD_STRUCTURE]"
    ])


data_division = NodeType("data_division", unit, node_name="dataDivision")
data_division.attribute("file_section",                 file_section,                 parser_paths="{fileSection}")
data_division.attribute("working_storage_section",      working_storage_section,      parser_paths="{workingStorageSection}")
data_division.attribute("thread_local_storage_section", thread_local_storage_section, parser_paths="{threadLocalStorageSection}")
data_division.attribute("object_storage_section",       object_storage_section,       parser_paths="{objectStorageSection}")
data_division.attribute("local_storage_section",        local_storage_section,        parser_paths="{localStorageSection}")
data_division.attribute("linkage_section",              linkage_section,              parser_paths="{linkageSection}")
data_division.attribute("communication_section",        communication_section,        parser_paths="{communicationSection}")
data_division.attribute("report_section",               report_section,               parser_paths="{reportSection}")
data_division.attribute("screen_section",               screen_section,               parser_paths="{screenSection}")

sentence = NodeType("sentence", unit, node_name="sentence")
sentence.attribute("statements", stmt_list, parser_paths="{statement|nestedStatements|compilerStatement}")

sentence_list = ListType(sentence, private=False)

paragraph = NodeType("paragraph", unit, node_name="paragraph")
paragraph.attribute("name",      STRING,  parser_paths="{paragraphName}")
paragraph.attribute("sentences", sentence_list, parser_paths="{sentence}")
paragraph.attribute("exit_node", exit_node,     parser_paths="{(.//exit_node)[last()]}")

paragraph_list = ListType(paragraph, private=False)

section = NodeType("section", unit, node_name="section")
section.attribute("name",       STRING,   parser_paths="{sectionName}")
section.attribute("sentences",  sentence_list,  parser_paths="{sentence}")
section.attribute("paragraphs", paragraph_list, parser_paths="{paragraph}")
section.attribute("exit_node",  exit_node,      parser_paths="{(.//exit_node)[last()]}")

section_list = ListType(section, private=False)

declaratives = NodeType("declaratives", unit, node_name="declaratives")
declaratives.attribute("sections", section_list, parser_paths="{.//declarativeSection}")


optional = NodeType("optional", phrase, node_name="optional")

procedure_division_parameter = NodeType("procedure_division_parameter")
procedure_division_parameter.attribute("optional", optional, parser_paths="{optional}")
# TODO Set up a rule for dataName instead ? Alias the qualifiedVersion somehow ?
procedure_division_parameter.attribute("value", identifier, parser_paths="{value[dataName]} as qualifiedDataName")

procedure_division_parameter_by_reference = NodeType("procedure_division_parameter_by_reference", procedure_division_parameter, node_name="procedureDivision/header/using/byReference/arg")
procedure_division_parameter_by_value     = NodeType("procedure_division_parameter_by_value",     procedure_division_parameter, node_name="procedureDivision/header/using/byValue/arg")
procedure_division_parameter_by_output    = NodeType("procedure_division_parameter_by_output",    procedure_division_parameter, node_name="procedureDivision/header/using/byOutput/arg")

procedure_division_parameter_list = ListType(procedure_division_parameter, private=False)

procedure_division_header = NodeType("procedure_division_header", header, node_name="procedureDivision/header")
procedure_division_header.attribute("using",     procedure_division_parameter_list, parser_paths="{using/$*/arg}")
procedure_division_header.attribute("returning", identifier,                        parser_paths="{returning[dataName]} as qualifiedDataName")

procedure_division = NodeType("procedure_division", unit, node_name="procedureDivision")
procedure_division.attribute("header",       procedure_division_header, parser_paths="{header}")
procedure_division.attribute("declaratives", declaratives,              parser_paths="{declaratives}")
procedure_division.attribute("sentences",    sentence_list,             parser_paths="{sentence}")
procedure_division.attribute("paragraphs",   paragraph_list,            parser_paths="{paragraph}")
procedure_division.attribute("sections",     section_list,              parser_paths="{section}")
procedure_division.attribute("exit_node",    exit_node,                 parser_paths="{(.//exit_node)[last()]}")


# This is the generic type for any source unit.
source_unit = NodeType("source_unit", unit, node_name="source_unit")
source_unit.attribute("source_units", unit_list, parser_paths="{sourceUnit/$*} default source_unit")

# A program definition is a type of compilation unit.
program_definition = NodeType("program_definition", unit, node_name="programDefinition")
program_definition.attribute("identification_division", identification_division, parser_paths="{identificationDivision}")
program_definition.attribute("environment_division",    environment_division,    parser_paths="{environmentDivision}")
program_definition.attribute("data_division",           data_division,           parser_paths="{dataDivision}")
program_definition.attribute("procedure_division",      procedure_division,      parser_paths="{procedureDivision}")
program_definition.attribute("source_units",            unit_list,               parser_paths="{sourceUnit/$*} default source_unit")
program_definition.also_trap("HALSTEAD")

# TODO complete function definition (and generated QL)
function_definition = NodeType("function_definition", unit, node_name="functionDefinition")
# TODO needs different type ? there is no {.//program_name}. Check STD.
function_definition.attribute("identification_division", identification_division, parser_paths="{identificationDivision}")
# TODO move these to common source_unit ?
function_definition.attribute("environment_division",    environment_division,    parser_paths="{environmentDivision}")
function_definition.attribute("data_division",           data_division,           parser_paths="{dataDivision}")
function_definition.attribute("procedure_division",      procedure_division,      parser_paths="{procedureDivision}")

division = UnionType("division",
  identification_division,
  environment_division,
  data_division,
  procedure_division
)


preprocessing_directive = UnionType("preprocessing_directive", 
  copy,
  call_arg_copied,  # Special use case of copy.
  replace
)

preprocessing_directive_list = ListType(preprocessing_directive, private=False)


directive = NodeType("directive")
unknown_directive = NodeType("unknown_directive", directive)
# unknown_compiler_directive = NodeType("unknown_compiler_directive", compiler_directive, node_name="compiler_directive")

source_format_directive = NodeType("source_format_directive", directive, node_name="directive/iso/instruction/source", node_ns="cobol-directives")
source_format_directive.attribute("format", STRING, parser_paths="{format}")

m_f_set_statement = NodeType("m_f_set_statement", directive, node_name="directive/mf/set", node_ns="cobol-directives")
m_f_set_statement.attribute("format", STRING, parser_paths="{sourceformat[last()]/parameter}")

directive_list = ListType(directive, private=False)


text = NodeType("text")
text.attribute("preprocessing_directives", preprocessing_directive_list, parser_paths="{//copyStatement|//replaceStatement|/replaced/copyStatement|/replaced/replaceStatement}")
text.attribute("handled_directives",       directive_list,               parser_paths="{/handled/$*} default unknown_directive")
text.also_trap("NUMLINES")

compilation_group = NodeType("compilation_group", text, node_name="compilationGroup")
compilation_group.attribute("source_units", unit_list, parser_paths="{sourceUnit/$*} default source_unit")

copybook = NodeType("copybook", text, node_name="copybook")
copybook.attribute("units",      unit_list, parser_paths="{sourceUnit/$*} default source_unit")
copybook.attribute("statements", stmt_list, parser_paths="{copybookHoldingBehaviour/statement}")
copybook.attribute("sentences",  sentence_list,  parser_paths="{copybookHoldingBehaviour/sentence}")
copybook.attribute("paragraphs", paragraph_list, parser_paths="{copybookHoldingBehaviour/paragraph}")
copybook.attribute("sections",   section_list,   parser_paths="{copybookHoldingBehaviour/section}")
copybook.attribute("entries",    description_entry_list,  parser_paths=[
        "in {copybookHoldingData}",
        "  {constantEntry|dataDescriptionEntry|fileDescriptionEntry/$*|recordDescriptionEntry/$*}",
        "  [NESTED_RECORD_STRUCTURE]"
    ])

named_unit = UnionType("named_unit",
  identification_division, 
  section, 
  paragraph
)
named_unit.attribute("name", STRING)


file_i_o_stmt = UnionType("file_i_o_stmt", 
  open, 
  close, 
  read, 
  merge, 
  sort, 
  delete, 
  return_stmt, 
  start, 
  write, 
  rewrite
)


# SQL -------------------------------------------------------------------------

sql_reference = NodeType("sql_reference")

sql_unknown_reference = NodeType("sql_unknown_reference", sql_reference, node_name="<SQL> unknown_ref")

sql_table_name = NodeType("sql_table_name", sql_reference, node_name="<SQL> tableName")
sql_table_name.attribute("name",    STRING, parser_paths="{identifier}")
sql_table_name.attribute("schema",  STRING, parser_paths="{schemaName}")
sql_table_name.attribute("module",  STRING, parser_paths="{module}")
sql_table_name.attribute("catalog", STRING, parser_paths="{catalogName}")

sql_cursor_name = NodeType("sql_cursor_name", sql_reference, node_name="<SQL> cursorName")
sql_cursor_name.attribute("name",    STRING, parser_paths="{identifier}")
sql_cursor_name.attribute("module",  STRING, parser_paths="{module}")

sql_host_parameter_name = NodeType("sql_host_parameter_name", sql_reference, node_name="<SQL> hostParameterName")
sql_host_parameter_name.attribute("name", STRING, parser_paths="{identifier}")


sql_table_reference = UnionType("sql_table_reference",
  sql_unknown_reference,
  sql_table_name
)

sql_table_reference_list = ListType(sql_table_reference, private=False)


sql_expr = NodeType("sql_expr")

sql_unknown_expr = NodeType("sql_unknown_expr", sql_expr, node_name="<SQL> unknown_expr")

sql_or_expr = NodeType("sql_or_expr", sql_expr)
sql_and_expr = NodeType("sql_and_expr", sql_expr)

sql_not_expr = NodeType("sql_not_expr", sql_expr, node_name="<SQL> negation")

sql_like_predicate = NodeType("sql_like_predicate", sql_expr, node_name="<SQL> likePredicate")
sql_comparison = NodeType("sql_comparison", sql_expr, node_name="<SQL> comparison")

sql_condition_expr = UnionType("sql_condition_expr",
  sql_unknown_expr,
  sql_or_expr,
  sql_and_expr,
  sql_not_expr,
  sql_like_predicate,
  sql_comparison
)

sql_or_expr.attribute("left_operand", sql_condition_expr)
sql_or_expr.attribute("right_operand", sql_condition_expr)
sql_and_expr.attribute("left_operand", sql_condition_expr)
sql_and_expr.attribute("right_operand", sql_condition_expr)

sql_not_expr.attribute("expression", sql_condition_expr, parser_paths="{term}")

sql_like_predicate.attribute("pattern", STRING, parser_paths="{pattern/stringLiteral}")

sql_comparison_op = NodeType("sql_comparison_op")

sql_eq_op   = NodeType("sql_eq_op",   sql_comparison_op, node_name="<SQL> equalsOp")
sql_l_t_op  = NodeType("sql_l_t_op",  sql_comparison_op, node_name="<SQL> lessThanOp")
sql_g_t_op  = NodeType("sql_g_t_op",  sql_comparison_op, node_name="<SQL> greaterThanOp")
sql_n_eq_op = NodeType("sql_n_eq_op", sql_comparison_op, node_name="<SQL> notEqualsOp")
sql_g_e_op  = NodeType("sql_g_e_op",  sql_comparison_op, node_name="<SQL> greaterThanOrEqualsOp")
sql_l_e_op  = NodeType("sql_l_e_op",  sql_comparison_op, node_name="<SQL> lessThanOrEqualsOp")

sql_comparison.attribute("op", sql_comparison_op, parser_paths="{comparisonOp}")


sql_spec = NodeType("sql_spec")

sql_unknown_spec = NodeType("sql_unknown_spec", sql_spec, node_name="<SQL> unknown_spec")

sql_host_parameter_specification = NodeType("sql_host_parameter_specification", sql_spec, node_name="<SQL> hostParameterSpecification")
sql_host_parameter_specification.attribute("parameter", sql_host_parameter_name, parser_paths="{hostParameterName}")
sql_host_parameter_specification.attribute("indicator", sql_host_parameter_name, parser_paths="{indicator/hostParameterName}")


sql_clause = NodeType("sql_clause")

sql_where_clause = NodeType("sql_where_clause", sql_clause, node_name="<SQL> where")
sql_where_clause.attribute("search_condition", sql_condition_expr, parser_paths="{searchCondition}")


sql_aggregate_function = NodeType("sql_aggregate_function")

sql_count_all = NodeType("sql_count_all", sql_aggregate_function, node_name="<SQL> countAll")


sql_into_target = UnionType("sql_into_target",
  sql_unknown_spec,
  sql_host_parameter_specification
)

sql_into_target_list = ListType(sql_into_target, private=False)

sql_into_clause = NodeType("sql_into_clause", sql_clause, node_name="<SQL> into")
sql_into_clause.attribute("targets", sql_into_target_list, parser_paths="{$*} default unknown_spec")

sql_from_clause = NodeType("sql_from_clause", sql_clause, node_name="<SQL> from")
sql_from_clause.attribute("targets", sql_table_reference_list, parser_paths="{tableReferenceList/tableReference/$*} default unknown_ref")


sql_selection = UnionType("sql_selection",
  sql_unknown_expr,
  sql_aggregate_function
)

sql_selection_list = ListType(sql_selection, private=False)


sql_stmt = NodeType("sql_stmt")
sql_other_stmt = NodeType("sql_other_stmt", sql_stmt, node_name="<SQL> sql_other_stmt")


sql_delete_stmt = NodeType("sql_delete_stmt", sql_stmt, node_name="<SQL> deleteStatement")
sql_delete_stmt.attribute("table_name", sql_table_name, parser_paths="{targetTable/tableName}")

sql_declare_cursor_stmt = NodeType("sql_declare_cursor_stmt", sql_stmt, node_name="<SQL> declareCursorStatement")
sql_declare_cursor_stmt.attribute("cursor", sql_cursor_name, parser_paths="{cursorName}")

sql_open_stmt = NodeType("sql_open_stmt", sql_stmt, node_name="<SQL> openStatement")
sql_open_stmt.attribute("cursor", sql_cursor_name, parser_paths="{cursorName}")

sql_close_stmt = NodeType("sql_close_stmt", sql_stmt, node_name="<SQL> closeStatement")
sql_close_stmt.attribute("cursor", sql_cursor_name, parser_paths="{cursorName}")

sql_lock_table_stmt = NodeType("sql_lock_table_stmt", sql_stmt, node_name="<SQL> lockTableStatement")
sql_lock_table_stmt.attribute("table_name", sql_table_name, parser_paths="{tableName}")

sql_select_stmt = NodeType("sql_select_stmt", sql_stmt, node_name="<SQL> selectStatement")
sql_select_stmt.attribute("selection", sql_selection_list, parser_paths="{selectList/$*} default unknown_expr")
sql_select_stmt.attribute("into", sql_into_clause, parser_paths="{into}")
sql_select_stmt.attribute("from", sql_from_clause, parser_paths="{from}")
sql_select_stmt.attribute("where", sql_where_clause, parser_paths="{where}")

sql_update_stmt = NodeType("sql_update_stmt", sql_stmt, node_name="<SQL> updateStatement")
sql_update_stmt.attribute("where", sql_where_clause, parser_paths="{where}")

sql_alter_stmt = NodeType("sql_alter_stmt", sql_stmt, node_name="<SQL> alterStatement")
sql_alter_stmt.attribute("subject", STRING, parser_paths="{subject}")

sql_create_stmt = NodeType("sql_create_stmt", sql_stmt, node_name="<SQL> createStatement")
sql_drop_stmt = NodeType("sql_drop_stmt", sql_stmt, node_name="<SQL> dropStatement")
sql_rename_stmt = NodeType("sql_rename_stmt", sql_stmt, node_name="<SQL> renameStatement")

sql_d_d_l = UnionType("sql_d_d_l",
  sql_alter_stmt,
  sql_create_stmt,
  sql_drop_stmt,
  sql_rename_stmt
)


sql = NodeType("sql", stmt, node_name="execSQLStatement")
sql.attribute("stmt", sql_stmt, parser_paths=[
        "using <SQL>",
        "  {sqlStatement/$*} default sql_other_stmt"
    ])


# Comments --------------------------------------------------------------------

comment = NodeType("comment")
comment.attribute("text", STRING)

basic_comment  = NodeType("basic_comment", comment)


# AST Node --------------------------------------------------------------------

ast_node = UnionType("ast_node", *[val for val in globals().values()
             if isinstance(val, NodeType) and val.is_base_case_type() or
                isinstance(val, ListType) or
                isinstance(val, UnionType)])
