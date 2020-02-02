/**
 * @id cbl/data-division-loc
 * @name Number of lines of code in data divisions
 * @description Measures the number of lines of code in each data division, ignoring lines that
 *              contain only comments or whitespace.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType data-division
 * @metricAggregate avg sum max
 */
import cobol

from DataDivision f, int n
where n = f.getMetrics().getNumberOfLinesOfCode()
select f, n
order by n desc
