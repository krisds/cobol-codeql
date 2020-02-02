/**
 * @id cbl/halstead/effort
 * @name Halstead effort
 * @description Measures the expected effort to implement the program.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u, float n
where n = u.getMetrics().getHalsteadEffort()
select u, n
order by n desc
