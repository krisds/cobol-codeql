/**
 * @id cbl/procedure-division-loc
 * @name Number of lines of code in procedure divisions
 * @description Measures the number of lines of code in each procedure division, ignoring lines that
 *              contain only comments or whitespace.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType procedure-division
 * @metricAggregate avg sum max
 */
import cobol

from ProcedureDivision f, int n
where n = f.getMetrics().getNumberOfLinesOfCode()
select f, n
order by n desc
