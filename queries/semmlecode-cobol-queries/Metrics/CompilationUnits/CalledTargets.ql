/**
 * @id cbl/number-of-targets-called
 * @name Number of CALL targets
 * @description A high number of CALL targets indicates that a program is tightly coupled and may be costly to maintain.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType procedure-division
 * @metricAggregate sum
 */
 
import cobol

from ProcedureDivision p, int n
where n = count ( string target |
  exists ( Call call |
    call.hasAncestor(p) and
    target = call.getProgramName().toString()) |
  target )
select p, n
order by n desc