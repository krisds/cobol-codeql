import AST

class DataDescriptionEntry extends DataDescriptionEntry_ {
  private string getGroupItemUsageClause() {
    exists(DataDescriptionEntry p |
      p = this.getEntryParent() |
      if exists(p.getUsage()) then
        result = p.getUsage().getNormalizedUsage()
      else
        result = p.getGroupItemUsageClause())
  }

  /**
   * Get the normalized explicit or implied usage of this data item.
   * This follows the [IBM rules](http://www-01.ibm.com/support/knowledgecenter/SSQ2R2_9.0.0/com.ibm.ent.cbl.zos.doc/PGandLR/ref/rlddeusa.html),
   * returning one of:
   * 
   * 1. an explicit `USAGE` clause on this data item,
   * 2. a `GROUP USAGE` clause on an enclosing data item, or
   * 3. an implied usage determined by the picture string.
   */
  string getNormalizedUsage() {
    if exists(DataDescriptionEntry_.super.getUsage()) then
      result = DataDescriptionEntry_.super.getUsage().getNormalizedUsage()
    else if exists(this.getGroupItemUsageClause()) then
      result = this.getGroupItemUsageClause()
    else if this.getPicture().getNormalizedPictureString().matches("%N%") then
      result = "NATIONAL"
    else
      result = "DISPLAY"
  }
  
  DataDivision getDataDivision() {
    this.hasAncestor(result)
  }

  predicate signIsSeparate() {
    getSign().isSeparate()
    or getEnclosingUnit().getProgramDefinition().getEnvironmentDivision().getConfigurationSection().getSpecialNames().getNumericSign().isSeparate()
  }
  
  /**
   * Get the size of the data item as defined by this picture clause.
   * If this item has no picture clause, the returned value will be zero.
   */
  int getSizeFromPicture() {
    // See STD.BK: 13.18.39.3
    result = count(string s | s = "A" or s = "B" or s = "E" or s = "G" or s = "N" or
                              s = "X" or s = "Z" or s = "9" or s = "0" or s = "1" or
                              s = "/" or s = "," or s = "." or s = "+" or s = "-" or
                              s = "*" or (signIsSeparate() and s = "S")
                            | getPicture().getNormalizedPictureString().indexOf(s))
  }

  private DataDescriptionEntry getANestedNonRedefiningEntry() {
    result = getANestedEntry() and not exists (result.getRedefines())
  }

  private int getMaxSizeMultiplier() {
    if (exists(getOccurs())) then result = getOccurs().getMaximum()
    else result = 1
  }
  
  private int getOverallMaxSizeMultiplier(DataDescriptionEntry e) {
    if (this = e) then result = getMaxSizeMultiplier()
    else result = e.getMaxSizeMultiplier() * getOverallMaxSizeMultiplier(e.getParent().getParent().(DataDescriptionEntry))
  }
  
  /**
   * Get the (maximum) size of the data item as defined by its picture clause,
   * or as the sum of the size of its nested entries, but disregarding any
   * repetition (i.e. OCCURS clause) defined on the item itself.
   * 
   * Note: the size indicates maximum size only, as the appearance of 'OCCURS 
   * DEPENDING ON' clauses requires runtime information to calculate the actual
   * size.
   */
  int getSizeForSingleElement() {
      if (not exists(getANestedEntry())) then
        result = getSizeFromPicture()
      else
        result = sum(DataDescriptionEntry c |
            c = getANestedNonRedefiningEntry+()
          | c.getSizeFromPicture() * getOverallMaxSizeMultiplier(c) / getMaxSizeMultiplier())
  }
  
  /**
   * Get the (maximum) size of the data item as defined by its picture clause,
   * or as the sum of the size of its nested entries.
   * 
   * Note: the size indicates maximum size only, as the appearance of 'OCCURS 
   * DEPENDING ON' clauses requires runtime information to calculate the actual
   * size.
   */
  int getSizeInFull() {
      if (not exists(getANestedEntry())) then
        result = getSizeFromPicture() * getMaxSizeMultiplier()
      else
        result = sum(DataDescriptionEntry c |
            c = getANestedNonRedefiningEntry+()
          | c.getSizeFromPicture() * getOverallMaxSizeMultiplier(c))
  }
  
  DataDescriptionEntry getEntryAtOffset(int offset) {
    offset >= 0 and offset < getSizeInFull() and
    if (not exists(getANestedEntry())) then
      (offset = 0 and result = this)
    else exists(DataDescriptionEntry nested, int d |
      nested = getANestedEntry() and
      d = nested.getOffsetFromParent() and
      offset >= d and
      offset < d + nested.getSizeInFull() and
      result = nested.getEntryAtOffset(d - offset)
    )
  }
  
  int getOffsetFromParent() {
    exists ( DescriptionEntryList list, int n |
      this = list.getItem(n) and
      result = sum(DataDescriptionEntry prev, int m |
          m >= 0 and m < n and
          prev = list.getItem(m)
        | prev.getSizeInFull())
    )
  }
  
  override string toString() {
    if exists(getName()) then
      result = "DataDescriptionEntry " + getLevelNumber() + " " + getName()
    else
      result = "DataDescriptionEntry " + getLevelNumber() + " <filler>"
  }
}

