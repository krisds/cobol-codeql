/**
 * @id cbl/sql/cursor-declaration-in-procedure-division
 * @name Declaring SQL cursor in procedure division
 * @description Declaring cursors in the procedure division is confusing
 *              because such declarations are not executable code.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from Sql execSql, SqlDeclareCursorStmt declare
where
  declare = execSql.getStmt() and
  exists(execSql.getEnclosingProcedureDivision())
select declare, "Declaring cursor '" + declare.getCursor().toString() + "' in the procedure division is discouraged."