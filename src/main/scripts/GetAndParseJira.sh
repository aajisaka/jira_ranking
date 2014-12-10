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

# Number of issues get per request. At most 100.
SIZE=100

if [ $# -lt 1 -o $# -gt 2 ]; then
  echo "Usage: ./GetAndParseJira.sh <count> [<size>]" 1>&2
  exit 1
fi

COUNT=`expr $1 - 1`

if [ $# -eq 2 ]; then
  if [ $SIZE -ge 1 -a $SIZE -le 100 ]; then
    SIZE=$2
  fi
fi

for i in `seq 0 $COUNT`
do
  # Get JSON formatted information from ASF JIRA via REST API
  offset=`expr $i \* $SIZE`
  wget -O HADOOP${i}.json "https://issues.apache.org/jira/rest/api/2/search?jql=project%20in%20(HADOOP%2C%20HDFS%2C%20MAPREDUCE%2C%20YARN)%20AND%20status%20in%20(Resolved%2C%20Closed)%20ORDER%20BY%20updated%20DESC&maxResults=${SIZE}&startAt=${offset}"
  
  # Parse JSON
  ./JiraParse.py HADOOP${i}.json >> tmp.tsv

  /bin/rm -f HADOOP${i}.json
done

# Get input format (issue_no, project, reporter, assignee) from parsed file
sed 's/-/	/' tmp.tsv > tmp2.tsv
awk -F'\t' '{print $2"\t"$1"\t"$5"\t"$3}' tmp2.tsv > jira.tsv

/bin/rm -f tmp.tsv tmp2.tsv
