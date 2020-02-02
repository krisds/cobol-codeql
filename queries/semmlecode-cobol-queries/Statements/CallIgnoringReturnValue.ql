/**
 * @id cbl/ignored-return-value-in-call
 * @name CALL ignores return value
 * @description The program being called provides a return value which is being ignored.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, ProgramDefinition prog, Identifier returning
where prog = call.getTarget()
  and exists ( ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    returning = header.getReturning() and
    not exists (call.getGiving())
  )
select call, "Call ignores $@.",
       returning, "return value"
