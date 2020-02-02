/**
 * @id cbl/sql/locked-table
 * @name Locking database table
 * @description Locking an entire table prevents others from accessing it.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags maintainability concurrency
 */

import cobol

from SqlLockTableStmt lock
select lock, "Table '" + lock.getTableName() + "' locked."
