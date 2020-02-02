/**
 * @id cbl/halstead/length
 * @name Halstead length
 * @description Measures the total number of operands and operators in the program.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u, int n
where n = u.getMetrics().getHalsteadLength()
select u, n
order by n desc
