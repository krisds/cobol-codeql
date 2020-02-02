/**
 * @id cbl/sei-maintainability-index
 * @name SEI Maintainability Index
 * @description Gives a measure of the maintainability of this file. Low values indicate that the program may be hard to modify correctly, especially for developers new to the project.
 * @kind treemap
 * @treemap.warnOn lowValues
 * @metricType compilation-unit
 * @metricAggregate avg min
 */
import cobol

from CompilationUnit u, float n
where n = u.getMetrics().getSEIMaintainabilityIndex()
select u, n
order by n asc