#!/bin/bash
WORK_DIR=`pwd`
#BASE_PATH="$(cd "$(dirname "${BASE_SOURCE[0]}" )" && pwd)"
BASE_PATH=$(cd "$(dirname "${0}" )" && pwd)
echo "work dir is $WORK_DIR"
echo "base dir is $BASE_PATH"

mvn dalgen:run -f $BASE_PATH/pom.xml -Dconfig.dir=$WORK_DIR/config.xml -Dexecute.target=$1 -Dtable.name=$2
