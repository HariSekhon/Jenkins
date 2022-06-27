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

# Downloads and replaces existing groovy files in the local directory with the upstream files from HariSekhon/Jenkins if they exist upstream
#
# This allows one to maintain a local private copy of select functions, and get any updates periodically if wanted
#
# This is not as automated as running a direct fork which has 1 button sync and 1 button Pull Requests
# - you will need to git diff and commit yourself, and also correct any divergence by hand, but this was requested by one client for more control

set -euo pipefail
[ -n "${DEBUG:-}" ] && set -x
srcdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$srcdir"

url="https://raw.githubusercontent.com/HariSekhon/Jenkins/master/vars"

tmp="$(mktemp)"

unalias mv &>/dev/null || :

for filename in *.groovy; do
    if curl -sf "$url/$filename" > "$tmp"; then
        {
            echo "// copied from $url/$filename"
            echo
            sed 's|//.*|| ; /^[[:space:]]*$/d' "$tmp"
        } > "$filename"
        echo "Downloaded $filename"
    fi
done
