/**
 * @id cbl/duplicate-paragraph-name
 * @name Duplicate paragraph name
 * @description Paragraphs with duplicate names in the same section lead to
 *              undefined behavior when the name is referenced by a control
 *              flow construct.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Paragraph p1, Paragraph p2, string container
where p1 != p2 and 
  ((exists(Section s | s.getAParagraph() = p1 and s.getAParagraph() = p2) and container = "section") or
   (exists(ProcedureDivision pd | pd.getAParagraph() = p1 and pd.getAParagraph() = p2) and container = "procedure division" ))
  and p1.getName() = p2.getName()
  and startsBefore(p1, p2)
select p1, "This paragraph has the same name as $@ from the same " + container + ".", p2, p2.getName()

private predicate startsBefore(Paragraph a, Paragraph b) {
    exists (Location al, Location bl |
        al = a.getLocation() and
        bl = b.getLocation() and
        al.getStartLine() * 1000 + al.getStartColumn() < bl.getStartLine() * 1000 + bl.getStartColumn() 
    )
}