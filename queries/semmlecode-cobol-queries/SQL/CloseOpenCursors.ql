/**
 * @id cbl/sql/unclosed-cursor
 * @name Cursor not closed
 * @description Not closing cursors may cause resource leaks and potential errors.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from Sql execSqlOpen, SqlOpenStmt sqlOpen, SqlCursorName openCursor,
     AstNode scope, string scopeDescription
where
  ( noMatchingCloseInLoop(scope, execSqlOpen, sqlOpen, openCursor) and
    scopeDescription = "loop"
  ) or (
    noMatchingCloseInProgram(scope, execSqlOpen, sqlOpen, openCursor) and
    scopeDescription = "procedure division"
  )
  
select sqlOpen, "Cursor '" + openCursor.toString() + "' is not closed in the $@ in which it was opened.",
       scope, scopeDescription


predicate noMatchingCloseInLoop(Perform p, Sql execSqlOpen, SqlOpenStmt sqlOpen, SqlCursorName openCursor) {
  sqlOpen = execSqlOpen.getStmt() and
  openCursor = sqlOpen.getCursor() and

  execSqlOpen = p.getAStmtInScope() and
  exists(p.getLoopForm()) and
  
  not exists ( Sql execSqlClose, SqlCloseStmt sqlClose, SqlCursorName closeCursor |
    sqlClose = execSqlClose.getStmt() and
    execSqlClose = p.getAStmtInScope() and
    closeCursor = sqlClose.getCursor() and
    openCursor.matches(closeCursor)
  )
}

predicate noMatchingCloseInProgram(ProcedureDivision pd, Sql execSqlOpen, SqlOpenStmt sqlOpen, SqlCursorName openCursor) {
  sqlOpen = execSqlOpen.getStmt() and
  openCursor = sqlOpen.getCursor() and

  pd = execSqlOpen.getEnclosingProcedureDivision() and

  not exists ( Perform p |
    execSqlOpen = p.getAStmtInScope() and
    exists(p.getLoopForm())
  ) and
  
  not exists ( Sql execSqlClose, SqlCloseStmt sqlClose, SqlCursorName closeCursor |
    execSqlClose = execSqlOpen.getASuccessorStmt+() and
    sqlClose = execSqlClose.getStmt() and
    closeCursor = sqlClose.getCursor() and
    openCursor.matches(closeCursor)
  )
}