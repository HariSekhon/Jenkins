#!/usr/bin/env bash
#  vim:ts=4:sts=4:sw=4:et
#
#  Author: Hari Sekhon
#  Date: 2022-06-27 11:07:18 +0100 (Mon, 27 Jun 2022)
#
#  https://github.com/HariSekhon/Jenkins
#
#  License: see accompanying Hari Sekhon LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
#
#  https://www.linkedin.com/in/HariSekhon
#

# https://github.com/HariSekhon/Jenkins/blob/master/vars/download.sh

# Downloads and replaces existing groovy files in the local directory with the latest function/pipeline files from HariSekhon/Jenkins if they exist upstream
#
# This allows one to maintain a local private copy of select functions, and get any updates periodically if wanted
#
# This is not as automated as running a direct fork which has 1 button sync and 1 button Pull Requests
# - you will need to git diff and commit yourself, and also correct any divergence by hand, but this was requested by one client for more control
#
# Usage:
#
#   cd to your repos library repo's vars/ directory, then run:
#
#       cd vars/
#
#       curl -f https://raw.githubusercontent.com/HariSekhon/Jenkins/master/vars/download.sh > download.sh && chmod +x download.sh
#
# Download or update any number of functions you want, given as their groovy filenames as you see in the HariSekhon/Jenkins repo::
#
#       ./download.sh argoDeploy.groovy
#
# Or run without any args to search for every *.groovy file in the local directory under vars/ and, if available in HariSekhon/Jenkins repo, overwrite it with the latest version from GitHub
#
#       ./download.sh
#
# This will first check for a local checkout at checkout path $checkout (~/github/jenkins), which makes development easier and can also be used to override using local forked function copies if available

set -euo pipefail
[ -n "${DEBUG:-}" ] && set -x
#srcdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# in case we want to download into a different dir than where we store this download.sh
#cd "$srcdir"

url="https://raw.githubusercontent.com/HariSekhon/Jenkins/master/vars"
checkout=~/github/jenkins

if git remote -v | grep -q '^origin[[:space:]].*HariSekhon/Jenkins'; then
    echo "Running out of origin repo, aborting..."
    exit 1
fi

tmp="$(mktemp)"

unalias cp &>/dev/null || :
unalias mv &>/dev/null || :

filelist=("$@")
if [ $# -eq 0 ]; then
    filelist=(*.groovy)
fi

if [ -d "$checkout" ]; then
    # only allow the use of the checkout if it's really the original origin
    pushd "$checkout" >/dev/null
    if ! git remote -v 2>/dev/null | grep -qi 'origin.*harisekhon'; then
        checkout=""
    fi
    popd >/dev/null
fi

for filename in "${filelist[@]}"; do
    if ! [[ "$filename" =~ \. ]]; then
        filename+=".groovy"
    fi
    # much faster if you have a local checkout, easier for development, and gives option of using local override functions
    if [ -f "$checkout/vars/$filename" ]; then
        # not really faster,  safer to cp
        #tmp="$checkout/vars/$filename"
        cp "$checkout/vars/$filename" "$tmp"
        echo "Copied $filename" >&2
    else
        curl -sf "$url/$filename" > "$tmp" || continue
        echo "Downloaded $filename" >&2
    fi
    if [ -s "$tmp" ]; then
        shebang_detected=0
        comment_line="$(head -n1 "$tmp")"
        if [ "$(cut -c1-2 <<< "$comment_line")" = "#!" ]; then
            shebang_detected=1
            echo "$comment_line"
            comment_line="$(sed -n '2p' "$tmp")"
        fi

        comment_char="$(cut -c1 <<< "$comment_line")"
        if [ "$comment_char" = "/" ]; then
            comment_char="//"
        fi
        if [[ "$comment_char" =~ [#/] ]]; then
            echo "$comment_char"
            echo "$comment_char copied from $url/$filename"
            echo "$comment_char"
        fi

        #sed 's|//.*|| ; /^[[:space:]]*$/d' "$tmp"
        if [ "$shebang_detected" = 1 ]; then
            tail -n+2 "$tmp"
        else
            cat "$tmp"
        fi
    fi > "$filename"
done
