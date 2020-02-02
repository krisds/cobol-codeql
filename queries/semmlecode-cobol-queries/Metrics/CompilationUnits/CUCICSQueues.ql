/**
 * @id cbl/cics/number-of-queue-references
 * @name Number of queues referenced in CICS blocks
 * @description Measures the number of queues referenced in CICS commands in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate sum
 */
 
import cobol

from CompilationUnit u, int n
where n = count( Cics c |
  exists(c.getQueue()) and
  c.getParent+() = u
)
select u, n
order by n desc