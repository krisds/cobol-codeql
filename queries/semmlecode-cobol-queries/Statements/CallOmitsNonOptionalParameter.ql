/**
 * @id cbl/omitted-parameter-in-call
 * @name CALL omits a non-optional parameter
 * @description CALL should pass values for required parameters.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, int position, OmittedArgument omitted,
     ProgramDefinition prog, NonOptionalParameter nonOptional
where prog = call.getTarget()
  and exists ( ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    position >= 0 and
    position < call.getUsingSize() and
    position < header.getUsingSize() and
    omitted = call.getUsing(position) and
    nonOptional = header.getUsing(position)
  )
select call, "This call $@ $@, which is not optional.",
       omitted, "omits",
       nonOptional, "parameter " + position

class OmittedArgument extends CallArg {
  OmittedArgument() {
    exists (Omitted o | o.getParent() = this)
  }
}

class NonOptionalParameter extends ProcedureDivisionParameter {
  NonOptionalParameter() {
    not exists (getOptional())
  }
}
