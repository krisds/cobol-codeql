/**
 * @id cbl/redundant-target-in-perform
 * @name Redundant argument to PERFORM
 * @description An out-of-line 'PERFORM' statement with two identical
 *              references is likely a typographical error.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags correctness
 */

import cobol

from PerformOutofline p, ProcedureReference r2
where p.getProcedureName1().getName() = r2.getName()
  and p.getProcedureName2() = r2
select p, "The second procedure name $@ is redundant.", r2, r2.getName()