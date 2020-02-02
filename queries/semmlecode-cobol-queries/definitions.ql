import cobol

/**
 * @id cbl/jump-to-definition
 * @name Jump-to-definition links
 * @description Generates use-definition pairs that provide the data
 *              for jump-to-definition in the code viewer.
 * @kind definitions
 */
 
from Reference e, AstNode def, string kind
where def = e.getTarget()
  and kind = kind(e)
select e, def, kind


string kind(Reference e) {
    if (e instanceof ProcedureReference) then result = "P"
    else if (e instanceof IndexReference) then result = "I"
    else if (e instanceof DataReference) then result = "D"
    else if (e instanceof FileReference) then result = "F"
    else result = "?"
}