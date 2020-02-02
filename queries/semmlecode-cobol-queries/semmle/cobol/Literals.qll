import cobol

class Literal extends Literal_ {
  override string toString() { result = this.getValue() }

  /**
   * Is this literal alphanumeric, regardless of context?
   */
  predicate isAlphanumeric() { none() }

  /**
   * Is this literal numeric, regardless of context? 
   */
  predicate isNumeric() { none() }
}

class AlphanumericLiteral extends AlphanumericLiteral_ {
  override string toString() { result = this.getValue() }

  override predicate isAlphanumeric() { any() }
}

class NumericLiteral extends NumericLiteral_ {
  override string toString() { result = this.getValue() }

  override predicate isNumeric() { any() }
}

class OtherLiteral extends OtherLiteral_ {
  override string toString() { result = this.getValue() }
}

class FigurativeConstantLiteral extends FigurativeConstantLiteral_ {
  override string toString() { result = this.getValue() }
}

class AllLiteral extends FigurativeConstantLiteral {
  AllLiteral() {
    getValue().toUpperCase().matches("ALL%")
  }

  override predicate isAlphanumeric() { getLiteral().isAlphanumeric() }
  override predicate isNumeric() { getLiteral().isNumeric() }
}

class SpaceLiteral extends FigurativeConstantLiteral {
  SpaceLiteral() {
    getValue().toUpperCase().matches("SPACE%")
  }

  override predicate isAlphanumeric() { any() }
}

class QuoteLiteral extends FigurativeConstantLiteral {
  QuoteLiteral() {
    getValue().toUpperCase().matches("QUOTE%")
  }

  override predicate isAlphanumeric() { any() }
}
