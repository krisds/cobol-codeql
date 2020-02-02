/**
 * @id cbl/sql/unconditional-update
 * @name Unconditional UPDATE
 * @description Updating data without a condition may modify your entire database.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from SqlUpdateStmt stmt
where not exists (stmt.getWhere())
select stmt, "Unconditional update of data."
