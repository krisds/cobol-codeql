/**
 * @id cbl/internal/paragraph-out-of-order
 * @name Paragraphs after sentences
 * @description Sentences should always come before paragraphs in a section.
 * @kind problem
 * @problem.severity info
 */

import cobol

from Section s, Paragraph p, Sentence t
where p = s.getAParagraph()
  and t = s.getASentence()
  and p.getLocation().(ParagraphLocation).startsBefore(t.getLocation())
select p, "This paragraph appears before a $@ in this section.", t, "sentence"



class SentenceLocation extends Location {
    SentenceLocation() {
        exists (Sentence s | s.getLocation() = this)
    }
}

class ParagraphLocation extends Location {
    ParagraphLocation() {
        exists (Paragraph p | p.getLocation() = this)
    }

    predicate startsBefore(SentenceLocation that) {
      exists (File f, int sl1, int sc1, int sl2, int sc2 |
        locations_default(this, f, sl1, sc1, _, _) and
        locations_default(that, f, sl2, sc2, _, _) and
        (sl1 < sl2 or
         sl1 = sl2 and sc1 < sc2)
      )
    }
}
