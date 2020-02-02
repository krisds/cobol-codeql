/**
 * @id cbl/cyclomatic-complexity
 * @name Cyclomatic Complexity
 * @description Files with a large number of possible execution paths might be difficult to understand.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg max
 */
import cobol

from CompilationUnit u, int n
where n = u.getMetrics().getCyclomaticComplexity()
select u, n
order by n desc
