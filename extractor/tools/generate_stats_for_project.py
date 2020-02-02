#!/usr/bin/env python3
#
# This script is used to generate a new set of database statistics
# based on a given cobol project.

import glob
import os
import shutil
import subprocess
import sys
import tempfile

if len(sys.argv) < 2:
	print("No project name given.")
	os.terminate(-1)

project_name = sys.argv[1]

script_dir = os.path.dirname(os.path.realpath(__file__))
cobol_pack_dir = os.path.abspath(os.path.join(script_dir, "..", ".."))
git_code_dir = os.path.abspath(os.path.join(cobol_pack_dir, "..", ".."))
os.environ["GIT_CODE_DIR"] = git_code_dir

dbscheme = os.path.join(cobol_pack_dir, "queries", "semmlecode-cobol-queries", "semmlecode.cobol.dbscheme")
dbstats = os.path.join(script_dir, "_stats", project_name + ".stats")

tmpdir = tempfile.mkdtemp(prefix="odasa-cobol-stats-")
print("Carrying out work in '" + tmpdir + "'")
os.chdir(tmpdir)

project_files = os.path.join(git_code_dir, "buildutils-internal", "dashboard-cobol", "projects", project_name, "*")
files = glob.glob(project_files)
for file in files:
	shutil.copy(file, tmpdir)

print("Adding snapshot...")
subprocess.check_call(["odasa", "addSnapshot", "--latest"])

print("Building snapshot...")
subprocess.check_call(["odasa", "buildSnapshot", "--verbosity", "2", "--latest"])

print("Validating database...")
files = glob.glob(os.path.join(tmpdir, "revision-*"))
for file in files:
	dbdir = os.path.join(file, "working", "db-cobol")
	subprocess.check_call(["odasa", "checkDatabase", "--dbscheme", dbscheme, "--db", dbdir])

print("Collecting stats...")
subprocess.check_call(["odasa", "collectStats", "--dbscheme", dbscheme,
	"--db", dbdir, "--outputFile", "dbstats_temp.txt"])
print("Normalizing line endings...")
os.system("sed 's/\r$//' dbstats_temp.txt > " + dbstats) # remove any Windows line endings

print("Done generating %s." % (dbstats))
