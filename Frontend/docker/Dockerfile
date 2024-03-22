# Created by "Jiří Štefka <jiri@stefka.eu>"
# This code is under the GPLv3 license.
FROM node:18-alpine

LABEL developer="Jiří Štefka <jiri@stefka.eu>"
LABEL maintainer="Jiří Štefka <jiri@stefka.eu>"

# Install git (needed to download the Frontend)
RUN apk add --no-cache git

# Clone Ree6 frontend
RUN git clone https://github.com/Ree6-Applications/Webinterface /Webinterface

# Setup the files and permissions
RUN chown -R node:node /Webinterface
USER node:node
WORKDIR /Webinterface/Frontend

# Install the needed modules
RUN npm install vite && npm install && npm i -D @sveltejs/adapter-node --force

# Patch the config to use the node adapter
RUN sed -i "s@import adapter from '\@sveltejs/adapter-auto';@import adapter from '\@sveltejs/adapter-node';@g" svelte.config.js

RUN npm run build
RUN rm -rf src/ static/

CMD ["node", "build/index.js"]
