import cobol

class QualifiedDataNameWithSubscripts extends QualifiedDataNameWithSubscripts_ {
  private
  int pictureSizeFromRef() {
    if (getSubscriptsSize() > 0) then
      result = getReference().getTarget().getSizeForSingleElement()
    else
      result = getReference().getTarget().getSizeInFull()
  }

  private
  int modifiedSizeFromRef() {
    result = getReferenceModifier().getLengthValue()
  }

  private
  int modifiedRemainingFromRef() {
    result = pictureSizeFromRef() + 1 - getReferenceModifier().getStartValue()
  }

  private
  predicate hasUnknownConstraint() {
    exists(getReferenceModifier().getLength()) and
    not exists(getReferenceModifier().getLengthValue())
  }
  
  int sizeFromRef() {
    not hasUnknownConstraint() and
    result = min(int x |
      x = pictureSizeFromRef() or
      x = modifiedSizeFromRef() or
      x = modifiedRemainingFromRef()
    )
  }
  
  override string toString() { result = "QualifiedDataNameWithSubscripts" }
}
