#!/bin/sh
#
# Scripts for Ranking Jira
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

if [ $# -ne 1 ]; then
  echo "Usage: ./GetAndParseJira.sh <issues>" 1>&2
  exit 1
fi

FILES=`expr $1 / 100 - 1`
REMAINDER=`expr $1 % 100`

# Download json from ASF JIRA via REST API and parse it.
# Number of issues per request is hard-coded to 100
# because ASF JIRA accepts at most 100 issues per request.
if [ $FILES -ge 0 ] ; then
  PARALLEL=2
  seq 0 $FILES | xargs -P$PARALLEL -n1 sh -c 'offset=`expr $0 \* 100`; wget -O $0.json "https://issues.apache.org/jira/rest/api/2/search?jql=project%20in%20(HADOOP%2C%20HDFS%2C%20MAPREDUCE%2C%20YARN)%20AND%20status%20in%20(Resolved%2C%20Closed)%20ORDER%20BY%20updated%20DESC&maxResults=100&startAt=$offset"'
  seq 0 $FILES | xargs -P1 -n1 sh -c './jiraParse.py $0.json >> tmp.tsv'
fi

# Download and parse the remainder
if [ $REMAINDER -gt 0 ] ; then
  OFFSET=`expr $FILES \* 100 + 100`
  wget -O reminder.json "https://issues.apache.org/jira/rest/api/2/search?jql=project%20in%20(HADOOP%2C%20HDFS%2C%20MAPREDUCE%2C%20YARN)%20AND%20status%20in%20(Resolved%2C%20Closed)%20ORDER%20BY%20updated%20DESC&maxResults=$REMAINDER&startAt=$OFFSET"
  ./jiraParse.py reminder.json >> tmp.tsv
fi

# Remove JSON
/bin/rm -f *.json

# Get input format (issue_no, project, reporter, assignee) from parsed file
sed 's/-/	/' tmp.tsv > tmp2.tsv
awk -F'\t' '{print $2"\t"$1"\t"$5"\t"$3}' tmp2.tsv > jira.tsv

/bin/rm -f tmp.tsv tmp2.tsv
