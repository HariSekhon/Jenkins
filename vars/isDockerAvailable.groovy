//
//  Author: Hari Sekhon
//  Date: 2022-07-03 18:47:04 +0100 (Sun, 03 Jul 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  Liceese: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//                     I s   D o c k e r   A v a i l a b l e
// ========================================================================== //

// returns Boolean if 'docker' command is found in the $PATH
//
// XXX: could extend to check that Docker server is responding too

def call () {
  isCommandAvailable('docker')
}
