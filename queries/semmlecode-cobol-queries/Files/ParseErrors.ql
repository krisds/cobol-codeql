/**
 * @id cbl/parse-error
 * @name Parse error
 * @description The COBOL extractor failed to parse a file.
 * @kind problem
 * @problem.severity warning
 * @precision high
 */

import cobol

from Error e
where e.isParseError()
select e, e.getMessage()