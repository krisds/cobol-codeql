/**
 * @id cbl/max-nesting-depth
 * @name The maximum nesting depth of statements
 * @description Measures the maximum nesting depth of statements in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate max
 */
 
import cobol

from CompilationUnit u, int n
where n = u.getMetrics().getStatementNestingDepth()
select u, n
order by n desc