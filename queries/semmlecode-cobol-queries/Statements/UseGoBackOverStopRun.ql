/**
 * @id cbl/use-of-stop-run
 * @name Use GOBACK rather than STOP RUN
 * @description STOP RUN will cause the run unit to be immediately terminated even if
 *              a subprogram is currently being executed. In this situation GOBACK instead returns to the
 *              calling program.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from Stop s
where not exists(s.getLiteral())
select s, "Use GOBACK rather than STOP RUN."
