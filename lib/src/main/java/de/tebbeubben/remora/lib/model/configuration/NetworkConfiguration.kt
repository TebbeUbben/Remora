package de.tebbeubben.remora.lib.model.configuration

import java.security.PrivateKey

data class NetworkConfiguration(
    val projectId: String,
    val privateKeyId: String,
    val privateKey: PrivateKey,
    val tokenUri: String,
    val clientEmail: String,
    val apiKey: String,
    val applicationId: String,
    val gcmSenderId: String
)