package de.tebbeubben.remora.lib.model.configuration

import dagger.Reusable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

@Reusable
internal class GoogleServicesJson @Inject constructor() {
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
        val projectInfo = json["project_info"]?.jsonObject
            ?: throw IllegalArgumentException("Missing project_info")
        val client = json["client"]?.jsonArray?.getOrNull(0)?.jsonObject
            ?: throw IllegalArgumentException("Missing client entry")

        return Data(
            projectId = projectInfo["project_id"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing project_id"),
            apiKey = client["api_key"]?.jsonArray?.firstOrNull()?.jsonObject?.get("current_key")?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing current_key"),
            applicationId = client["client_info"]?.jsonObject?.get("mobilesdk_app_id")?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing mobilesdk_app_id"),
            gcmSenderId = projectInfo["project_number"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Missing project_number"),
        )
    }

    data class Data(
        val projectId: String,
        val apiKey: String,
        val applicationId: String,
        val gcmSenderId: String
    )
}