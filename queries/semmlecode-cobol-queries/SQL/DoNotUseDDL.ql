/**
 * @id cbl/sql/database-structure-modification
 * @name Database structure modified
 * @description Letting an application modify the structure of the database
 *              may break the overall system.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from SqlDDL ddl
where not (ddl.(SqlAlterStmt).getSubject().toUpperCase() = "SESSION")
select ddl, "Database structure gets modified at run-time."
