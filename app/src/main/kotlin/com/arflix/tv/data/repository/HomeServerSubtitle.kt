package com.arflix.tv.data.repository

import com.arflix.tv.data.model.Subtitle
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.Locale

internal data class HomeServerSubtitleStream(
    val index: Int,
    val language: String,
    val displayTitle: String,
    val title: String,
    val isExternal: Boolean,
    val isTextSubtitleStream: Boolean,
    val isForced: Boolean,
    val isHearingImpaired: Boolean
)

internal fun HomeServerSubtitleStream.toSubtitle(
    serverUrl: String,
    itemId: String,
    mediaSourceId: String,
    accessToken: String,
    provider: String
): Subtitle? {
    if (!isExternal || !isTextSubtitleStream || index < 0) return null
    if (itemId.isBlank() || mediaSourceId.isBlank() || accessToken.isBlank()) return null

    val baseUrl = serverUrl.toHttpUrlOrNull() ?: return null
    val url = baseUrl.newBuilder()
        .addPathSegment("Videos")
        .addPathSegment(itemId)
        .addPathSegment(mediaSourceId)
        .addPathSegment("Subtitles")
        .addPathSegment(index.toString())
        .addPathSegment("Stream.vtt")
        .addQueryParameter("api_key", accessToken)
        .addQueryParameter("copyTimestamps", "true")
        .build()
        .toString()

    val language = normalizeHomeServerLanguage(language)
    val rawLabel = displayTitle.ifBlank { title }.ifBlank { language }
    val label = if (
        isHearingImpaired &&
        !rawLabel.contains("sdh", ignoreCase = true) &&
        !rawLabel.contains("cc", ignoreCase = true) &&
        !rawLabel.contains("hearing", ignoreCase = true)
    ) {
        "$rawLabel SDH"
    } else {
        rawLabel
    }

    return Subtitle(
        id = "jellyfin:$mediaSourceId:$index",
        url = url,
        lang = language,
        label = label,
        provider = provider,
        isForced = isForced
    )
}

private fun normalizeHomeServerLanguage(value: String): String {
    val language = value.trim().lowercase(Locale.US).substringBefore('-').substringBefore('_')
    return when (language) {
        "eng", "english" -> "en"
        "spa", "spanish" -> "es"
        "fra", "fre", "french" -> "fr"
        "deu", "ger", "german" -> "de"
        "ita", "italian" -> "it"
        "por", "portuguese" -> "pt"
        "nld", "dut", "dutch" -> "nl"
        "rus", "russian" -> "ru"
        "zho", "chi", "chinese" -> "zh"
        "jpn", "japanese" -> "ja"
        "kor", "korean" -> "ko"
        "ara", "arabic" -> "ar"
        "hin", "hindi" -> "hi"
        "tur", "turkish" -> "tr"
        "pol", "polish" -> "pl"
        "swe", "swedish" -> "sv"
        "nor", "nob", "nno", "norwegian" -> "no"
        "dan", "danish" -> "da"
        "fin", "finnish" -> "fi"
        "ell", "gre", "greek" -> "el"
        "ces", "cze", "czech" -> "cs"
        "hun", "hungarian" -> "hu"
        "ron", "rum", "romanian" -> "ro"
        "" -> "und"
        else -> language
    }
}
