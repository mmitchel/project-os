#!/bin/bash
set -euo pipefail

mkdir -p "$HOME/.local/bin"
curl -fsSL https://storage.googleapis.com/git-repo-downloads/repo -o "$HOME/.local/bin/repo"
chmod +x "$HOME/.local/bin/repo"
echo "$HOME/.local/bin" >> "$GITHUB_PATH"
