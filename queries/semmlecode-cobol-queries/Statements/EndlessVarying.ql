/**
 * @id cbl/modified-loop-counter
 * @name Modifying VARYING loop counter
 * @description Changing the value of a VARYING loop counter may prevent the loop
 *              from ending.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from PerformVaryingInline varying, DataDescriptionEntry entry, ReceivingDataReference ref
where entry = varying.getEntryOperand()
  and ref.hasAncestor(varying.getAStatement())
  and entry = ref.getTarget()
select ref, "Update of loop variable $@ may prevent $@ from ending.",
       entry, entry.getName(),
       varying, "the loop"
