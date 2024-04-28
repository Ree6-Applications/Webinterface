#!/bin/sh
# Created by "Jiří Štefka <jiri@stefka.eu>"
# This code is under the GPLv3 license.
set -e

API_URL="${BACKEND_URL:-https://api.ree6.de}"
INVITE_LINK="${INVITE_URL:-https://invite.ree6.de}"
DEBUG="${DEBUG:-false}"

cd /Webinterface/Frontend

rm -f .env
echo -e "VITE_API_URL=${API_URL}\nVITE_INVITE_URL=${INVITE_LINK}" > .env

echo "BACKEND_URL is '${API_URL}'"
echo "Bot's INVITE_LINK is '${INVITE_LINK}'"
echo "DEBUG is '${DEBUG}'"
echo ===========================================================

# Build Frontend
# If debug flag is not set or is set to false do redirect stdout to /dev/null
echo "Building Frontend..."
if [[ "${DEBUG}" == "false" ]]; then
  export NODE_ENV="production"
else
  # npm run build && echo "Frontend build successful!" || echo "Frontend build failed!"
  export NODE_ENV="development"
fi

npm run build >/dev/null 2>&1 && echo "Frontend build successful!" || echo "Frontend build failed!"

node build/index.js