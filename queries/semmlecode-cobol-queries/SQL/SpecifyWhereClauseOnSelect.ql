/**
 * @id cbl/sql/unconditional-select
 * @name Unconditional SELECT
 * @description Selecting data without a condition will have you read your entire database.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from SqlSelectStmt stmt
where not exists (stmt.getWhere())
  // Make sure we're selecting from an actual table.
  and exists ( SqlTableReference ref |
    ref = stmt.getFrom().getATarget() and
    not ref.(SqlTableName).getName().toUpperCase() = "DUAL"
  )
select stmt, "Unconditional selection of data."
