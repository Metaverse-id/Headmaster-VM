package com.example.data

import com.example.model.RomManifest
import org.json.JSONArray
import org.json.JSONObject

object RomManifestParser {

    sealed class ValidationResult {
        data class Success(val manifest: RomManifest) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    /**
     * Parse manifest JSON string. Supports cleaning trailing comments (#...) and lenient parsing.
     */
    fun parseJson(rawJson: String): ValidationResult {
        return try {
            // Remove full line or inline comments starting with '#'
            val cleaned = rawJson.lines()
                .map { line ->
                    val commentIdx = line.indexOf('#')
                    if (commentIdx != -1) line.substring(0, commentIdx) else line
                }
                .joinToString("\n")
                .trim()

            val json = JSONObject(cleaned)

            if (!json.has("id")) return ValidationResult.Error("Missing required key: 'id'")
            if (!json.has("display_name")) return ValidationResult.Error("Missing required key: 'display_name'")
            if (!json.has("rom_version")) return ValidationResult.Error("Missing required key: 'rom_version'")
            if (!json.has("os_version")) return ValidationResult.Error("Missing required key: 'os_version'")
            if (!json.has("support_a32")) return ValidationResult.Error("Missing required key: 'support_a32'")
            if (!json.has("support_a64")) return ValidationResult.Error("Missing required key: 'support_a64'")
            if (!json.has("minimum_sdk_int")) return ValidationResult.Error("Missing required key: 'minimum_sdk_int'")
            if (!json.has("rom_uri")) return ValidationResult.Error("Missing required key: 'rom_uri'")

            val id = json.getString("id")
            val displayName = json.getString("display_name").trim()
            val romVersion = json.optInt("rom_version", 1)
            val osVersion = json.getString("os_version").trim()
            val supportA32 = json.optBoolean("support_a32", false)
            val supportA64 = json.optBoolean("support_a64", true)
            val minSdk = json.optInt("minimum_sdk_int", 28)

            // rom_uri can be a JSONArray or a single String
            val romUris = mutableListOf<String>()
            val romUriObj = json.get("rom_uri")
            if (romUriObj is JSONArray) {
                for (i in 0 until romUriObj.length()) {
                    romUris.add(romUriObj.getString(i))
                }
            } else if (romUriObj is String) {
                romUris.add(romUriObj)
            }

            if (romUris.isEmpty()) {
                return ValidationResult.Error("'rom_uri' must contain at least one ROM image path (.img)")
            }

            val magiskUri = if (json.has("magisk_uri")) json.optString("magisk_uri") else null
            val suUri = if (json.has("su_uri")) json.optString("su_uri") else null
            val xposedUri = if (json.has("xposed_uri")) json.optString("xposed_uri") else null
            val playUri = if (json.has("play_uri")) json.optString("play_uri") else null

            val manifest = RomManifest(
                id = id,
                displayName = displayName,
                romVersion = romVersion,
                osVersion = osVersion,
                supportA32 = supportA32,
                supportA64 = supportA64,
                minimumSdkInt = minSdk,
                romUri = romUris,
                magiskUri = magiskUri,
                suUri = suUri,
                xposedUri = xposedUri,
                playUri = playUri
            )

            ValidationResult.Success(manifest)
        } catch (e: Exception) {
            ValidationResult.Error("Invalid JSON syntax in manifest.json: ${e.localizedMessage}")
        }
    }

    /**
     * Converts a RomManifest to formatted JSON string.
     */
    fun toJson(manifest: RomManifest, pretty: Boolean = true): String {
        val json = JSONObject()
        json.put("id", manifest.id)
        json.put("display_name", manifest.displayName)
        json.put("rom_version", manifest.romVersion)
        json.put("os_version", manifest.osVersion)
        json.put("support_a32", manifest.supportA32)
        json.put("support_a64", manifest.supportA64)
        json.put("minimum_sdk_int", manifest.minimumSdkInt)

        val uriArray = JSONArray()
        manifest.romUri.forEach { uriArray.put(it) }
        json.put("rom_uri", uriArray)

        manifest.magiskUri?.let { json.put("magisk_uri", it) }
        manifest.suUri?.let { json.put("su_uri", it) }
        manifest.xposedUri?.let { json.put("xposed_uri", it) }
        manifest.playUri?.let { json.put("play_uri", it) }

        return if (pretty) json.toString(4) else json.toString()
    }
}
