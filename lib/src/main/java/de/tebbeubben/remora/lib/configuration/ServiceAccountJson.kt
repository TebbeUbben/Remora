package de.tebbeubben.remora.lib.configuration

import dagger.Reusable
import de.tebbeubben.remora.lib.util.Crypto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.security.InvalidKeyException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import javax.inject.Inject
import kotlin.io.encoding.Base64

@Reusable
internal class ServiceAccountJson @Inject constructor(
    private val crypto: Crypto
) {
    /**
     * @throws IllegalArgumentException in case the provided JSON is not a valid google-services.json configuration file.
     */
    fun parseFromJson(rawJson: String): Data {
        val json = try {
            Json.decodeFromString<JsonObject>(rawJson)
        } catch (e: SerializationException) {
            throw IllegalArgumentException(
                "The provided google-services.json file is not valid JSON.",
                e
            )
        }

        if (json["type"]?.jsonPrimitive?.contentOrNull != "service_account") {
            throw IllegalArgumentException("Credentials file has wrong type.")
        }

        val privateKeyBase64 = json["private_key"]?.jsonPrimitive?.contentOrNull
            ?: throw IllegalArgumentException("Missing private_key")

        val privateKey = try {
             crypto.decodeRSAPrivateKey(
                Base64.Pem.decode(
                    privateKeyBase64
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                )
            )
        } catch (e: InvalidKeySpecException) {
            throw IllegalArgumentException("private_key is not valid", e)
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException("private_key is not valid", e)
        }
        return Data(
            projectId = json["project_id"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing project_id"),
            privateKeyId = json["private_key_id"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing private_key_id"),
            privateKey = privateKey,
            tokenUri = json["token_uri"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing token_uri"),
            clientEmail = json["client_email"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing client_email")
        )
    }

    data class Data(
        val projectId: String,
        val privateKeyId: String,
        val privateKey: PrivateKey,
        val tokenUri: String,
        val clientEmail: String
    )
}