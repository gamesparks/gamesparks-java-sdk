import fileinput, string, sys, re

if len(sys.argv) < 1:
	print "Provide buildVersion parameter. "
	exit()

buildVersion = sys.argv[1]

fileName = "pom.xml"

replacements = [("<version>", buildVersion, "-SNAPSHOT</version>\n")]


data = []

with open(fileName, "rt") as fin:
	data = fin.readlines()


output = []

for l in data:
	for replace in replacements:
		if not replace[0] or not replace[2] in l:
			continue
		
		splits = l.rsplit(replace[0]);
		l = splits[0] + replace[0] + replace[1] + replace[2]
	output.append(l)

with open(fileName, "wt") as fout:
	for line in output:
		fout.write(line)