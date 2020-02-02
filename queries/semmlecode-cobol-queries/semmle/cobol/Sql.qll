import AST

// Please keep all SQL related code in this library.

class Sql extends Sql_ {
  override string toString() { result = "Sql" } 
}

class SqlReference extends SqlReference_ {
  override string toString() { result = "SqlReference" }
  
  SqlStmt getEnclosingStmt() {
    result = getParent+()
  }
}

class SqlUnknownReference extends SqlUnknownReference_ {
  override string toString() { result = "SqlUnknownReference" }
}

class SqlCursorName extends SqlCursorName_ {
  string getUpperName() { result = getName().toUpperCase() }
  
  predicate matches(SqlCursorName other) {
    getUpperName() = other.getUpperName() and
    if (exists(getModule())) then
      exists (other.getModule())
    else
      not exists(other.getModule())
  }
  
  override string toString() {
    if (exists(getModule())) then
      result = "MODULE." + getName()
    else
      result = getName()
  }
}

class SqlTableName extends SqlTableName_ {
  override string toString() {
    if (exists(getCatalog())) then (
      if (exists(getModule())) then
        result = getCatalog() + ".MODULE." + getName()
      else
        result = getCatalog() + "." + getSchema() + "." + getName()
    )
    else if (exists(getModule())) then
      result = "MODULE." + getName()
    else if (exists(getSchema())) then
      result = getSchema() + "." + getName()
    else
      result = getName()
  }
}

class SqlTableReference extends SqlTableReference_ {
  override string toString() { result = "SqlTableReference" }
}

class SqlTableReferenceList extends SqlTableReferenceList_ {
  override string toString() { result = "SqlTableReferenceList" }
}

class SqlStmt extends SqlStmt_ {
  Sql getEnclosingExecStmt() {
    this = result.getStmt()
  }
  
  override string toString() { result = "SqlStmt" }
}

class SqlOtherStmt extends SqlOtherStmt_ {
  override string toString() { result = "SqlOtherStmt" }
}

class SqlDeclareCursorStmt extends SqlDeclareCursorStmt_ {
  override string toString() { result = "SqlDeclareCursorStmt" }
}

class SqlOpenStmt extends SqlOpenStmt_ {
  override string toString() { result = "SqlOpenStmt" }
}

class SqlCloseStmt extends SqlCloseStmt_ {
  override string toString() { result = "SqlCloseStmt" }
}

class SqlDeleteStmt extends SqlDeleteStmt_ {
  override string toString() { result = "SqlDeleteStmt" }
}

class SqlAlterStmt extends SqlAlterStmt_ {
  override string toString() { result = "SqlAlterStmt" }
}

class SqlCreateStmt extends SqlCreateStmt_ {
  override string toString() { result = "SqlCreateStmt" }
}

class SqlDropStmt extends SqlDropStmt_ {
  override string toString() { result = "SqlDropStmt" }
}

class SqlRenameStmt extends SqlRenameStmt_ {
  override string toString() { result = "SqlRenameStmt" }
}

class SqlDDL extends SqlDDL_ {
  override string toString() { result = "SqlDDL" }
}

class SqlSelectStmt extends SqlSelectStmt_ {
  override string toString() { result = "SqlSelectStmt" }
}

class SqlUpdateStmt extends SqlUpdateStmt_ {
  override string toString() { result = "SqlUpdateStmt" }
}

class SqlLockTableStmt extends SqlLockTableStmt_ {
  override string toString() { result = "SqlLockTableStmt" }
}

class SqlClause extends SqlClause_ {
  override string toString() { result = "SqlClause" }
}

class SqlWhereClause extends SqlWhereClause_ {
  override string toString() { result = "SqlWhereClause" }
}

class SqlFromClause extends SqlFromClause_ {
  override string toString() { result = "SqlFromClause" }
}

class SqlHostParameterName extends SqlHostParameterName_ {
  private predicate sameProgramSameName(DataDescriptionEntry e) {
    exists (DataDivision dd, ProgramDefinition pd |
      ( this.hasAncestor(dd) or
        this.hasAncestor(pd.getEnvironmentDivision()) or
        this.hasAncestor(pd.getProcedureDivision())
      ) and
    
      dd = e.getDataDivision() and
      dd = pd.getDataDivision() and
      e.getName() = this.getName()
    )
  }

  /** Does this reference have a particular Data Description Entry target? */
  predicate hasTarget(DataDescriptionEntry e) {
    this.sameProgramSameName(e)
  }

  /** Get the target Data Description Entry of this reference. */
  DataDescriptionEntry getTarget() { hasTarget(result) }
  
  override string toString() { result = "SqlHostParameterName" }
}
