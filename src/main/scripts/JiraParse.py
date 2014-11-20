#!/usr/bin/python
# -*- coding: utf-8 -*-

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
