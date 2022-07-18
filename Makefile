#
#  Author: Hari Sekhon
#  Date: 2022-05-16 15:06:57 +0100 (Mon, 16 May 2022)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/HariSekhon/Jenkins
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
#
#  https://www.linkedin.com/in/HariSekhon
#

# For serious Makefiles see the DevOps Bash tools repo:
#
#	https://github.com/HariSekhon/DevOps-Bash-tools
#
#	Makefile
#	Makefile.in - generic include file with lots of Make targets

SHELL = /usr/bin/env bash

.PHONY: *
default: test clean
	@:

test:
	@# script is in DevOps Bash tools repo, clone whole repo for dependency lib and put it in the $PATH
	check_groovyc.sh

push:
	git push

clean:
	@#echo "Removing .class files"
	find . -name '*.class' -exec rm {} \;

wc:
	git ls-files Jenkinsfile vars/ | xargs wc -l

sync:
	. .envrc; github_repo_fork_sync.sh
