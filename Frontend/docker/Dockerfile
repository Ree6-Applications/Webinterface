# Created by "Jiří Štefka <jiri@stefka.eu>"
# This code is under the GPLv3 license.
FROM node:18-alpine

LABEL developer="Jiří Štefka <jiri@stefka.eu>"
LABEL maintainer="Jiří Štefka <jiri@stefka.eu>"

# Install git (needed to download the Frontend)
RUN apk add --no-cache git

COPY start.sh /start.sh

# Clone Ree6 frontend
RUN git clone https://github.com/Ree6-Applications/Webinterface /Webinterface
RUN chown -R node:node /Webinterface

# The USER instruction sets the user name to use as the default user for the remainder of the current stage
USER node:node

WORKDIR /Webinterface/Frontend

# Install the needed modules
RUN npm install vite && npm install && npm i -D @sveltejs/adapter-node --force

# This is the command that will be run inside the image when you tell Docker to start the container
CMD ["sh","/start.sh"]
