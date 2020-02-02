import AST
import Metrics
import SourceLines
import Stmts

class CFlowNode extends AstNode {
  CFlowNode() {
    this instanceof ProcedureDivision or
    this instanceof Section or
    this instanceof Paragraph or
    this instanceof Sentence or
    this instanceof Stmt or
    this instanceof ExitNode or
    this instanceof OnExceptionBranch or
    this instanceof NotOnExceptionBranch or
    this instanceof AtEndBranch or
    this instanceof NotAtEndBranch or
    this instanceof AtEndOfPageBranch or
    this instanceof NotAtEndOfPageBranch or
    this instanceof OnOverflowBranch or
    this instanceof NotOnOverflowBranch or
    this instanceof OnSizeErrorBranch or
    this instanceof NotOnSizeErrorBranch or
    this instanceof InvalidKeyBranch or
    this instanceof NotInvalidKeyBranch
  }
}

/** A block of code larger than a statement */
class Unit extends Unit_ {
  /** Get the metrics for this unit */
  UnitMetrics getMetrics() {
    result = this
  }
  
  private Unit getNonProgramEnclosingUnit() {
    result = getEnclosingUnit() and not result instanceof ProgramDefinition
  }

  /** Get the program definition containing this unit.
   * If the unit is a program definition, return the unit itself. */
  ProgramDefinition getProgramDefinition() {
    if (this instanceof ProgramDefinition) then
      result = this
    else
      result = this.getNonProgramEnclosingUnit*().getEnclosingUnit()
  }

  override string toString() { result = Unit_.super.toString() }
}

/** A unit that is guaranteed to have a name. */
abstract class NamedUnit extends NamedUnit_, Unit {
  /** the name of this unit */
  abstract override string getName();
  override AstNode getParent() { result = Unit.super.getParent() }
  override string toString() { result = this.getName() }
}

class IdentificationDivision extends IdentificationDivision_ {
  /** The program name specified in this identification division. */
  override string getName() {
    exists(string rawName | rawName =  IdentificationDivision_.super.getName() |
      if rawName.matches("'%'") or
         rawName.matches("\"%\"")
      then
        result = rawName.substring(1, rawName.length() - 1)
      else
        result = rawName
    )
  }
}

abstract class Procedure extends AstNode {
  abstract Sentence getFirstSentence();
  abstract Sentence getLastSentence();
  abstract Procedure getNextLexicalProcedure();
  abstract ExitNode getExitNode();
  abstract string getName();
  string getUpperName() { result = getName().toUpperCase() }

  // Relates statements to the procedures they belong to.
  abstract Stmt getAStatement();

  Stmt getFirstStatementInRange() {
    result = getFirstSentence().getFirstStatement()
  }
  
  Stmt getLastStatementInRange() {
    result = getLastSentence().getLastStatement()
  }
  
  /** Which procedure is in the direct flow from this one ? 
   * We're not relying on control flow here. Just looking at targets of GO TO
   * statements, and at the last statement in the procedure. */
  Procedure getASucceedingProcedure() {
    // The next lexical procedure is a valid successor, but only if we don't
    // jump away right before reaching it.
    ( exists( Stmt s | 
        s = getLastStatementInRange() and 
        not s instanceof GoTo ) and
      result = getNextLexicalProcedure() )
    or
    // Anything mentioned in a GO TO is a valid successor.
    ( exists( GoTo g |
        g.hasAncestor(this) and
        result = g.getATarget().getTarget()
      ) )
   
    // I'm disregarding PERFORM statements. I'm only interested in procedures
    // at the same "call level" (for lack of a better word) as this one.
  }
}

class DeclarativeSection extends Section {
  DeclarativeSection() {
    exists(ProcedureDivision pd |
      pd.getDeclaratives().getASection() = this)
  }

  override string toString() { result = "Declarative section " + this.getName() }
}

class Section extends Section_, Procedure {
  override AstNode getParent() { result = Section_.super.getParent() }
  override string toString() { result = "Section " + this.getName() }

  override string getName() { result = Section_.super.getName() }
  override ExitNode getExitNode() {
    if exists(Section_.super.getExitNode()) then
      result = Section_.super.getExitNode()
    else
      result = this.getParent().getParent().(ProcedureDivision).getExitNode()
  }
  
  override Procedure getNextLexicalProcedure() {
    result = getParent().(SectionList).getNextItem(this)
  }

  override Sentence getFirstSentence() {
    (not exists(this.getASentence()) and
     result = this.getFirstParagraph().getFirstSentence()) or
    result = Section_.super.getFirstSentence()
  }

  override Sentence getLastSentence() {
    (not exists(this.getAParagraph()) and
     result = Section_.super.getLastSentence()) or
    result = Section_.super.getLastParagraph().getLastSentence()
  }

  predicate isEmpty() {
    ( not exists(getAParagraph()) or
      getParagraphsSize() = 0 )
    and
    forall ( Stmt s |
      s = getASentence().getAStatement() | 
      s.isCompilerGenerated()
    )
  }
  
  override Stmt getAStatement() {
    result.hasAncestor(getASentence()) or
    result = getAParagraph().getAStatement()
  }
}

class Paragraph extends Paragraph_, Procedure {
  override AstNode getParent() { result = Paragraph_.super.getParent() }
  override string toString() { result = "Paragraph " + this.getName() }

  override string getName() { result = Paragraph_.super.getName() }
  override ExitNode getExitNode() {
    if exists(Paragraph_.super.getExitNode()) then
      result = Paragraph_.super.getExitNode()
    else
      result = this.getParent().getParent().(Section).getExitNode()
  }
  
  override Procedure getNextLexicalProcedure() {
    result = getParent().(ParagraphList).getNextItem(this) or
    
    (not exists(getParent().(ParagraphList).getNextItem(this)) and 
     result = getParent().getParent().(Section).getNextLexicalProcedure())
  }

  
  override Sentence getFirstSentence() { result = Paragraph_.super.getFirstSentence() }
  override Sentence getLastSentence() { result = Paragraph_.super.getLastSentence() }
  
  override Stmt getAStatement() {
    result.hasAncestor(getASentence())
  }
}

class ExitParagraph extends Paragraph {
  ExitParagraph() {
    getSentencesSize() = 1 and
    getFirstSentence().getStatementsSize() = 1 and
    getFirstSentence().getFirstStatement() instanceof Exit
  }
}

class Sentence extends Sentence_ {
  /** Was this sentence inserted by the "compiler" ? */
  predicate isCompilerGenerated() {
    compgenerated(this)
  }
}

class ProgramDefinition extends ProgramDefinition_ {
  /** true for a node that is contained within this program
    * directly (not in a nested program) */
  predicate contains(AstNode n) {
    n.getParent() = this
    or (not(n.getParent() instanceof ProgramDefinition) and this.contains(n.getParent()))
  }

  /** Get the metrics for this unit */
  override ProgramDefinitionMetrics getMetrics() {
    result = this
  }
}

/** A compilation unit is an outermost program definition */
class CompilationUnit extends ProgramDefinition {
  CompilationUnit() {
    // Directly contained in a UnitList
    this.getParent().getParent() instanceof Text
  }

  /** Get the metrics for this unit */
  override CompilationUnitMetrics getMetrics() {
    result = this
  }
}
