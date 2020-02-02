/**
 * @id cbl/wrong-number-of-arguments-in-call
 * @name Wrong number of arguments in a CALL
 * @description CALL should pass the required number of arguments.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, int numberOfArgs,
     ProgramDefinition prog, int minNumberOfParams, int maxNumberOfParams,
     string rangeMsg
where prog = call.getTarget()
  and numberOfArgs = countArgs(call)
  and exists (ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    maxNumberOfParams = countParams(header) and
    minNumberOfParams = maxNumberOfParams - trailingOptionalParameters(header) and
    (numberOfArgs < minNumberOfParams or numberOfArgs > maxNumberOfParams) and
    if (minNumberOfParams = maxNumberOfParams) then
      rangeMsg = maxNumberOfParams.toString()
    else
      rangeMsg = minNumberOfParams + " to " + maxNumberOfParams
  )
select call, "$@ expects " + rangeMsg + " argument(s). " +
             "This CALL specifies " + numberOfArgs + " instead.",
       prog, prog.getIdentificationDivision().getName()

int countArgs(Call call) {
  if (not exists(call.getUsingList())) then
    result = 0
  else
    result = call.getUsingSize()
}

int countParams(ProcedureDivisionHeader header) {
  if (not exists(header.getUsingList())) then
    result = 0
  else
    result = header.getUsingSize()
}

int trailingOptionalParameters(ProcedureDivisionHeader h) {
  result = count ( OptionalParameter p | p = trailingOptional(h) | p )
}

OptionalParameter trailingOptional(ProcedureDivisionHeader h) {
  result = h.getLastUsing()
  or result.getParent().(ProcedureDivisionParameterList).getNextItem(result) = trailingOptional(h)
}

class OptionalParameter extends ProcedureDivisionParameter {
  OptionalParameter() { exists (getOptional()) }
}
