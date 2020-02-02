import AST

class Comment extends Comment_ {
  Location getScope() {
    commentScopes(this, result)
  }


  override string toString() { result = "Comment" }
}

/**
 * An lgtm suppression comment.
 */
class LgtmSuppressionComment extends Comment {
    string annotation;

    // Logic here based on what's found in python.
    LgtmSuppressionComment() {
        // match `lgtm[...]` anywhere in the comment
        annotation = this.getText().regexpFind("(?i)\\blgtm\\s*\\[[^\\]]*\\]", _, _)
        or
        // match `lgtm` at the start of the comment and after semicolon
        annotation = this.getText().regexpFind("(?i)(?<=^|;)\\s*lgtm(?!\\B|\\s*\\[)", _, _).trim()
    }

    /** Gets the suppression annotation in this comment. */
    string getAnnotation() {
        result = annotation
    }
}
