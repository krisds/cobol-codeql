import AST

/** A reference to another entity in the program. */
class Reference extends Reference_ {
  /** Get the name of this reference. */
  string getName() { none() }

  /** Get the name of this reference, in upper case. */
  string getUppercaseName() { result = getName().toUpperCase() }
  
  /** Get the target of this reference. */
  AstNode getTarget() { none() }
}

/** A reference to a procedure (a section or paragraph specified in the Procedure Division). */
class ProcedureReference extends ProcedureReference_ {
  override string toString() {
    if this.isQualified() then
      result = this.getQualification() + "." + this.getName()
    else
      result = this.getName()
  }

  private string getUpperName() { result = getName().toUpperCase() }

  private predicate isQualified() { exists(this.getQualification()) }
  
  private predicate hasQualifiedTarget(Procedure p) {
    p instanceof Paragraph
    and p.getUpperName() = this.getUpperName()
    and exists(ProcedureDivision pd, Section s | 
      s.getUpperName() = this.getQualification().toUpperCase()
      and p.hasAncestor(s)
      and s.hasAncestor(pd)
      and this.hasAncestor(pd)
    )
  }

  private predicate hasGlobalTarget(Procedure p) {
    p.getUpperName() = this.getUpperName()
    and exists(ProcedureDivision pd | p.hasAncestor(pd) and this.hasAncestor(pd))
  }

  private predicate hasLocalTarget(Procedure p) {
    p instanceof Paragraph
    and p.getUpperName() = this.getUpperName()
    and exists(Section x | p.hasAncestor(x) and this.hasAncestor(x))
  }

  /** Does this reference have a particular Procedure target? */
  predicate hasTarget(Procedure p) {
    if this.isQualified() then
      this.hasQualifiedTarget(p)
    else if this.hasLocalTarget(_) then
      this.hasLocalTarget(p)
    else
      this.hasGlobalTarget(p)
  }

  /** Get the target Procedure of this reference. */
  override Procedure getTarget() { hasTarget(result) }
}

/**
 * This predicate is logically part of IndexReference, but
 * has been pulled out so it can be used in the character.
 */
private predicate dataReferenceHasIndexTarget(DataReference_ ref, IndexedBy i) {
  exists(ProgramDefinition pd |
      ref.hasAncestor(pd.getProcedureDivision()) and
      i.hasAncestor(pd.getDataDivision()) and
      i.getAnIndex() = ref.getName())
}

/** A reference to an IndexedBy phrase on an entry in the Data Division. */
class IndexReference extends DataReference_ {
  IndexReference() {
    /* Our definition of an IndexReference is just those instances of
     * @data_reference that do in fact reference some index introduced
     * in the Data Division. This leaves any unresolved DataReference_ to
     * be a DataReference. A name clash would violate "uniqueness of
     * reference" and would not be a legal program.
     */
    dataReferenceHasIndexTarget(this,_)
  }

  /** Does this index reference have a particular IndexedBy phrase target? */
  predicate hasTarget(IndexedBy i) {
    dataReferenceHasIndexTarget(this,i)
  }

  /** Get the target IndexedBy phrase of this index name. */
  override IndexedBy getTarget() {
    hasTarget(result)
  }

  override string toString() { result = "IndexReference" }
}

/** A reference to a user name specified in the Data Division. */
class DataReference extends DataReference_ {
  DataReference() {
    not (this instanceof IndexReference)
  }

  private predicate sameProgramSameName(DataDescriptionEntry e) {
    exists (DataDivision dd, ProgramDefinition pd |
      ( this.hasAncestor(dd) or
        this.hasAncestor(pd.getEnvironmentDivision()) or
        this.hasAncestor(pd.getProcedureDivision())
      ) and
    
      dd = e.getDataDivision() and
      dd = pd.getDataDivision() and
      e.getUppercaseName() = this.getUppercaseName()
    )
  }

  private DataDescriptionEntry qc(int index) {
    (
      index = this.getQualifiersSize() - 1 and
      result.getName() = this.getQualifier(index)
      and exists(ProgramDefinition pd | 
        this.hasAncestor(pd.getProcedureDivision()) and
        result.hasAncestor(pd.getDataDivision())
      )
    ) or (
      // Skipping an intermediate level
      result = qc(index).getANestedEntry() and
      result.getName() = this.getQualifier(index)
    ) or (
      // Matching one qualifier
      result = qc(index + 1).getANestedEntry() and
      result.getName() = this.getQualifier(index)
    )
  }

  /** Does this reference have a particular Data Description Entry target? */
  predicate hasTarget(DataDescriptionEntry e) {
    this.sameProgramSameName(e) and
    (
      not exists(this.getQualifiersList())
      or
      this.getQualifiersSize() = 0
      or
      e.getEntryParent+() = qc(0)
    )
  }

  /** Get the target Data Description Entry of this reference. */
  override DataDescriptionEntry getTarget() { hasTarget(result) }

  override string toString() {
    result = "Reference: " + this.getName()
  }
}

/** A data reference used as a receiving operand. */
class ReceivingDataReference extends DataReference {
  ReceivingDataReference() {
    exists (Stmt s | this = s.getAReceivingOperand().(QualifiedDataNameWithSubscripts).getReference())
  }
}

class FileControlEntry extends FileControlEntry_ {
  override string toString() { result = "FileControlEntry" } 
}


class FileReference extends FileReference_ {
  override FileControlEntry getTarget() {
    result.getUppercaseName() = getUppercaseName() and
    exists (ProgramDefinition pd |
      (this.hasAncestor(pd.getDataDivision()) or
       this.hasAncestor(pd.getEnvironmentDivision()) or
       this.hasAncestor(pd.getProcedureDivision())) and
      result.hasAncestor(pd.getEnvironmentDivision())
    )
  }
}

class ReferenceModifier extends ReferenceModifier_ {
  int getStartValue() { result = getStart().(NumericExpr).getLiteral().toString().toInt() }
  int getLengthValue() { result = getLength().(NumericExpr).getLiteral().toString().toInt() }
}
