/**
 * @id cbl/cics/number-of-commands
 * @name Number of CICS commands
 * @description Measures the number of CICS commands in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate sum
 */
 
import cobol

from CompilationUnit u, int n
where n = count(Cics s | s.getParent+() = u)
select u, n
order by n desc