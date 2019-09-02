#!/bin/bash

set -e

# export Docker Swarm secrets to environment variables required by Play application
export MAILJET_APIKEY_PUBLIC=$(cat "${MAILJET_APIKEY_PUBLIC_FILE}")
export MAILJET_APIKEY_PRIVATE=$(cat "${MAILJET_APIKEY_PRIVATE_FILE}")
export POSTGRES_PASSWORD=$(cat "${POSTGRES_PASSWORD_FILE}")
export PLAY_HTTP_SECRET_KEY=$(cat "${PLAY_HTTP_SECRET_KEY_FILE}")
export AUTOLOGIN_SECRET_KEY=$(cat "${AUTOLOGIN_SECRET_KEY_FILE}")

sh -c "./perspectives-batchs-${BATCHS_VERSION}/bin/perspectives-batchs -Dlogger.resource=${BATCHS_LOGBACK_RESOURCE} -Dconfig.resource=${BATCHS_CONFIG_RESOURCE}"
