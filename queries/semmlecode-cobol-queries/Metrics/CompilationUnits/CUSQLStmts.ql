/**
 * @id cbl/sql/number-of-statements
 * @name Number of SQL statements
 * @description Measures the number of SQL statements in a compilation unit.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate sum
 */
 
import cobol

from CompilationUnit u, int n
where n = count(Sql s | s.getParent+() = u)
select u, n
order by n desc