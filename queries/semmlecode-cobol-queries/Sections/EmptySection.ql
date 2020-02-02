/**
 * @id cbl/empty-section
 * @name Empty section
 * @description Empty sections serve no purpose and can confuse the reader.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags readability
 */

import cobol

from Section s
where s.isEmpty()
select s, "This section is empty."