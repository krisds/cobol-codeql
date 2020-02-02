/**
 * @id cbl/lines-of-water-in-files
 * @name Number of lines of water in files
 * @description Measures the number of lines of water in each file, 
 *              which the Koopa parser has skipped over.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType file
 * @metricAggregate avg sum max
 */
import cobol

from File f, int n
where n = f.getNumberOfLinesOfWater()
select f, n
order by n desc
