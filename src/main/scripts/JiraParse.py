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
issues = data.get('issues')

for issue in issues:
  key = issue.get('key').encode("utf-8")

  assignee = issue.get('fields').get('assignee', None)
  if assignee is None:
    assigneeDisplayName = 'none'
    assigneeCompanyName = 'none'
  else:
    assigneeDisplayName = assignee.get('displayName').encode("utf-8")
    assigneeEmailAddress = assignee.get('emailAddress')
    assigneeCompanyName = re.sub(r'^.+@', '', assigneeEmailAddress).encode("utf-8")

  reporter = issue.get('fields').get('reporter', None)
  if reporter is None:
    reporterDisplayName = 'none'
    reporterCompanyName = 'none'
  else:
    reporterDisplayName = reporter.get('displayName').encode("utf-8")
    reporterEmailAddress = reporter.get('emailAddress')
    reporterCompanyName = re.sub(r'^.+@', '', reporterEmailAddress).encode("utf-8")
  
  output = key + '\t' + assigneeDisplayName + '\t' + assigneeCompanyName + '\t' + reporterDisplayName + '\t' + reporterCompanyName  
  print(output)

f.close()
