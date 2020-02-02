import AST

class BinaryExpr extends BinaryExpr_ {
  override abstract Expr getLeftOperand();
  override abstract Expr getRightOperand();
    
  Expr getAnOperand() {
    result = getLeftOperand() or
    result = getRightOperand()
  }
}

class AbbrLogExpr extends AbbrLogExpr_ {
  RelationOperand getAnObject() {
    none()
  }
}

class AbbrLogAndExpr extends AbbrLogAndExpr_ {
  override RelationOperand getAnObject() {
    result = getLeftOperand().getAnObject() or
    result = getRightOperand().getAnObject() 
  }
}

class AbbrLogNotExpr extends AbbrLogNotExpr_ {
  override RelationOperand getAnObject() {
    result = getExpression().getAnObject()
  }
}

class AbbrLogOrExpr extends AbbrLogOrExpr_ {
  override RelationOperand getAnObject() {
    result = getLeftOperand().getAnObject() or
    result = getRightOperand().getAnObject() 
  }
}

class RelationObjectExpr extends RelationObjectExpr_ {
  override RelationOperand getAnObject() {
    result = getOperand() or
    result = getOperand().(AbbrLogExpr).getAnObject()
  }
}

class ComparisonExpr extends ComparisonExpr_ {
  RelationOperand getAnObject() {
    result = getSubject() or
    result = getObject().getAnObject()
  }
}
