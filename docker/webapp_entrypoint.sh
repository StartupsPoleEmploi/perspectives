#!/bin/bash

set -e

# export Docker Swarm secrets to environment variables required by Play application
export ALGOLIA_PLACES_APIKEY=$(cat "${ALGOLIA_PLACES_APIKEY_FILE}")
export ALGOLIA_PLACES_APPID=$(cat "${ALGOLIA_PLACES_APPID_FILE}")
export OAUTH2_CLIENT_ID=$(cat "${OAUTH2_CLIENT_ID_FILE}")
export OAUTH2_CLIENT_SECRET=$(cat "${OAUTH2_CLIENT_SECRET_FILE}")
export MAILJET_APIKEY_PUBLIC=$(cat "${MAILJET_APIKEY_PUBLIC_FILE}")
export MAILJET_APIKEY_PRIVATE=$(cat "${MAILJET_APIKEY_PRIVATE_FILE}")
export AUTOLOGIN_SECRET_KEY=$(cat "${AUTOLOGIN_SECRET_KEY_FILE}")
export POSTGRES_PASSWORD=$(cat "${POSTGRES_PASSWORD_FILE}")
export PLAY_HTTP_SECRET_KEY=$(cat "${PLAY_HTTP_SECRET_KEY_FILE}")
export GOOGLE_TAG_MANAGER_CONTAINER_ID=$(cat "${GOOGLE_TAG_MANAGER_CONTAINER_ID_FILE}")
export SLACK_WEBHOOK_URL=$(cat "${SLACK_WEBHOOK_URL_FILE}")
export ADMIN_API_KEY=$(cat "${ADMIN_API_KEY_FILE}")

sh -c "./perspectives-webapp/bin/perspectives-webapp -Dlogger.resource=${WEBAPP_LOGBACK_RESOURCE} -Dconfig.resource=${WEBAPP_CONFIG_RESOURCE}"
