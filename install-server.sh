#!/bin/bash

mkdir "server" && cd "server"
wget $(curl "https://api.github.com/repos/Anuken/Mindustry/releases/latest" -s | jq '.assets | .[-1] | .browser_download_url' | sed -E 's/.*"([^"]+)".*/\1/')

echo "Installed"