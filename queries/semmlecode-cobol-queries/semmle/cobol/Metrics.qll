import Units

class UnitMetrics extends Unit, SourceLine {
  /** the number of distinct operators in this unit */
  int getHalsteadn1Distinct() {
    halstead_counts(this,result,_,_,_)
  }

  /** the number of distinct operands in this unit */
  int getHalsteadn2Distinct() {
    halstead_counts(this,_,result,_,_)
  }

  /** the total number of operators in this unit */
  int getHalsteadN1() {
    halstead_counts(this,_,_,result,_)
  }

  /** the total number of operators in this unit */
  int getHalsteadN2() {
    halstead_counts(this,_,_,_,result)
  }

  /**
   *  Get the Halstead vocabulary size of this unit.
   *  This is the sum of the n1 and n2 Halstead metrics
   */
  int getHalsteadVocabulary() {
    result = getHalsteadn1Distinct() + getHalsteadn2Distinct()
  }

  /**
   *  Get the Halstead length of this unit.
   *  This is the sum of the N1 and N2 Halstead metrics.
   */
  int getHalsteadLength() {
    result = getHalsteadN1() + getHalsteadN2()
  }

  /**
   *  Get the Halstead volume of this unit. This is the
   *  Halstead size multiplied by the log of the
   *  Halstead vocabulary. It represents the information
   *  content of the function.
   */
  float getHalsteadVolume() {
    result = ((float)this.getHalsteadLength()) * this.getHalsteadVocabulary().log2()
  }

  /**
   * Get the Halstead implementation effort for this unit.
   * This is the product of the volume and difficulty.
   */
  float getHalsteadEffort() {
    result = this.getHalsteadVolume() * this.getHalsteadDifficulty()
  }

  /**
   * Get the Halstead difficulty value of this unit. This is proportional to the number of unique
   * operators, and further proportional to the ratio of total operands to unique operands.
   */
  float getHalsteadDifficulty() {
    result = (float)(this.getHalsteadn1Distinct() *
                     this.getHalsteadN2()) /
                       (float)(2 * this.getHalsteadn2Distinct())
  }

  /**
   * Get the Halstead 'delivered bugs' metric for this unit.
   * This metric correlates with the complexity of
   * the software, but is known to be an underestimate of bug counts.
   */
  float getHalsteadDeliveredBugs() {
    result = this.getHalsteadEffort().pow(2.0/3.0) / 3000.0
  }

  /**
   * Get the maximum nesting depth of statements in this unit.
   */
  int getStatementNestingDepth() {
    result = max(Stmt s | s.getParent+() = this
                        | count(Stmt t | t = s.getParent+()))
  }

  /** the number of statements in this unit */
  int getNumberOfStmts() {
    result = count(Stmt s | s.hasAncestor(this) and not s.isCompilerGenerated())
  }

  override string toString() { result = this.(Unit).toString() }
}


class ProgramDefinitionMetrics extends UnitMetrics, ProgramDefinition {
  /**
   * Get the approximate Cyclomatic Complexity for this program.
   * This is calculated for a single unit as E - N + 2 (where E and N are
   * the number of edges and nodes in the control-flow graph respectively)
   * and is closely approximated by PI + 1, where PI is the number of
   * branch statements.
   */
  int getCyclomaticComplexity() {
    result = sum(Alter a | this.contains(a) | a.getAlterationsSize()) +
             sum(GoTo x | this.contains(x) and exists(x.getDependingOn()) | x.getTargetsSize()) +
             count(IfThenElse x | this.contains(x)) +
             sum(Evaluate x | this.contains(x) | x.getBranchesSize()) +
             count(Until x | this.contains(x)) +
             count(Varying x | this.contains(x)) +
             count(Times x | this.contains(x)) +
             count(OnExceptionBranch x | this.contains(x)) +
             count(NotOnExceptionBranch x | this.contains(x)) +
             count(OnSizeErrorBranch x | this.contains(x)) +
             count(NotOnSizeErrorBranch x | this.contains(x)) +
             count(OnOverflowBranch x | this.contains(x)) +
             count(InvalidKeyBranch x | this.contains(x)) +
             count(NotInvalidKeyBranch x | this.contains(x)) +
             count(AtEndBranch x | this.contains(x)) +
             count(NotAtEndBranch x | this.contains(x)) +
             count(AtEndOfPageBranch x | this.contains(x)) +
             count(NotAtEndOfPageBranch x | this.contains(x)) +
             1
  }
  
  override string toString() { result = ProgramDefinition.super.toString() }
  override ProgramDefinitionMetrics getMetrics() { result = this }
}


class CompilationUnitMetrics extends ProgramDefinitionMetrics, CompilationUnit {
  /**
   * Get the average Cyclomatic Complexity of the programs in this compilation unit.
   */
  override int getCyclomaticComplexity() {
    result = avg(ProgramDefinition pd |
                 pd.getParent*() = this |
                 ProgramDefinitionMetrics.super.getCyclomaticComplexity())
  }

  /**
   * Get the SEI Maintainability Index for this compilation unit.
   * M = 171 - 5.2 * ln(V) - 0.23 * G - 16.2 * ln (LOC) + 50 * sin (sqrt(2.4 * CM))
   */
  float getSEIMaintainabilityIndex() {
    result = 171.0 - 5.2 * this.getHalsteadVolume().log()
                   - 0.23 * (float)this.getCyclomaticComplexity()
                   - 16.2 * this.getNumberOfLinesOfCode().log()
                   - 50.0 * (2.46 * 100.0 * ((float)this.getNumberOfLinesOfComments() /
                                             (float)this.getNumberOfLines())).sqrt().sin()
  }
  
  override string toString() { result = CompilationUnit.super.toString() }
  override CompilationUnitMetrics getMetrics() { result = this }
}
