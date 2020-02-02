/**
 * @id cbl/badly-indented-statement
 * @name Indentation following decision
 * @description A statement indented with respect to a preceding decision statement
 *              will be executed unconditionally even though it appears to be part of the decision.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

/**
 * Is `s` followed `t` lexically, within the same procedure?  
 */
predicate stmtLexicalSucc(IfThenElse s, Stmt t) {
  s.getEnclosingProcedureDivision() = t.getEnclosingProcedureDivision() and
  exists(StmtList l |
    l.getNextItem(s) = t or
    (l.getLastItem() = s and
     exists(SentenceList sl, Sentence sentence1, Sentence sentence2 |
       sentence1.getStatementsList() = l and
       sl.getNextItem(sentence1) = sentence2 and
       sentence2.getFirstStatement() = t)))
}

from IfThenElse s, Stmt t
where
  stmtLexicalSucc(s, t)
  and s.getLocation().getStartColumn() < t.getLocation().getStartColumn()
  and (not exists(s.getScopeTerminator()) or
       s.getScopeTerminator().getLocation().getStartColumn() > t.getLocation().getStartColumn())
select t, "The indentation of this statement makes it appear as though it is part of the preceding $@.", s, "decision statement" 