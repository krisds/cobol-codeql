/**
 * @id cbl/display-data-in-computation
 * @name DISPLAY data items slow down computations
 * @description Type conversions impose additional overhead when using DISPLAY data items for computations.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from DisplayItem displayItem, Stmt computation
where displayItemUsedInComputation(displayItem, computation)
select displayItem, "Using DISPLAY items $@ takes additional overhead for data type conversions.",
       computation, "for computations"

predicate displayItemUsedInComputation(DisplayItem displayItem, ComputationalStmt computation) {
  ( displayItem = computation.getASendingDataEntry()
    or displayItem = computation.getAReceivingDataEntry() 
  )
}