class DisplayItem extends DataDescriptionEntry {
  DisplayItem() {
    this.getNormalizedUsage().matches("DISPLAY%")
    or this.getNormalizedUsage() = "NATIONAL"
  }
}

class BinaryItem extends DataDescriptionEntry {
  BinaryItem() {
    this.getNormalizedUsage() = "BINARY"
  }
}

class PackedDecimalItem extends DataDescriptionEntry {
  PackedDecimalItem() {
    this.getNormalizedUsage() = "PACKED-DECIMAL"
  }
}

class GroupLevelDataItem extends DataDescriptionEntry {
  GroupLevelDataItem() {
    exists(this.getANestedEntry())
  }
}

class TopLevelDataItem extends DataDescriptionEntry {
  TopLevelDataItem() {
    not (getParent().getParent() instanceof DataDescriptionEntry)
  }
}

class ElementaryDataItem extends DataDescriptionEntry {
  ElementaryDataItem() {
    not exists(this.getANestedEntry())
  }
}

class ConstantEntry extends ConstantEntry_ {
  override string toString() { 
    result = "ConstantEntry " + getLevelNumber() + " " + getName()
  }
}

class ReportDescriptionEntry extends ReportDescriptionEntry_ {
  override string toString() { 
    result = "ReportDescriptionEntry " + getName()
  }
}

class ReportGroupDescriptionEntry extends ReportGroupDescriptionEntry_ {
  override string toString() { 
    if exists(getName()) then
      result = "ReportGroupDescriptionEntry " + getLevelNumber() + " " + getName()
    else
      result = "ReportGroupDescriptionEntry " + getLevelNumber() + " <filler>"
  }
}

class ScreenDescriptionEntry extends ScreenDescriptionEntry_ {
  override string toString() { 
    if exists(getName()) then
      result = "ScreenDescriptionEntry " + getLevelNumber() + " " + getName()
    else
      result = "ScreenDescriptionEntry " + getLevelNumber() + " <filler>"
  }
}

class PictureClause extends PictureClause_ {
  override string toString() { 
    result = "PictureClause " + getPictureString()
  }

  /**
   * Does this picture clause correspond to a numeric type?
   */
  predicate isNumeric() {
    (
      getNormalizedPictureString().charAt(_) = "9" or
      getNormalizedPictureString().charAt(_) = "Z"
    ) and not (
      getNormalizedPictureString().charAt(_) = "A" or
      getNormalizedPictureString().charAt(_) = "X"
    )
  }

  /**
   * Does this picture clause correspond to an alphanumeric / alphabetic type?
   */
  predicate isAlphanumeric() {
    getNormalizedPictureString().charAt(_) = "A" or
    getNormalizedPictureString().charAt(_) = "X"
  }

  /**
   * Does this picture clause correspond to an alphanumeric / alphabetic type
   * that can hold just one character?
   */
  predicate isCharacter() {
  	isAlphanumeric() and
  	count(int i |
  	  getNormalizedPictureString().charAt(i) = "A" or
  	  getNormalizedPictureString().charAt(i) = "X" or
  	  getNormalizedPictureString().charAt(i) = "9"
  	) = 1
  }
}

class UsageClause extends UsageClause_ {
  /** 
   * Return a normalized version of the usage clause,
   * preferring the semantic names `BINARY` and
   * `PACKED-DECIMAL` over `COMP-*`.
   */
  string getNormalizedUsage() {
    result = this.getOperand()
                 .toUpperCase()
                 .replaceAll("COMPUTATIONAL", "COMP")
                 .replaceAll("NATIVE", "")
                 .replaceAll("COMP-3", "PACKED-DECIMAL")
                 .regexpReplaceAll("COMP(-[45])?", "BINARY")
                 .trim()
  }

  override string toString() { 
    result = "UsageClause " + getOperand()
  }
}

class ValueClause extends ValueClause_ {
  override string toString() { 
    result = "ValueClause"
  }
}

class DescriptionEntry extends DescriptionEntry_ {
  predicate hasEntryParent(DescriptionEntry parent) {
    parent.getANestedEntry() = this
  }
  
  DescriptionEntry getEntryParent() {
    this.hasEntryParent(result)
  }

  string getUppercaseName() { result = getName().toUpperCase() }
}

class FileDescription extends FileDescription_ {
  override string getName() { none() }

  FileControlEntry getAFileControlEntry() {
    result.getUppercaseName() = getUppercaseName()
  }
}

class SignClause extends SignClause_ {
  predicate isSeparate() {
    exists(getSeparate())
  }
  
  override string toString() { 
    result = "SignClause"
  }
}

class NumericSignClause extends NumericSignClause_ {
  predicate isSeparate() {
    exists(getSeparate())
  }
  
  override string toString() {
    result = "NumericSignClause"
  }
}

class OccursClause extends OccursClause_ {
  /** Get the minimum of this `OccursClause`. */
  override int getMinimum() {
    if (exists (super.getMinimum())) then
      result = super.getMinimum()
    else
      result = getMaximum()
  }
  
  override string toString() { result = "OccursClause" }
}
