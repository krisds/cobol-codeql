/**
 * @id cbl/halstead/vocabulary
 * @name Halstead vocabulary
 * @description Number of distinct operands and operators used
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u
select u, u.getMetrics().getHalsteadVocabulary()
