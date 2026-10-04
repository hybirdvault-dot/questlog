package com.questlog.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.questlog.app.core.model.GameStatus
import com.questlog.app.core.model.ProofStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Room contract tests. Requires a connected device/emulator
 * (run via ./gradlew connectedDebugAndroidTest).
 */
@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var db: QuestlogDatabase
    private lateinit var dao: GameDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, QuestlogDatabase::class.java).build()
        dao = db.gameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: String,
        rawgId: Int?,
        status: GameStatus = GameStatus.WANT,
        proofStatus: ProofStatus? = null,
        platforms: List<String> = listOf("PC"),
        addedAt: Instant = Instant.fromEpochSeconds(1),
    ) = GameEntity(
        id = id,
        rawgId = rawgId,
        title = "T-$id",
        coverUrl = null,
        releaseYear = 2024,
        platforms = platforms,
        rawgRating = null,
        description = null,
        status = status,
        personalRating = 4,
        notes = "notes",
        addedAt = addedAt,
        updatedAt = addedAt,
        completedAt = null,
        proofTxSignature = null,
        proofAt = null,
        proofStatus = proofStatus,
        proofWalletAddress = null,
    )

    @Test
    fun duplicate_rawgId_upserts_instead_of_duplicating() = runTest {
        dao.insertGame(entity("a", 1))
        dao.insertGame(entity("b", 1))
        val all = dao.observeGames().first()
        assertEquals(1, all.size)
        assertEquals("b", all.first().id)
    }

    @Test
    fun manual_entries_with_null_rawgId_coexist() = runTest {
        dao.insertGame(entity("a", null))
        dao.insertGame(entity("b", null))
        assertEquals(2, dao.observeGames().first().size)
    }

    @Test
    fun updateProof_walks_pending_to_confirmed() = runTest {
        dao.insertGame(entity("a", 1, status = GameStatus.COMPLETED, proofStatus = ProofStatus.PENDING))
        dao.updateProof("a", "SIG", Instant.fromEpochSeconds(9), ProofStatus.CONFIRMED, "WALLET")
        val game = dao.observeGame("a").first()!!
        assertEquals(ProofStatus.CONFIRMED, game.proofStatus)
        assertEquals("SIG", game.proofTxSignature)
        assertEquals("WALLET", game.proofWalletAddress)
        assertEquals(Instant.fromEpochSeconds(9), game.proofAt)
    }

    @Test
    fun updateProof_walks_pending_to_failed() = runTest {
        dao.insertGame(entity("a", 1, status = GameStatus.COMPLETED, proofStatus = ProofStatus.PENDING))
        dao.updateProof("a", null, null, ProofStatus.FAILED, null)
        assertEquals(ProofStatus.FAILED, dao.observeGame("a").first()!!.proofStatus)
    }

    @Test
    fun observeVerifiedCompletions_returns_only_completed_and_confirmed() = runTest {
        dao.insertGame(entity("v", 1, status = GameStatus.COMPLETED, proofStatus = ProofStatus.CONFIRMED))
        dao.insertGame(entity("p", 2, status = GameStatus.COMPLETED, proofStatus = ProofStatus.PENDING))
        dao.insertGame(entity("n", 3, status = GameStatus.PLAYING, proofStatus = ProofStatus.CONFIRMED))
        val verified = dao.observeVerifiedCompletions().first()
        assertEquals(listOf("v"), verified.map { it.id })
    }

    @Test
    fun converters_round_trip_platform_list_and_instant() = runTest {
        val added = Instant.fromEpochMilliseconds(123456789L)
        dao.insertGame(entity("a", 1, platforms = listOf("PC", "Nintendo Switch"), addedAt = added))
        val game = dao.observeGame("a").first()!!
        assertEquals(listOf("PC", "Nintendo Switch"), game.platforms)
        assertEquals(added, game.addedAt)
    }
}
