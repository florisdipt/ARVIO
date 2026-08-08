package com.arflix.tv.data.repository

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeServerSubtitleTest {
    @Test
    fun externalTextTrackUsesJellyfinSubtitleEndpoint() {
        val subtitle = HomeServerSubtitleStream(
            index = 3,
            language = "eng",
            displayTitle = "English",
            title = "",
            isExternal = true,
            isTextSubtitleStream = true,
            isForced = false,
            isHearingImpaired = false
        ).toSubtitle(
            serverUrl = "https://jellyfin.example/media",
            itemId = "item-id",
            mediaSourceId = "source-id",
            accessToken = "test-token",
            provider = "Jellyfin"
        )

        val result = requireNotNull(subtitle)
        val url = result.url.toHttpUrl()
        assertEquals("en", result.lang)
        assertEquals("Jellyfin", result.provider)
        assertEquals(
            "/media/Videos/item-id/source-id/Subtitles/3/Stream.vtt",
            url.encodedPath
        )
        assertEquals("test-token", url.queryParameter("api_key"))
        assertEquals("true", url.queryParameter("copyTimestamps"))
    }

    @Test
    fun bitmapTrackIsNotAddedAsExternalTextSubtitle() {
        val subtitle = HomeServerSubtitleStream(
            index = 2,
            language = "eng",
            displayTitle = "English",
            title = "",
            isExternal = true,
            isTextSubtitleStream = false,
            isForced = false,
            isHearingImpaired = false
        ).toSubtitle(
            serverUrl = "https://jellyfin.example",
            itemId = "item-id",
            mediaSourceId = "source-id",
            accessToken = "test-token",
            provider = "Jellyfin"
        )

        assertNull(subtitle)
    }

    @Test
    fun hearingImpairedTrackGetsSdhLabel() {
        val subtitle = HomeServerSubtitleStream(
            index = 1,
            language = "nld",
            displayTitle = "Dutch",
            title = "",
            isExternal = true,
            isTextSubtitleStream = true,
            isForced = true,
            isHearingImpaired = true
        ).toSubtitle(
            serverUrl = "https://jellyfin.example",
            itemId = "item-id",
            mediaSourceId = "source-id",
            accessToken = "test-token",
            provider = "Jellyfin"
        )

        val result = requireNotNull(subtitle)
        assertEquals("nl", result.lang)
        assertEquals("Dutch SDH", result.label)
        assertTrue(result.isForced)
    }
}
