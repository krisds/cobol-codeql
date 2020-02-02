import Files

/** A location as given by a file, a start line, a start column,
 * an end line, and an end column. */
class Location extends @location {
  /** Gets the file for this location */
  File getFile() {
    locations_default(this, result, _, _, _, _)
  }

  /** Gets the start line of this location */
  int getStartLine() {
    locations_default(this, _, result, _, _, _)
  }

  /** Gets the start column of this location */
  int getStartColumn() {
    locations_default(this, _, _, result, _, _)
  }

  /** Gets the end line of this location */
  int getEndLine() {
    locations_default(this, _, _, _, result, _)
  }

  /** Gets the end column of this location */
  int getEndColumn() {
    locations_default(this, _, _, _, _, result)
  }
  
  /** Get the number of lines covered by this location. */
  int getNumLines() {
    result = getEndLine() - getStartLine() + 1
  }

  string toString() {
    result = this.getFile().getName() + ":" + this.getStartLine().toString()
  }

  predicate hasLocationInfo(string filepath, int bl, int bc, int el, int ec) {
    exists(File f |
      locations_default(this, f, bl, bc, el, ec) and
      filepath = f.getAbsolutePath()
    )
  }
}

/** A source element with a location. */
class Locatable extends @locatable {
  /** Get this element's location. */
  Location getLocation() {
    hasLocation(this, result)
  }
  
  /** Get the number of lines covered by this element. */
  int getNumLines() {
    result = getLocation().getNumLines()
  }

  string toString() {
    none()
  }
}
