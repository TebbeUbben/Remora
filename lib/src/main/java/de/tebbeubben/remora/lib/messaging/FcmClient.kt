package de.tebbeubben.remora.lib.messaging

import de.tebbeubben.remora.lib.configuration.NetworkConfigurationStorage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.coroutines.executeAsync
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
internal class FcmClient @Inject constructor(
    private val configStorage: NetworkConfigurationStorage
) {

    private val lock = emptyArray<Any>()
    private var accessToken: AccessToken? = null
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                if (!request.url.toString().startsWith(FCM_BASE_URL)) return chain.proceed(request)
                var currentAccessToken = synchronized(lock) {
                    accessToken.takeIf { it != null && it.projectId == configStorage.config?.projectId && it.expiresAt > System.currentTimeMillis() + 60_000 }
                        ?: refreshAccessToken()
                }
                return chain.proceed(
                    request.newBuilder()
                        .header("Authorization", "Bearer ${currentAccessToken.accessToken}")
                        .build()
                )
            }

        })
        .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
        .build()

    private fun refreshAccessToken(): AccessToken {
        val config = configStorage.config!!
        val request = Request.Builder()
            .url(config.tokenUri)
            .post(
                FormBody.Builder()
                    .add("grant_type", GRANT_TYPE)
                    .add("assertion", createJwt())
                    .build()
            )
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.body.string()}")
        }
        val accessTokenResponse = json.decodeFromString<AccessTokenResponse>(response.body.string())
        val accessToken = AccessToken(
            projectId = config.projectId,
            accessToken = accessTokenResponse.accessToken,
            expiresAt = System.currentTimeMillis() + accessTokenResponse.expiresIn * 1000
        )
        this.accessToken = accessToken
        return accessToken
    }

    suspend fun sendFCM(topic: String, data: Map<String, String>, collapseKey: String? = null, ttl: Long? = null): String {
        val requestBody = FcmSendRequest(
            message = FcmMessage(
                topic = topic,
                data = data,
                android = FcmAndroidConfig(
                    priority = "high",
                    ttl = if (ttl != null) (ttl.toDouble() / 1000.0).toString() + "s" else null,
                    collapseKey = collapseKey
                )
            )
        )
        val request = Request.Builder()
            .url(FCM_SEND_URL.replace("{project_id}", configStorage.config!!.projectId))
            .post(
                json.encodeToString(requestBody)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()
        val response = client.newCall(request).executeAsync()
        if (response.code == 401) {
            synchronized(lock) { accessToken = null }
        }
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.body.string()}")
        }
        return json.decodeFromString<FcmSendResponse>(response.body.string()).messageId
    }

    private fun createJwt(): String {
        val config = configStorage.config!!
        val base64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        val header = JwtHeader("RS256", "JWT", config.privateKeyId)
        val timestamp = System.currentTimeMillis() / 1000
        val payload =
            JwtPayload(config.clientEmail, timestamp, timestamp + 3600, SCOPE, config.tokenUri)
        val headerBase64 = base64.encode(json.encodeToString(header).encodeToByteArray())
        val payloadBase64 = base64.encode(json.encodeToString(payload).encodeToByteArray())
        val signatureProvider = Signature.getInstance("SHA256withRSA")
        signatureProvider.initSign(config.privateKey)
        signatureProvider.update("$headerBase64.$payloadBase64".encodeToByteArray())
        val signatureBase64 = base64.encode(signatureProvider.sign())
        return "$headerBase64.$payloadBase64.$signatureBase64"
    }

    @Serializable
    private data class FcmSendRequest(
        @SerialName("message") val message: FcmMessage
    )

    @Serializable
    private data class FcmSendResponse(
        @SerialName("name") val messageId: String
    )

    @Serializable
    private data class FcmAndroidConfig(
        @SerialName("priority") val priority: String,
        @SerialName("ttl") val ttl: String?,
        @SerialName("collapse_key") val collapseKey: String?,
    )

    @Serializable
    private data class FcmMessage(
        @SerialName("topic") val topic: String,
        @SerialName("data") val data: Map<String, String>,
        @SerialName("android") val android: FcmAndroidConfig
    )

    @Serializable
    private data class JwtHeader(
        @SerialName("alg") val algorithm: String,
        @SerialName("typ") val type: String,
        @SerialName("kid") val keyId: String
    )

    @Serializable
    private data class JwtPayload(
        @SerialName("iss") val issuer: String,
        @SerialName("iat") val issuedAt: Long,
        @SerialName("exp") val expiration: Long,
        @SerialName("scope") val scope: String,
        @SerialName("aud") val audience: String
    )

    @Serializable
    private data class AccessTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Int
    )

    private data class AccessToken(
        val projectId: String,
        val accessToken: String,
        val expiresAt: Long
    )

    companion object {
        private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private const val FCM_BASE_URL = "https://fcm.googleapis.com"
        private const val FCM_SEND_URL = "$FCM_BASE_URL/v1/projects/{project_id}/messages:send"
    }
}