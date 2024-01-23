#!/bin/sh
# Created by "Jiří Štefka <jiri@stefka.eu>"
# This code is under the GPLv3 license.
set -e

API_URL="${BACKEND_URL:-https://api.ree6.de}"
INVITE_LINK="${INVITE_URL:-https://invite.ree6.de}"
DEBUG="${DEBUG:-false}"

cd /Webinterface/Frontend

echo "BACKEND_ULR is '${API_URL}'"
echo "Bot's INVITE_LINK is '${INVITE_LINK}'"
echo "DEBUG is '${DEBUG}'"
echo ===========================================================

# Clear the build directory and reset the repository
rm -rf build/ src/ svelte.config.js
git reset --hard

# Patch svelte.config.js to use adapter-node as adapter-auto doesn't detect the environment
echo "Patching svelte.config.js to use adapter-node..."
sed -i "s@import adapter from '\@sveltejs/adapter-auto';@import adapter from '\@sveltejs/adapter-node';@g" svelte.config.js

# Patch constants.ts to use our api url
echo "Patching constants.ts (applying your settings)..."
sed -i "s@export const BASE_PATH = \"https://api.ree6.de\"@export const BASE_PATH = \"${API_URL}\"@g" src/lib/scripts/constants.ts
# Patch contants.tx to use our invite url
sed -i "s@export const INVITE_URL = \"https://invite.ree6.de\"@export const INVITE_URL = \"${INVITE_LINK}\"@g" src/lib/scripts/constants.ts

# Build Frontend
# If debug flag is not set or is set to false do redirect stdout to /dev/null
echo "Building Frontend..."
if [[ "${DEBUG}" == "false" ]]; then
  export NODE_ENV="production" && npm run build >/dev/null 2>&1 && echo "Frontend build successful!"
else
  # npm run build && echo "Frontend build successful!" || echo "Frontend build failed!"
  npm run build
fi

# Delete source code files that were used to build the app that are no longer needed
rm -rf src/ static/

node build/index.js
