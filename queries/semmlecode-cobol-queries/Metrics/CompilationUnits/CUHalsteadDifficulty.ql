/**
 * @id cbl/halstead/difficulty
 * @name Halstead difficulty
 * @description Measures the error proneness of implementing the program.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u, float n
where n = u.getMetrics().getHalsteadDifficulty()
select u, n
order by n desc
