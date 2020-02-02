import semmle.cobol.Files
import semmle.cobol.AST


private
string relativePath(File file) {
  result = file.getRelativePath().replaceAll("\\", "/")
}

// This relation is cached to prevent inlining for performance reasons
private cached
predicate tokenLocation(File file, int sl, int sc, int ec, int el, DuplicateOrSimilarity copy, int index) {
  file = copy.sourceFile() and
  tokens(copy, index, sl, sc, ec, el)
}

class DuplicateOrSimilarity extends @duplication_or_similarity
{
  private
  int lastToken() {
    result = max(int i | tokens(this, i, _, _, _, _) | i)
  }

  int tokenStartingAt(Location loc) {
    tokenLocation(loc.getFile(), loc.getStartLine(), loc.getStartColumn(),
      _, _, this, result)
  }

  int tokenEndingAt(Location loc) {
    tokenLocation(loc.getFile(), _, _,
      loc.getEndLine(), loc.getEndColumn(), this, result)
  }

  int sourceStartLine() {
    tokens(this, 0, result, _, _, _)
  }

  int sourceStartColumn() {
    tokens(this, 0, _, result, _, _)
  }

  int sourceEndLine() {
    tokens(this, this.lastToken(), _, _, result, _)
  }

  int sourceEndColumn() {
    tokens(this, this.lastToken(), _, _, _, result)
  }

  int sourceLines() {
    result = this.sourceEndLine() + 1 - this.sourceStartLine()
  }

  int getEquivalenceClass() {
    duplicateCode(this, _, result) or similarCode(this, _, result)
  }

  File sourceFile() {
    exists(string name |
      duplicateCode(this, name, _) or similarCode(this, name, _) |
      name.replaceAll("\\", "/") = relativePath(result)
    )
  }

  predicate hasLocationInfo(string filepath, int startline, int startcolumn, int endline, int endcolumn) {
      sourceFile().getAbsolutePath() = filepath and
      startline = sourceStartLine() and
      startcolumn = sourceStartColumn() and
      endline = sourceEndLine() and
      endcolumn = sourceEndColumn()
  }

  string toString() { none() }

  DuplicateOrSimilarity extendingBlock() {
    exists(File file, int sl, int sc, int ec, int el |
      tokenLocation(file, sl, sc, ec, el, this, _) and
      tokenLocation(file, sl, sc, ec, el, result, 0)) and
    this != result
  }
}

predicate similar_extension(SimilarBlock start1, SimilarBlock start2, SimilarBlock ext1, SimilarBlock ext2, int start, int ext) {
    start1.getEquivalenceClass() = start and
    start2.getEquivalenceClass() = start and
    ext1.getEquivalenceClass() = ext and
    ext2.getEquivalenceClass() = ext and
    start1 != start2 and
    (ext1 = start1 and ext2 = start2 or
     similar_extension(start1.extendingBlock(), start2.extendingBlock(), ext1, ext2, _, ext)
    )
}

predicate duplicate_extension(DuplicateBlock start1, DuplicateBlock start2, DuplicateBlock ext1, DuplicateBlock ext2, int start, int ext) {
    start1.getEquivalenceClass() = start and
    start2.getEquivalenceClass() = start and
    ext1.getEquivalenceClass() = ext and
    ext2.getEquivalenceClass() = ext and
    start1 != start2 and
    (ext1 = start1 and ext2 = start2 or
     duplicate_extension(start1.extendingBlock(), start2.extendingBlock(), ext1, ext2, _, ext)
    )
}


class DuplicateBlock extends DuplicateOrSimilarity, @duplication
{
  override string toString() {
    result = "Duplicate code: " + sourceLines() + " duplicated lines."
  }
}

class SimilarBlock extends DuplicateOrSimilarity, @similarity
{
  override string toString() {
    result = "Similar code: " + sourceLines() + " almost duplicated lines."
  }
}

private
predicate stmtInBlock(Stmt s, AstNode p) {
  exists(s.getEnclosingProcedureDivision()) and
  s.hasAncestor(p) and
  not exists(Stmt t | t.hasAncestor(s))
}

predicate duplicateStatement(AstNode n1, AstNode n2, Stmt stmt1, Stmt stmt2) {
     exists(int equivstart, int equivend, int first, int last |
        stmtInBlock(stmt1, n1) and
        stmtInBlock(stmt2, n2) and
        duplicateCoversStatement(equivstart, equivend, first, last, stmt1) and
        duplicateCoversStatement(equivstart, equivend, first, last, stmt2) and
        stmt1 != stmt2 and
        n1 != n2
    )
}

private
predicate duplicateCoversStatement(int equivstart, int equivend, int first, int last, Stmt stmt) {
  exists(DuplicateBlock b1, DuplicateBlock b2, Location loc |
    stmt.getLocation() = loc and
    first = b1.tokenStartingAt(loc) and
    last = b2.tokenEndingAt(loc) and
    b1.getEquivalenceClass() = equivstart and
    b2.getEquivalenceClass() = equivend and
    duplicate_extension(b1, _, b2, _, equivstart, equivend)
  )
}

