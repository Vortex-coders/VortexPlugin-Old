#!/bin/bash

echo "To download the latest version of the server you must have the following packages: sh, wget, curl, jq"
read -s -p "Dow you want to continue?"

mkdir "server" && cd "server"
wget $(curl "https://api.github.com/repos/Anuken/Mindustry/releases/latest" -s | jq '.assets | .[-1] | .browser_download_url' | sed -E 's/.*"([^"]+)".*/\1/')

echo "Installed"