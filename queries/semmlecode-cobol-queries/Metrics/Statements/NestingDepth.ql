/**
 * @id cbl/statement-depth-in-sentences
 * @name Statement nesting depth
 * @description Measures the maximum nesting depth of statements in a sentence.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate max
 * @tags maintainability
 */
 
import cobol

from Sentence s, int n
where n = s.getMetrics().getStatementNestingDepth()
select s, n
order by n desc