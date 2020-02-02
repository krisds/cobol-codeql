/**
 * @id cbl/internal/ambiguous-data-reference
 * @name Unique data references
 * @description Every data reference should have exactly one target.
 * @kind problem
 * @problem.severity info
 */

import cobol

from DataReference r
where count(r.getTarget()) != 1
select r, "Every data reference should have 1 target, but this one has " + count(r.getTarget())