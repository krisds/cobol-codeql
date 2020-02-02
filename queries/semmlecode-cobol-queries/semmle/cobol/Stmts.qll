import AST

/** A statement. */
class Stmt extends Stmt_ {

  /** Get the paragraph containing this statement. */
  Paragraph getEnclosingParagraph() {
    result.hasDescendant(this)
  }

  /** Get the section containing this statement. */
  Section getEnclosingSection() {
    result.hasDescendant(this)
  }

  /** Get the procedure division containing this statement. */
  ProcedureDivision getEnclosingProcedureDivision() {
    result.hasDescendant(this)
  }

  /** Was this statement inserted by the "compiler" ? */
  predicate isCompilerGenerated() {
    compgenerated(this)
  }
  
  Stmt getASuccessorStmt() {
    stmt_succ(this, result)
  }
  
  /** Get the sentence containing this statement. */
  Sentence getEnclosingSentence() {
    result.hasDescendant(this)
  }

  /** Get an operand used as a sender. */
  Identifier getASendingOperand() {
    none()
  }
  
  /** Get an operand used as a receiver. */
  Identifier getAReceivingOperand() {
    none()
  }
  
  Identifier getAnOperand() {
    result = getASendingOperand() or
    result = getAReceivingOperand()
  }
  
  /** Get a data entry used as a sender. */
  DataDescriptionEntry getASendingDataEntry() {
    result = getASendingOperand().(QualifiedDataNameWithSubscripts).getReference().getTarget()
  }
  
  /** Get a data entry used as a receiver. */
  DataDescriptionEntry getAReceivingDataEntry() {
    result = getAReceivingOperand().(QualifiedDataNameWithSubscripts).getReference().getTarget()
  }
}

private predicate stmt_succ(AstNode node, Stmt stmt) {
  exists ( AstNode succ |
    succ = node.getASuccessor() and
    if (succ instanceof Stmt) then
      stmt = succ
    else
      stmt_succ(succ, stmt)
  )
}

class FileIOStmt extends FileIOStmt_ {
  FileControlEntry getAFileControlEntry() {
    none()
  }
}

predicate testsFileStatusRightAfter(FileIOStmt io, DataReference fileStatus) {
  exists ( QualifiedDataNameWithSubscripts operand |
    ( exists ( IfThenElse ite |
        ite = io.getASuccessorStmt() and 
        not ite.hasAncestor(io) and
        operand.hasAncestor(ite.getCondition())
      )
      or
      exists ( Evaluate eval |
        eval = io.getASuccessorStmt() and 
        not eval.hasAncestor(io) and
        ( operand.hasAncestor(eval.getASubject().(AstNode)) or
          operand.hasAncestor(eval.getABranch().(WhenBranch).getAnObject().(AstNode)) )
      )
    ) and
    ( // We either have a direct reference to the file status...
      fileStatus.getTarget() = operand.getReference().getTarget() or
      // Or we have a reference to a level 88 conditional defined for that file status.
      exists ( DataDescriptionEntry l88 |
        level88ForFileStatus(fileStatus, l88) and
        l88 = operand.getReference().getTarget()
      )
    )
  )
}

private 
predicate level88ForFileStatus(DataReference fileStatus, DataDescriptionEntry l88) {
  l88 = fileStatus.getTarget().getANestedEntry() and
  l88.getLevelNumber() = 88
}


class Open extends Open_ {
  override FileControlEntry getAFileControlEntry() {
    result = getAFile().getTarget()
  }
}

class Close extends Close_ {
  override FileControlEntry getAFileControlEntry() {
    result = getAFile().getTarget()
  }
}

class Read extends Read_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget()
  }
}

class Merge extends Merge_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget() or
    result = getAUsing().getTarget() or
    result = getAGiving().getTarget()
  }
}

class Sort extends Sort_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget() or
    result = getAUsing().getTarget() or
    result = getAGiving().getTarget()
  }
}

class Delete extends Delete_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget()
  }
}

class ReturnStmt extends ReturnStmt_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget()
  }
}

class Start extends Start_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget()
  }
}

class Write extends Write_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget() or
    isRecordInFile(getRecord(), result)
  }
}

class Rewrite extends Rewrite_ {
  override FileControlEntry getAFileControlEntry() {
    result = getFile().getTarget() or
    isRecordInFile(getRecord(), result)
  }
}

