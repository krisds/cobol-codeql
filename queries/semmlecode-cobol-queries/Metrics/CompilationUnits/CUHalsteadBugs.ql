/**
 * @id cbl/halstead/bug-measure
 * @name Halstead bug measure
 * @description Measures the expected number of delivered bugs.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u, float n
where n = u.getMetrics().getHalsteadDeliveredBugs()
select u, n
order by n desc
