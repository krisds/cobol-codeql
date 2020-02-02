/**
 * @id cbl/division-header-in-copybook
 * @name Copybook defines program structure
 * @description Hiding program structure in copybooks makes programs harder to read and understand.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags readability
 */

import cobol

from Copybook copy
where exists ( Unit u | 
        u.hasAncestor(copy) and
        exists (u.getHeader()) and
        not (u instanceof Section) and
        not (u instanceof Paragraph) and
        not (u instanceof Sentence)
      )
select copy, "Defining program structure in copybooks is strongly discouraged."