predicate isRecordInFile(QualifiedDataNameWithSubscripts record, FileControlEntry fileControl) {
  exists (DataDescriptionEntry field, FileDescriptionEntry fd |
    field = record.getReference().getTarget() and
    field.hasAncestor(fd) and
    fileControl.getName() = fd.getName() and
    exists (ProgramDefinition pd |
      fileControl.hasAncestor(pd.getEnvironmentDivision().getIoSection()) and
      fd.hasAncestor(pd.getDataDivision().getFileSection())
    )
  )
  or
  exists (DataDescriptionEntry field, SortMergeFileDescriptionEntry sd |
    field = record.getReference().getTarget() and
    field.hasAncestor(sd) and
    fileControl.getName() = sd.getName() and
    exists (ProgramDefinition pd |
      fileControl.hasAncestor(pd.getEnvironmentDivision().getIoSection()) and
      sd.hasAncestor(pd.getDataDivision().getFileSection())
    )
  )
}

/** A GO TO statement. */
class GoTo extends GoTo_ {
  /**
   * Get a successor of this GO TO statement.
   */
  override AstNode getASuccessor() {
    result = super.getASuccessor() or
    result = getATarget().getTarget() or
    exists(Alter a, AlterationClause c |
      this.getEnclosingProcedureDivision() = a.getEnclosingProcedureDivision()
      and c = a.getAnAlteration()
      and c.getFrom().getTarget() = this.getEnclosingParagraph() |
        result = c.getTo().getTarget())
  }
}

abstract class Perform extends Perform_ {
  /** If this PERFORM is a loop, this returns the loop's form. */
  override LoopForm getLoopForm() { none() }

  /** Match any statement which can be reached as a result from
   * executing this PERFORM. */
  Stmt getAStmtInScope() { none() }
}

/** An inline PERFORM statement. */
class PerformInline extends PerformInline_ {
  override string toString() { result = "PerformInline" }
  
  override Stmt getAStmtInScope() { result.hasAncestor(this) }
}


/** An out-of-line PERFORM statement. */
class PerformOutofline extends PerformOutofline_ {
  /**
   * A successor of this statement, either the start target
   * or the following statement (after completion of the PERFORM).
   */
  override AstNode getASuccessor() {
    result = getAContinuation()
    or result = this.getStartTarget()
  }

  AstNode getAContinuation() {
    result = super.getASuccessor()
  }

  /** Get the start target of this statement. */
  Procedure getStartTarget() {
    result = this.getProcedureName1().getTarget()
  }
  
  /** Get the end target of this statement. */
  Procedure getEndTarget() {
    result = this.getProcedureName2().getTarget() or
    (not exists(this.getProcedureName2()) and
     result = this.getProcedureName1().getTarget())
  }
  
  /** When running this PERFORM statement, 
   * which procedures may get executed ? */
  Procedure getAProcedureInRange() {
    // The start target
    result = getStartTarget()
    or
    // Anything succeeding not-the-end-target.
    exists( Procedure p |
      p = getAProcedureInRange() and
      not (p = getEndTarget()) and
      result = p.getASucceedingProcedure() )
  }

  
  override Stmt getAStmtInScope() {
    exists(Procedure proc |
      proc = getAProcedureInTransitiveRange() and
      result = proc.getAStatement()
    )
  }

  /** When running this PERFORM statement, 
   * which procedures may get executed,
   * including those which result from execution
   * of more PERFORM statements ? */
  Procedure getAProcedureInTransitiveRange() {
    result = this.getAProcedureInRange()
    or
      exists ( Procedure intermediate, PerformOutofline pool |
      intermediate = getAProcedureInRange() and
      pool = intermediate.getAStatement() and
      result = pool.getAProcedureInTransitiveRange()
    )
  }
}

/** A decision statement. */
abstract class Decision extends AstNode {
  abstract Branch getABranch();
}

/** An IF statement. */
class IfThenElse extends IfThenElse_, Decision {
  /** Get either the THEN branch or the ELSE branch. */
  override Branch getABranch() {
    result = this.getThen() or
    result = this.getElse()
  }

  override AstNode getParent() { result = IfThenElse_.super.getParent() }
  override AstNode getASuccessor() { result = IfThenElse_.super.getASuccessor() }
  override string toString() { result = IfThenElse_.super.toString() }
}

/** An EVALUATE statement. */
class Evaluate extends Evaluate_, Decision {
  /** Get any of the WHEN branches. */
  override Branch getABranch() {
    result = Evaluate_.super.getABranch()
  }

  override AstNode getParent() { result = Evaluate_.super.getParent() }
  override AstNode getASuccessor() { result = Evaluate_.super.getASuccessor() }
  override string toString() { result = Evaluate_.super.toString() }
}

/** A MOVE statement. */
class Move extends Move_ {
  override Identifier getASendingOperand() {
    result = getInitialOperand()
  }
  
  override Identifier getAReceivingOperand() {
    result = getAToOperand()
  }
  
  override string toString() { result = "Move" }
}

