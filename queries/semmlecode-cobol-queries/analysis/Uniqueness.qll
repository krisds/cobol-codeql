import cobol

predicate uniqueness_error(int number, string what, string problem) {
    (
        what = "toString" or what = "getLocation" or what = "getNode" or what = "getDefinition" or
        what = "getEntryNode" or what = "getOrigin" or what = "getAnInferredType"
    )
    and
    (
        number = 0 and problem = "no results for " + what + "()"
        or
        number in [2 .. 10]  and problem = number.toString() + " results for " + what + "()"
    )
}
