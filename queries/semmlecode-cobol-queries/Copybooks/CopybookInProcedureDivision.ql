/**
 * @id cbl/copy-in-procedure-division
 * @name Copybook in procedure division
 * @description Including logic from copybooks can lead to inconsistent systems.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       reusability
 */

import cobol

from Copy copy
where exists ( ProcedureDivision pd |
    copy.hasAncestor(pd)
    or exists (ProcedureDivisionLocation pdloc, CopyLocation cloc |
        pdloc = pd.getLocation() and
        cloc = copy.getLocation() and
        pdloc.containsCopy(cloc)
    )
  )
select copy, "Including logic from copybooks is strongly discouraged."



class CopyLocation extends Location {
    CopyLocation() {
        exists (Copy cb | cb.getLocation() = this)
    }
}

class ProcedureDivisionLocation extends Location {
    ProcedureDivisionLocation() {
        exists (ProcedureDivision pd | pd.getLocation() = this)
    }
    
    /** Does this location start before the given location? */
    predicate startsBeforeCopy(CopyLocation that) {
      exists (File f, int sl1, int sc1, int sl2, int sc2 |
        locations_default(this, f, sl1, sc1, _, _) and
        locations_default(that, f, sl2, sc2, _, _) and
        (sl1 < sl2 or
         sl1 = sl2 and sc1 < sc2)
      )
    }
    
    /** Does this location end after the given location? */
    predicate endsAfterCopy(CopyLocation that) {
      exists (File f, int el1, int ec1, int el2, int ec2 |
        locations_default(this, f, _, _, el1, ec1) and
        locations_default(that, f, _, _, el2, ec2) and
        (el1 > el2 or
         el1 = el2 and ec1 > ec2)
      )
    }
    
    predicate containsCopy(CopyLocation that) {
      this.startsBeforeCopy(that) and this.endsAfterCopy(that)
    }
}

