/**
 * @id cbl/halstead/volume
 * @name Halstead volume
 * @description The information contents of the program
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate avg
 */
import cobol

from CompilationUnit u
select u, u.getMetrics().getHalsteadVolume()
