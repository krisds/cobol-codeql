/**
 * @id cbl/internal/ast-node-problem
 * @name Cobol AstNode sanity check
 * @description Every AstNode should have exactly one toString and one Location.
 * @kind problem
 * @problem.severity info
 */

import cobol
import Uniqueness

predicate astnode_sanity(AstNode element, string clsname, string problem, string what) {
    exists(AstNode a |
        clsname = a.getAQlClass() and element = a |
        uniqueness_error(count(a.toString()), "toString", problem) and what = "at " + a.getLocation().toString() or
        uniqueness_error(strictcount(a.getLocation()), "getLocation", problem) and what = "at " + a.getLocation().toString() or
        not exists(a.getLocation()) and problem = "no location" and what = a.toString()
    )
}

from AstNode element, string clsname, string problem, string what
where astnode_sanity(element, clsname, problem, what)
select element, clsname + " " + what + " has " + problem
