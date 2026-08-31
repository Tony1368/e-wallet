# Base image
FROM node:25.9.0-alpine

# Make folder to put our files in
RUN mkdir -p /usr/src/app
RUN mkdir -p /usr/src/app/frontend

# Set working directory so that all
# subsequent command runs in this folder
WORKDIR /usr/src/app/frontend

# Copy package json and install dependencies
COPY package*.json ./
RUN npm install

# Copy our app
COPY . .

# Set environment variable to disable ESLint warnings as errors
ENV ESLINT_NO_DEV_ERRORS=true
ENV DISABLE_ESLINT_PLUGIN=true

# Expose port to access server
EXPOSE 3000

# Command to run our app
CMD [ "npm", "start" ]