/** An ADD statement. */
class Add extends Add_ {
  override Identifier getASendingOperand() {
    result = getAnInitialOperand()
    or result = getAToOperand()
  }
  
  override Identifier getAReceivingOperand() {
    if (exists(getAGivingOperand())) then
      result = getAGivingOperand()
    else
      result = getAToOperand()
  }

  /** Get a data entry used as a sender. */
  override DataDescriptionEntry getASendingDataEntry() {
    if (exists(getCorresponding())) then
      result = getCorresponding().getIdentifier().(QualifiedDataNameWithSubscripts).getReference().getTarget().getANestedEntry()
    else 
      result = super.getASendingDataEntry()
  }
  
  /** Get a data entry used as a receiver. */
  override DataDescriptionEntry getAReceivingDataEntry() {
    if (exists(getCorresponding())) then
      result = super.getAReceivingDataEntry().getANestedEntry()
    else 
      result = super.getAReceivingDataEntry()
  }
  
  override string toString() { result = "Add" }
}

/** A SUBTRACT statement. */
class Subtract extends Subtract_ {
  override Identifier getASendingOperand() {
    result = getAnInitialOperand()
    or result = getAFromOperand()
  }
  
  override Identifier getAReceivingOperand() {
    if (exists(getAGivingOperand())) then
      result = getAGivingOperand()
    else
      result = getAFromOperand()
  }
  
  /** Get a data entry used as a sender. */
  override DataDescriptionEntry getASendingDataEntry() {
    if (exists(getCorresponding())) then
      result = getCorresponding().getIdentifier().(QualifiedDataNameWithSubscripts).getReference().getTarget().getANestedEntry()
    else 
      result = super.getASendingDataEntry()
  }
  
  /** Get a data entry used as a receiver. */
  override DataDescriptionEntry getAReceivingDataEntry() {
    if (exists(getCorresponding())) then
      result = super.getAReceivingDataEntry().getANestedEntry()
    else 
      result = super.getAReceivingDataEntry()
  }
  
  override string toString() { result = "Subtract" }
}

/** A MULTIPLY statement. */
class Multiply extends Multiply_ {
  override Identifier getASendingOperand() {
    result = getInitialOperand()
    or result = getAByOperand()
  }
  
  override Identifier getAReceivingOperand() {
    if (exists(getAGivingOperand())) then
      result = getAGivingOperand()
    else
      result = getAByOperand()
  }
  
  override string toString() { result = "Multiply" }
}

/** A DIVIDE statement. */
class Divide extends Divide_ {
  override Identifier getASendingOperand() {
    result = getInitialOperand()
    or result = getAnIntoOperand()
  }
  
  override Identifier getAReceivingOperand() {
    ( if (exists(getAGivingOperand())) then
        result = getAGivingOperand()
      else
        result = getAnIntoOperand()
    ) or
    result = getRemainderOperand()
  }
  
  override string toString() { result = "Divide" }
}

/** A COMPUTE statement. */
class Compute extends Compute_ {
  override Identifier getASendingOperand() {
    result.hasAncestor(getExpr())
  }
  
  override Identifier getAReceivingOperand() {
    result = getAResultOperand()
  }
  
  override string toString() { result = "Compute" }
}

/** An inline PERFORM with a VARYING loop form. */
class PerformVaryingInline extends PerformInline {
  PerformVaryingInline() {
    getLoopForm() instanceof Varying
  }
  
  /** Get the VARYING loop form. */
  Varying getVarying() {
    result = getLoopForm()
  }
  
  /** Get the operand which is being varied. */
  Identifier getOperand() {
    result = getVarying().getOperand()
  }
  
  /** Get the data entry which is being varied. */
  DataDescriptionEntry getEntryOperand() {
    result = getOperand().(QualifiedDataNameWithSubscripts).getReference().getTarget()
  }
}

/** A CALL statement. */
class Call extends Call_ {
  ProgramDefinition getTarget() {
    "\"" + result.getIdentificationDivision().getName() + "\"" 
       = getProgramName().(Literal).getValue()
  }
  
  override string toString() { result = "Call" }
}

class CallArgWithValue extends CallArgWithValue_ {
  override CallArgValue getValue() { none() }
  override string toString() { result = "CallArgWithValue" }
}


class ScopeTerminator extends ScopeTerminator_ {
  Stmt getStmt() { result = getParent() }
  
  override string toString() { result = "ScopeTerminator" }
}

class ComputationalStmt extends ComputationalStmt_ {
    override abstract OnSizeErrorBranch getOnSizeError();
    override abstract NotOnSizeErrorBranch getNotOnSizeError();
    override string toString() { result = "ComputationalStmt" }
}
