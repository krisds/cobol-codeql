#!/usr/bin/env bash

# This script is used to generate a new set of database statistics
# based on several Cobol projects. These statistics then get merged into
# one final database statistics file.

DIR="$( cd "$( dirname "$0" )" && pwd )"
COBOL_PACK_DIR="$DIR/../../"
GIT_CODE_DIR="$COBOL_PACK_DIR/../../"
export GIT_CODE_DIR

DBSCHEME="$COBOL_PACK_DIR/queries/semmlecode-cobol-queries/semmlecode.cobol.dbscheme"
DBSTATS="$DBSCHEME.stats"

GENERATE_STATS="$DIR/generate_stats_for_project.py"
MERGE_STATS="$GIT_CODE_DIR/buildutils-internal/merge_stats/merge_stats"

STATS_DIR="$DIR/_stats"
mkdir -p $STATS_DIR
rm $STATS_DIR/*.stats

PROJECT_NAMES=(
  "testsuite"
  "acas"
  "cobcurses"
  "cobol-unit-test"
  "cobxref"
  "example-code"
  "itp-advanced-cobol-final"
  "openjensen"
)

for PROJECT_NAME in "${PROJECT_NAMES[@]}"
do
    echo "Generating stats for $PROJECT_NAME ..."
    python $GENERATE_STATS $PROJECT_NAME
done

echo "Merging all project statistics..."
python $MERGE_STATS \
    --output $DBSTATS \
    --normalise "stmt" \
    --shape "$STATS_DIR/${PROJECT_NAMES[0]}.stats" \
    $STATS_DIR/*.stats
