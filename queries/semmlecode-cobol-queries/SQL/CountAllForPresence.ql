/**
 * @id cbl/sql/count-all-for-existence-test
 * @name Testing presence of data by counting all matches
 * @description Counting all matches requires a full table scan.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags performance
 */

import cobol

from SqlSelectStmt stmt, DataDescriptionEntry entry, ComparisonExpr comp
where
  // We have a SELECT COUNT(*) INTO :ENTRY-NAME ...
  exists (int n, SqlHostParameterSpecification spec |
    stmt.getSelection(n) instanceof SqlCountAll
    and spec = stmt.getInto().getTarget(n)
    and entry = spec.getParameter().getTarget()
  )
  // Followed by an IF comparing the ENTRY_NAME ...
  and exists ( IfThenElse ite |
    ite = stmt.getEnclosingExecStmt().getASuccessorStmt() and
    comp = ite.getCondition() and (
      subjectGT0(entry, comp)      // ENTRY_NAME > 0
      or subjectGE1(entry, comp)   // ENTRY_NAME >= 1
      or objectGT0(entry, comp)    // 0 < ENTRY_NAME
      or objectGE1(entry, comp)    // 1 <= ENTRY_NAME
    )
  )
select stmt, "$@ has a count of all matching records, but $@.",
       entry, entry.getName(),
       comp, "it is only tested for presence of at least one"

       
predicate subjectGT0(DataDescriptionEntry entry, ComparisonExpr comp) {
  exists ( QualifiedDataNameWithSubscripts operand, RelationObjectExpr rel |
    operand = comp.getSubject() and
    entry = operand.getReference().getTarget() and
    rel = comp.getObject() and
    rel.getOperator() instanceof GTOp and
    rel.getOperand().(NumericLiteral).getValue().toInt() = 0
  )
}

predicate subjectGE1(DataDescriptionEntry entry, ComparisonExpr comp) {
  exists ( QualifiedDataNameWithSubscripts operand, RelationObjectExpr rel |
    operand = comp.getSubject() and
    entry = operand.getReference().getTarget() and
    rel = comp.getObject() and
    rel.getOperator() instanceof GEOp and
    rel.getOperand().(NumericLiteral).getValue().toInt() = 1
  )
}

predicate objectGT0(DataDescriptionEntry entry, ComparisonExpr comp) {
  exists ( QualifiedDataNameWithSubscripts operand, RelationObjectExpr rel |
    comp.getSubject().(NumericLiteral).getValue().toInt() = 0 and
    entry = operand.getReference().getTarget() and
    rel = comp.getObject() and
    rel.getOperator() instanceof LTOp and
    operand = rel.getOperand()
  )
}

predicate objectGE1(DataDescriptionEntry entry, ComparisonExpr comp) {
  exists ( QualifiedDataNameWithSubscripts operand, RelationObjectExpr rel |
    comp.getSubject().(NumericLiteral).getValue().toInt() = 1 and
    entry = operand.getReference().getTarget() and
    rel = comp.getObject() and
    rel.getOperator() instanceof LEOp and
    operand = rel.getOperand()
  )
}
