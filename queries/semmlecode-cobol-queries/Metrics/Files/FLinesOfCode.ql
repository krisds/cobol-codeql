/**
 * @id cbl/lines-of-code-in-files
 * @name Lines of code in files
 * @description Measures the number of lines of code in each file, ignoring lines that
 *              contain only comments or whitespace.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType file
 * @metricAggregate avg sum max
 * @tags maintainability
 *       complexity
 */
import cobol

from File f, int n
where n = f.getNumberOfLinesOfCode()
select f, n
order by n desc
