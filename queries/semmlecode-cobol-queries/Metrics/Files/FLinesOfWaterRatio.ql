/**
 * @id cbl/water-ratio-in-files
 * @name Ratio of lines of water to number of lines
 * @description The percentage of lines in a file that the parser has skipped over.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType file
 * @metricAggregate avg sum max
 */
import cobol

from File f, int n
where n = f.getNumberOfLines() and n > 0
select f, 100.0 * ((float)f.getNumberOfLinesOfWater() / (float)n) as ratio
order by ratio desc