private
predicate duplicateStatements(AstNode n1, AstNode n2, int duplicate, int total) {
  duplicate = strictcount(Stmt stmt | duplicateStatement(n1, n2, stmt, _)) and
  total = strictcount(Stmt stmt | stmtInBlock(stmt, n1))
}

/* Note that the exclusions below (that prevent a duplicate paragraph
 * from being flagged if it is included in a duplicate section or
 * text, and likewise for a duplicate section included in a text) are
 * *not* symmetric. Therefore it is possible for a section s1 to a
 * duplicate of another larger section s2, such that s2 is not a
 * duplicate of s1, and for a paragraph p2 (in s2) to be a duplicate
 * of a paragraph p1 (in s1). This decision has been taken for
 * consistency with the lack of symmetry-breaking within classes of
 * blocks i.e. it is possible to have two violations for p1 being a
 * duplicate of p2 and vice versa.
 */

/**
 * Find pairs of blocks that have more than 70% duplicate statements.
 */
predicate duplicateBlocks(AstNode n1, AstNode n2, float percent) {
    exists(int total, int duplicate |
      duplicateStatements(n1, n2, duplicate, total) |
      percent = 100.0*duplicate/total and
      percent > 70.0
   )
}

predicate duplicateParagraphs(Paragraph x, Paragraph y, float percent) {
  duplicateBlocks(x, y, percent) and
  percent > 90.0 and
  x.getMetrics().getNumberOfStmts() > 10 and
  not exists(Section s1, Section s2 |
        x.hasAncestor(s1) and y.hasAncestor(s2) |
        duplicateSections(s1, s2, _)) and
  not exists(Text t1, Text t2 |
        x.hasAncestor(t1) and y.hasAncestor(t2) |
        duplicateTexts(t1, t2, _))
}

predicate duplicateSections(Section x, Section y, float percent) {
  duplicateBlocks(x, y, percent) and
  percent > 90.0 and
  x.getMetrics().getNumberOfStmts() > 20 and
  not exists(Text t1, Text t2 |
        t1 = x.getEnclosingText() and
        t2 = y.getEnclosingText() |
        duplicateTexts(t1, t2, _))
}

predicate duplicateTexts(Text x, Text y, float percent) {
  duplicateBlocks(x, y, percent)
}

private
predicate similarStatement(AstNode n1, AstNode n2, Stmt stmt1, Stmt stmt2) {
     exists(int start, int end, int first, int last |
        stmtInBlock(stmt1, n1) and
        stmtInBlock(stmt2, n2) and
        similarCoversStatement(start, end, first, last, stmt1) and
        similarCoversStatement(start, end, first, last, stmt2) and
        stmt1 != stmt2 and
        n1 != n2
    )
}

private
predicate similarCoversStatement(int equivstart, int equivend, int first, int last, Stmt stmt) {
  exists(SimilarBlock b1, SimilarBlock b2, Location loc |
    stmt.getLocation() = loc and
    first = b1.tokenStartingAt(loc) and
    last = b2.tokenEndingAt(loc) and
    b1.getEquivalenceClass() = equivstart and
    b2.getEquivalenceClass() = equivend and
    similar_extension(b1, _, b2, _, equivstart, equivend)
  )
}

private
predicate similarStatements(AstNode n1, AstNode n2, int similar, int total) {
  similar = strictcount(Stmt stmt | similarStatement(n1, n2, stmt, _)) and
  total = strictcount(Stmt stmt | stmtInBlock(stmt, n1))
}

/**
 * Find pairs of blocks that have more than 80% similar statements.
 */
predicate similarBlocks(AstNode n1, AstNode n2, float percent) {
    exists(int total, int similar |
      similarStatements(n1, n2, similar, total) |
      percent = 100.0*similar/total and
      percent > 80.0
    )
}



predicate similarParagraphs(Paragraph x, Paragraph y, float percent) {
  similarBlocks(x, y, percent) and
  percent > 90.0 and
  not duplicateParagraphs(x, y, _) and
  x.getMetrics().getNumberOfStmts() > 10 and
  not exists(Section s1, Section s2 |
        x.hasAncestor(s1) and y.hasAncestor(s2) |
        similarSections(s1, s2, _) or
        duplicateSections(s1, s2, _)) and
  not exists(Text t1, Text t2 |
        t1 = x.getEnclosingText() and
        t2 = y.getEnclosingText() |
        similarTexts(t1, t2, _) or
        duplicateTexts(t1, t2, _))
}

predicate similarSections(Section x, Section y, float percent) {
  similarBlocks(x, y, percent) and
  percent > 90.0 and
  not duplicateSections(x, y, _) and
  x.getMetrics().getNumberOfStmts() > 20 and
  not exists(Text t1, Text t2 |
        t1 = x.getEnclosingText() and
        t2 = y.getEnclosingText() |
        similarTexts(t1, t2, _) or
        duplicateTexts(t1, t2, _))
}

predicate similarTexts(Text x, Text y, float percent) {
  similarBlocks(x, y, percent) and
  percent > 80.0 and
  not duplicateTexts(x, y, _)
}