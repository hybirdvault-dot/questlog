package com.questlog.app.data.repository

import com.funkatronics.encoders.Base58
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.solana.networking.HttpNetworkDriver
import com.solana.networking.HttpRequest
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Drives ProofOfPlayRepository through a fake HttpNetworkDriver (no real RPC, no MWA).
 * Asserts the confirm/err/never-throw contract. The 30s timeout branch is intentionally
 * NOT covered — see report (wall-clock deadline is not virtual-time friendly).
 */
class ProofOfPlayRepositoryTest {

    private class FakeDriver(private val respond: (String) -> String) : HttpNetworkDriver {
        override suspend fun makeHttpRequest(request: HttpRequest): String {
            val method = JSONObject(request.body.orEmpty()).getString("method")
            return respond(method)
        }
    }

    private class ThrowingDriver : HttpNetworkDriver {
        override suspend fun makeHttpRequest(request: HttpRequest): String =
            throw IOException("network down")
    }

    private val memoProgram = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"

    private fun rpc(result: String) = """{"jsonrpc":"2.0","id":1,"result":$result}"""

    private val blockhashResponse = rpc(
        """{"context":{"slot":1},"value":{"blockhash":"$memoProgram","lastValidBlockHeight":999}}""",
    )

    private fun game() = Game(
        id = "id",
        rawgId = 718135,
        title = "Palworld",
        coverUrl = null,
        releaseYear = 2024,
        platforms = emptyList(),
        rawgRating = null,
        description = null,
        status = GameStatus.COMPLETED,
        personalRating = 4,
        notes = null,
        addedAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
        completedAt = Instant.fromEpochSeconds(1760000000),
        proofTxSignature = null,
        proofAt = null,
        proofStatus = null,
        proofWalletAddress = null,
    )

    private fun ByteArray.containsSequence(needle: ByteArray): Boolean {
        if (needle.isEmpty() || size < needle.size) return false
        outer@ for (i in 0..size - needle.size) {
            for (j in needle.indices) {
                if (this[i + j] != needle[j]) continue@outer
            }
            return true
        }
        return false
    }

    @Test
    fun build_unsigned_transaction_succeeds_and_embeds_memo() = runTest {
        val repo = ProofOfPlayRepository(FakeDriver { blockhashResponse })
        val result = repo.buildUnsignedTransaction(game(), memoProgram)

        assertTrue(result.isSuccess)
        val bytes = result.getOrThrow()
        val text = String(bytes, Charsets.UTF_8)
        assertTrue(text.contains("\"app\":\"questlog\""))
        assertTrue(text.contains("\"title\":\"Palworld\""))
        assertTrue(bytes.containsSequence(Base58.decode(memoProgram)))
    }

    @Test
    fun submit_proof_confirmed_on_finalized() = runTest {
        val repo = ProofOfPlayRepository(
            FakeDriver { method ->
                when (method) {
                    "sendTransaction" -> rpc("\"SIGabc\"")
                    "getSignatureStatuses" -> rpc(
                        """{"context":{"slot":2},"value":[{"confirmationStatus":"finalized","err":null}]}""",
                    )
                    else -> error("unexpected RPC method: $method")
                }
            },
        )
        val result = repo.submitProof(byteArrayOf(1, 2, 3))
        assertTrue(result.isSuccess)
        assertEquals("SIGabc", result.getOrThrow())
    }

    @Test
    fun submit_proof_failed_on_onchain_error() = runTest {
        val repo = ProofOfPlayRepository(
            FakeDriver { method ->
                when (method) {
                    "sendTransaction" -> rpc("\"SIGabc\"")
                    "getSignatureStatuses" -> rpc(
                        """{"context":{"slot":2},"value":[{"confirmationStatus":"processed","err":{"InstructionError":[0,"Custom"]}}]}""",
                    )
                    else -> error("unexpected RPC method: $method")
                }
            },
        )
        assertTrue(repo.submitProof(byteArrayOf(1)).isFailure)
    }

    @Test
    fun submit_proof_never_throws_on_transport_failure() = runTest {
        val repo = ProofOfPlayRepository(ThrowingDriver())
        assertTrue(repo.submitProof(byteArrayOf(1)).isFailure)
    }

    @Test
    fun build_never_throws_when_rpc_fails() = runTest {
        val repo = ProofOfPlayRepository(ThrowingDriver())
        assertTrue(repo.buildUnsignedTransaction(game(), memoProgram).isFailure)
    }

    @Test
    fun rpc_error_field_becomes_failure_not_exception() = runTest {
        val repo = ProofOfPlayRepository(
            FakeDriver { """{"jsonrpc":"2.0","id":1,"error":{"code":-32600,"message":"bad request"}}""" },
        )
        assertTrue(repo.buildUnsignedTransaction(game(), memoProgram).isFailure)
    }
}
