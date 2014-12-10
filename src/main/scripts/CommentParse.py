#!/usr/bin/python
# -*- coding: utf-8 -*-
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

import json
import re
import sys

argvs = sys.argv
argc = len(argvs)
if (argc != 2) :
  print 'Usage: $ python %s filename' % argvs[0]
  quit()

f = open(argvs[1])
data = json.load(f)
comments = data.get('comments')

for comment in comments:
  author = comment.get('author', None)

  if author is None:
    authorDisplayName = 'none'
    authorCompanyName = 'none'
  else:
    authorDisplayName = author.get('displayName').encode("utf-8")
    authorEmailAddress = author.get('emailAddress')
    authorCompanyName = re.sub(r'^.+@', '', authorEmailAddress).encode("utf-8")

  output = authorDisplayName + '\t' + authorCompanyName
  print(output)

f.close()
