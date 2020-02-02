import Files

class Error extends @error, Locatable {
  override string toString() {
    result = getMessage()
  }

  string getMessage() {
    errors(this,result,_)
  }
  
  predicate isParseError() {
    errors(this,_,1)
  }
}
