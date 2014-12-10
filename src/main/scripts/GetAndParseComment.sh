#!/bin/bash
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

if [ $# -gt 1 ]; then
  echo "Usage: ./GetAndParseComment" 1>&2
  exit 1
fi

COUNT=`expr $1 - 1`

if [ $# -eq 2 ]; then
  if [ $SIZE -ge 1 -a $SIZE -le 100 ]; then
    SIZE=$2
  fi
fi

MAPREDUCE_SIZE=6115
HDFS_SIZE=7450
YARN_SIZE=2930
HADOOP_SIZE=11360

array=();

for i in `seq 1 ${MAPREDUCE_SIZE}`
do
  array+=" "
  array+="MAPREDUCE-$i"
done

for i in `seq 1 ${HDFS_SIZE}`
do
  array+=" "
  array+="HDFS-$i"
done

for i in `seq 1 ${YARN_SIZE}`
do
  array+=" "
  array+="YARN-$i"
done

for i in `seq 1 ${HADOOP_SIZE}`
do
  array+=" "
  array+="HADOOP-$i"
done

#for e in ${array[@]}; do
#    echo $e
#done

for issue in ${array[@]}; do
  wget --no-check-certificate -O comment.json "https://issues.apache.org/jira/rest/api/2/issue/$issue/comment"
  ./CommentParse.py comment.json >> tmp.tsv
  cat comment.json >> comment-all.json
  /bin/rm -f comment.json
done

