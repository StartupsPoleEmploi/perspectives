package fr.poleemploi.perspectives.authentification.infra.autologin

case class AutologinConfig(secretKey: String,
                           issuer: String,
                           expirationInSeconds: Long)
