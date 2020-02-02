/**
 * @id cbl/use-of-goto
 * @name Use of GO TO
 * @description The GO TO statement can make control flow hard to understand.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags readability
 */

import cobol

from GoTo g, AstNode owner, string description
where not goesToExitParagraphInSameSection(g)
  and owner = g.getEnclosingSentence().getEnclosingUnit()
  and description = getDescription(owner)
select g, "GO TO statements may make $@ hard to understand.", owner, description

predicate goesToExitParagraphInSameSection(GoTo g) {
  exists ( ExitParagraph exit, Section s |
    g.getTargetsSize() = 1 and
    exit = g.getFirstTarget().getTarget() and
    g.hasAncestor(s) and
    s.getLastParagraph() = exit
  )
}

string getDescription(Unit enclosing) {
  result = "paragraph " +  enclosing.(Paragraph).getName() or
  result = "section " +  enclosing.(Section).getName() or
  enclosing instanceof ProcedureDivision and result = "the program"
}