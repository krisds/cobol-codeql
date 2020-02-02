/**
 * @id cbl/cics/number-of-map-references
 * @name Number of maps referenced in CICS commands
 * @description Measures the number of maps referenced in CICS commands in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate sum
 */
 
import cobol

from CompilationUnit u, int n
where n = count( Cics c |
  exists(c.getMap()) and
  c.getParent+() = u
)
select u, n
order by n desc