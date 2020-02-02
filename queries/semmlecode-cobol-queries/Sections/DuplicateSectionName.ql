/**
 * @id cbl/duplicate-section-name
 * @name Duplicate section name
 * @description Sections with duplicate names in the same program will
 *              either give a compilation error or undefined behavior at runtime.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Section s1, Section s2, ProcedureDivision pd
where s1 != s2
  and pd.getASection() = s1
  and pd.getASection() = s2
  and s1.getName() = s2.getName()
  and startsBefore(s1, s2)
select s1, "This section has the same name as $@ which appears later in the same procedure division.", s2, s2.getName()

private predicate startsBefore(Section a, Section b) {
    exists (Location al, Location bl |
        al = a.getLocation() and
        bl = b.getLocation() and
        al.getStartLine() * 1000 + al.getStartColumn() < bl.getStartLine() * 1000 + bl.getStartColumn() 
    )
}