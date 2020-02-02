/**
 * @id cbl/cics/number-of-file-references
 * @name Number of references to files in CICS commands
 * @description Measures the number of references to files in CICS commands in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate sum
 */
 
import cobol

from CompilationUnit u, int n
where n = count( Cics c |
  exists(c.getFile()) and
  c.getParent+() = u
)
select u, n
order by n desc