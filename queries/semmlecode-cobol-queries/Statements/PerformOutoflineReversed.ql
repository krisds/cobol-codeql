/**
 * @id cbl/out-of-order-targets-in-perform
 * @name PERFORM with inverted arguments
 * @description An out-of-line 'PERFORM' statement where the second argument appears
 *              before the first in the source code leads to very surprising control flow.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from PerformOutofline s, Procedure p1, Procedure p2
where p1 = s.getProcedureName1().getTarget()
  and p2 = s.getProcedureName2().getTarget()
  and p2.getLocation().getStartLine() < p1.getLocation().getStartLine()
select s, "The first procedure name $@ appears after the second $@.", p1, p1.getName(), p2, p2.getName()