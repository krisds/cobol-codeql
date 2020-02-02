/**
 * @id cbl/comment-ratio-in-files
 * @name Comment ratio in files
 * @description The percentage of lines in a file that contain comments.
 * @kind treemap
 * @treemap.warnOn lowValues
 * @metricType file
 * @metricAggregate avg max
 * @tags maintainability
 *       documentation
 */
import cobol

from File f, int n
where n = f.getNumberOfLines() and n > 0
select f, 100.0 * ((float)f.getNumberOfLinesOfComments() / (float)n) as ratio
order by ratio desc
