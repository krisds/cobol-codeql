/**
 * @id cbl/cics/missing-resp
 * @name CICS call does not capture condition states
 * @description Without the RESP option you cannot test if a condition was raised
 *              during execution of the CICS call.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags correctness
 */

import cobol

from Cics cics
where not exists (cics.getResp())
  // RETURN commands just exit the running program, so no need to capture status conditions.
  and not (cics.getCommand().toUpperCase() = "RETURN")
select cics, "This '" + cics.getCommand().toUpperCase() + "' call does not capture condition status."