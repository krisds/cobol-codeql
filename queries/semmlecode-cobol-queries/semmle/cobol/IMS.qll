import AST

class CBLTDLICall extends Call {
  CBLTDLICall() {
    exists ( string cbltdli |
      cbltdli = getProgramName().(AlphanumericLiteral).getValue().toUpperCase() and
      ( cbltdli = "'CBLTDLI'"
        or cbltdli = "\"CBLTDLI\""
      )
    )
  }
  
  CallArg getPCBArg() {
    result = getUsing(1)
  }
  
  DataDescriptionEntry getPCBEntry() {
    result = getPCBArg().(CallArgByReference).getValue().(QualifiedDataNameWithSubscripts).getReference().getTarget()
  }
}

/* Program Communication Block */
class PCB extends DataDescriptionEntry {
  PCB() {
    getLevelNumber() = 1 and
    // "All the PCBs used in the application program must be defined inside the 
    // Linkage Section of the COBOL program because PCB resides outside the 
    // application program."
    getParent().getParent() instanceof LinkageSection and
    exists (CBLTDLICall call | this = call.getPCBEntry())
  }

  /** The status entry starts at position 10 in the PCB. */
  DataDescriptionEntry getStatusEntry() {
    result = getEntryAtOffset(10)
  }
}