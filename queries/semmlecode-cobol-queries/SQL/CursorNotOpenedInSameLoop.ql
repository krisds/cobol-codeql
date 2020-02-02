/**
 * @id cbl/sql/close-of-cursor-not-opened-in-same-loop
 * @name Cursor not opened in loop
 * @description Attempting to close a cursor that is not open may cause possible errors.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from Perform p, Sql execSqlClose, SqlCloseStmt sqlClose, SqlCursorName closeCursor 
where
  // Look for an SQL CLOSE in a loop. 
  sqlClose = execSqlClose.getStmt() and
  execSqlClose = p.getAStmtInScope() and
  exists(p.getLoopForm()) and
  closeCursor = sqlClose.getCursor() and
  
  // Check that there is no matching SQL OPEN in the same loop.
  not exists ( Sql execSqlOpen, SqlOpenStmt sqlOpen, SqlCursorName openCursor |
    sqlOpen = execSqlOpen.getStmt() and
    execSqlOpen = p.getAStmtInScope() and
    openCursor = sqlOpen.getCursor() and
    closeCursor.matches(openCursor)
  )
  
select sqlClose, "Closing cursor '" + closeCursor.toString() + "' in a $@ in which it was not opened.",
       p, "loop"
