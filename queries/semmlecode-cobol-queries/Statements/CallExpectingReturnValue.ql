/**
 * @id cbl/unexpected-return-value-in-call
 * @name CALL expects return value
 * @description The program being called does not provide a return value.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, ProgramDefinition prog, CallGiving giving
where prog = call.getTarget()
  and exists ( ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    not exists (header.getReturning()) and
    giving = call.getGiving()
  )
select call, "Call expects a $@, but the $@ does not provide one.",
       giving, "return value",
       prog, "callee"
