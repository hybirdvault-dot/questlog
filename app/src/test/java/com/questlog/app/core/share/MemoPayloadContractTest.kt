package com.questlog.app.core.share

import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.questlog.app.data.repository.ProofOfPlayRepository
import com.solana.networking.HttpNetworkDriver
import com.solana.networking.HttpRequest
import kotlinx.datetime.Instant
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Asserts the CANONICAL Proof-of-Play v1 memo payload contract (Build Bible Part 14).
 * The payload builder is private, so it is exercised via reflection to avoid changing
 * production code.
 */
class MemoPayloadContractTest {

    private class NoopDriver : HttpNetworkDriver {
        override suspend fun makeHttpRequest(request: HttpRequest): String =
            error("HTTP must not be called for a pure memo-payload test")
    }

    private val repository = ProofOfPlayRepository(NoopDriver())

    private fun memoJson(game: Game): JSONObject {
        val method = ProofOfPlayRepository::class.java
            .getDeclaredMethod("memoPayload", Game::class.java)
            .apply { isAccessible = true }
        return JSONObject(method.invoke(repository, game) as String)
    }

    private fun game(
        title: String = "Palworld",
        rawgId: Int? = 718135,
        completedAt: Instant? = Instant.fromEpochSeconds(1760000000),
        personalRating: Int? = 4,
    ) = Game(
        id = "id",
        rawgId = rawgId,
        title = title,
        coverUrl = null,
        releaseYear = 2024,
        platforms = emptyList(),
        rawgRating = null,
        description = null,
        status = GameStatus.COMPLETED,
        personalRating = personalRating,
        notes = null,
        addedAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
        completedAt = completedAt,
        proofTxSignature = null,
        proofAt = null,
        proofStatus = null,
        proofWalletAddress = null,
    )

    @Test
    fun canonical_payload_has_exactly_the_contract_fields() {
        val json = memoJson(game())
        assertEquals(1, json.getInt("v"))
        assertEquals("questlog", json.getString("app"))
        assertEquals("Palworld", json.getString("title"))
        assertEquals(718135, json.getInt("rawgId"))
        assertEquals(1760000000L, json.getLong("completedAt"))
        assertEquals(4, json.getInt("rating"))
        assertEquals(
            setOf("v", "app", "title", "rawgId", "completedAt", "rating"),
            json.keys().asSequence().toSet(),
        )
    }

    @Test
    fun null_rating_still_serializes_without_extra_fields() {
        val json = memoJson(game(personalRating = null))
        assertEquals("Palworld", json.getString("title"))
        assertFalse(json.has("rating"))
        assertTrue(
            json.keys().asSequence().all {
                it in setOf("v", "app", "title", "rawgId", "completedAt")
            },
        )
    }

    @Test
    fun manual_entry_without_rawgId_serializes() {
        val json = memoJson(game(rawgId = null))
        assertFalse(json.has("rawgId"))
        assertEquals("questlog", json.getString("app"))
    }

    @Test
    fun utf8_round_trip_is_lossless() {
        val payload = memoJson(game(title = "Pokémon — Édition")).toString()
        assertEquals(payload, String(payload.encodeToByteArray(), Charsets.UTF_8))
    }

    @Test
    fun completed_at_is_unix_epoch_seconds() {
        val json = memoJson(game(completedAt = Instant.fromEpochSeconds(1700000000)))
        assertEquals(1700000000L, json.getLong("completedAt"))
    }
}
