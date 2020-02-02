/** A source element for which the number of lines of code and
  * comments is available */
class SourceLine extends @sourceline {
  /** the number of lines in this file */
  int getNumberOfLines() {
    numlines(this,result,_,_,_)
  }

  /** the number of lines containing code in this file */
  int getNumberOfLinesOfCode() {
    numlines(this,_,result,_,_)
  }

  /** the number of lines containing comments in this file */
  int getNumberOfLinesOfComments() {
    numlines(this,_,_,result,_)
  }

  /** the number of lines containing water in this file */
  int getNumberOfLinesOfWater() {
    numlines(this,_,_,_,result)
  }

  string toString() {
    result = "SourceLine: (" +
           getNumberOfLines() + "," +
           getNumberOfLinesOfCode() + "," +
           getNumberOfLinesOfComments() + ")"
  }
}