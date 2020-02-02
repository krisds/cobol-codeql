#!/usr/bin/env python3

import os
import subprocess
import sys

SCRIPT_DIR = os.path.dirname(__file__)
sys.path.append(os.path.abspath(os.path.join(SCRIPT_DIR, "..", "..", "..", "..", "buildutils-internal", "lib")))

COBOL_POPULATOR       = SCRIPT_DIR + "/../src/com/semmle/cobol/population/CobolPopulator.java"
COBOL_TYPES_FROM_SPEC = SCRIPT_DIR + "/../src/com/semmle/cobol/population/CobolTypesFromSpec.java"
COBOL_RULES_FROM_SPEC = SCRIPT_DIR + "/../src/com/semmle/cobol/population/CobolRulesFromSpec.java"
COBOL_DB_TEMPLATE     = SCRIPT_DIR + "/dbscheme.template"
COBOL_DB_SCHEME       = SCRIPT_DIR + "/../../queries/semmlecode-cobol-queries/semmlecode.cobol.dbscheme"
COBOL_QUERIES         = SCRIPT_DIR + "/../../queries/semmlecode-cobol-queries/semmle/cobol/"
LANGUAGE_JAVA         = SCRIPT_DIR + "/../../../../util-java7/src/com/semmle/util/language/LegacyLanguage.java"

from java_gen            import main as gen_java
from db_gen              import main as gen_db
from query_gen           import main as gen_query
from java_default_types  import main as gen_java_default_types
from java_rules_gen      import main as gen_java_rules

def git_hash_object(path):
    return subprocess.check_output(['git', 'hash-object', path], universal_newlines=True).strip()

print("Generating Cobol Java data model ...")
gen_java(["", "spec", COBOL_POPULATOR])

print("Generating Cobol database scheme ...")
gen_db(["", "spec", COBOL_DB_TEMPLATE, COBOL_DB_SCHEME])

print("Generating Cobol Java default types from spec ...")
gen_java_default_types(["", "spec", COBOL_TYPES_FROM_SPEC])

print("Generating Cobol Java rules from spec ...")
gen_java_rules(["", "spec", COBOL_RULES_FROM_SPEC])

print("Generating Cobol querying library ...")
gen_query(["", "--tostring-all", "cobol", "spec", COBOL_QUERIES])

dbschema_sha = git_hash_object(COBOL_DB_SCHEME)
with open(LANGUAGE_JAVA) as f:
    if not git_hash_object(COBOL_DB_SCHEME) in f.read():
        print("\nWARNING: SHA in %s is out-of-date (should be %s)" % (LANGUAGE_JAVA, git_hash_object(COBOL_DB_SCHEME)))
