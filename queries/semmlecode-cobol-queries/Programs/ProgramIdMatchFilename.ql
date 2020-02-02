/**
 * @id cbl/mismatched-program-id
 * @name PROGRAM-ID should match file name
 * @description To avoid confusion, the name specified in the PROGRAM-ID paragraph
 *              of an outermost program definition should be the same as the file name containing it.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags readability
 */

import cobol

from ProgramDefinition p
where not exists(ProgramDefinition p2 | p.hasAncestor(p2)) and
      not (p.getIdentificationDivision().getName().toUpperCase() =
           p.getLocation().getFile().getShortName().toUpperCase())
select p, "The PROGRAM-ID '" + p.getIdentificationDivision().getName() +
          "' does not match file '" + p.getLocation().getFile().getName() +
          "' containing this top-level program definition."