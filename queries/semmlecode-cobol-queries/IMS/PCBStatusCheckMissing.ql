/**
 * @id cbl/ims/untested-status-code
 * @name IMS DL/I call missing status code test
 * @description Failing to test whether an error was raised can lead to unexpected behavior.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags correctness
 */

import cobol

from CBLTDLICall call, PCB pcb,
     DataDescriptionEntry status
where pcb = call.getPCBEntry()
  and status = pcb.getStatusEntry()
  and not exists (Stmt succ, QualifiedDataNameWithSubscripts ref |
      succ = call.getASuccessorStmt() and
      ( ref.hasAncestor(succ.(IfThenElse).getCondition()) or
        ref.hasAncestor(succ.(Evaluate).getASubject()) or
        ref.hasAncestor(succ.(Evaluate).getABranch().(WhenBranch).getAnObject())
      ) and
      status = ref.getReference().getTarget()
  )
select call, "No test of status code $@ after IMS DL/I call.",
       status, status.getName()
