/**
 * @name Unique procedure references
 * @description Every procedure reference should have exactly one target.
 * @kind problem
 * @problem.severity info
 * @id cbl/internal/ambiguous-procedure-reference
 */

import cobol

from ProcedureReference pr
where count(pr.getTarget()) != 1
select pr, "Every procedure reference should have 1 target, but this one has " + count(pr.getTarget())