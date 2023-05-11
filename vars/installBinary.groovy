//
//  Author: Hari Sekhon
//  Date: 2022-06-21 13:12:02 +0100 (Tue, 21 Jun 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//                          I n s t a l l    B i n a r y
// ========================================================================== //

// Downloads a URL, extracts the given binary from the tarball/zip and copies it to $HOME/bin

// Adapted from the more advanced DevOps Bash tools repo's install_binary.sh and supporting scripts and lib/utils.sh
// TODO: could make this native Groovy instead of sh script

// you may need to call this to ensure the prerequisite commands for curl / tar / unzip are available:
//
//    installPackages(['curl', 'tar', 'unzip'])

// binary should be path to the binary after being unpacked, including any intermediate directory paths in an unpacked tarball or zip package
def call (Map args = [url: '', binary: '', overwrite: false, timeout: 15, timeoutUnits: 'MINUTES']) {

  args.url = args.url ?: error('URL not specified')
  args.binary = args.binary ?: args.url.tokenize('/')[-1]
  args.overwrite = args.overwrite ?: false
  args.timeout = args.timeout ?: 15
  args.timeoutUnits = args.timeoutUnits ?: 'MINUTES'

  timeout (time: args.timeout, unit: args.timeoutUnits) {

    // avoiding using Bash specific constructs like ${var//}
    //script {
    //  os = sh (returnStdout: true, label: 'Determine OS', script: 'uname -s').trim().toLowerCase()
    //  arch = sh (returnStdout: true, label: 'Determine Architecture', script: 'uname -m').trim()
    //  if (arch == 'x86_64') {
    //    arch = 'amd64' // files are conventionally named amd64 not x86_64
    //  }
    //  args.url = args.url.replaceAll('\\{os\\}' , "$os")
    //  args.url = args.url.replaceAll('\\{arch\\}' , "$arch")
    //}

    withEnv([
        "URL=${args.url}",
        "BINARY=${args.binary}",
        "OVERWRITE=${args.overwrite}",
        //"OS=$os",
        //"ARCH=$arch",
      ]
    ) {
      sh (
        // Blue Ocean UI doesn't put a space between from and url not matter what I do, must be a minor bug
        label: "Install Binary '$BINARY' from:  ${env.URL}", // using $URL or ${args.url} here prints 'class java.net.URL'
        script: '''
          set -eux

          # gets this error and exits with error code 2 even when || : is appended in gcloud-sdk container, which has bash too but must be running in strict bourne mode
          # even if running this before set -e this still happens
          #
          #   /home/jenkins/agent/workspace/test@tmp/durable-7bb89a26/script.sh: 3: set: Illegal option -o pipefail
          #
          #set -o pipefail || :

          if [ -z "$URL" ]; then
              echo "No URL passed to installBinary()"
              exit 1
          fi
          if [ -z "$BINARY" ]; then
              echo "No binary name passed to installBinary()"
              exit 1
          fi

          destination=~/bin

          mkdir -p -v "$destination"

          cd "$destination"

          if [ -f "BINARY" ]; then
              if [ "$OVERWRITE" != true ]; then
                echo "$BINARY already present, skipping"
                exit 0
              fi
          fi

          os="$(uname -s | tr '[:upper:]' '[:lower:]')"
          arch="$(uname -m)"

          if [ "$arch" = x86_64 ]; then
              arch=amd64  # files are conventionally named amd64 not x86_64
          fi

          URL="$(echo "$URL" | sed "s/{os}/$os/")"
          URL="$(echo "$URL" | sed "s/{arch}/$arch/")"

          package="${URL##*/}"
          tmp="/tmp/installBinary.$$"
          download_file="$tmp/$package"

          mkdir -p -v "$tmp"

          # copied from DevOps Bash tools lib/utils.sh
          export TRAP_SIGNALS="INT QUIT TRAP ABRT TERM EXIT"
          trap "rm -f '$download_file'" $TRAP_SIGNALS

          cd "$tmp"

          echo "Downloading: $URL"
          curl -sSLf -o "$download_file" "$URL"

          # adapted from has_tarball_extension() in lib/utils.sh
          if echo "$package" | grep -Eq '\\.(tgz|tar\\.gz|tbz|tar\\.bz2)$'; then
              echo "Extracting tarball package"
              if echo "$package" | grep -Eq '\\.(tgz|tar\\.gz)$'; then
                  tar xvzf "$download_file"
              elif echo "$package" | grep -Eq '\\.(tbz|tar\\.bz2)$'; then
                  tar xvjf "$download_file"
              fi
              if ! [ -f "$BINARY" ]; then
                  echo "Failed to find binary '$BINARY' in unpacked '$download_file' - is the given binary filename / path correct?"
                  exit 1
              fi
              download_file="$BINARY"
              echo
          elif echo "$package" | grep -Eq '\\.zip$'; then
              echo "Extracting zip package"
              unzip -o "$download_file"
              download_file="$BINARY"
          fi

          echo "Setting executable: $download_file"
          chmod +x "$download_file"
          echo

          destination="$destination/${download_file##*/}"
          # if there are any -linux-amd64 or -amd64-linux suffixes remove them either way around (this is why $os is stripped before and after)
          destination="${destination%%-$os}"
          destination="${destination%%_$os}"
          destination="${destination%%-$arch}"
          destination="${destination%%_$arch}"
          destination="${destination%%-$os}"
          destination="${destination%%_$os}"

          install_path="${destination%/*}"

          # common alias mv='mv -i' would force a prompt we don't want, even with -f
          unalias mv >/dev/null 2>&1 || :
          mv -fv "$download_file" "$destination"
          echo

          echo "Installation complete"
        '''
      )
    }
  }
}
