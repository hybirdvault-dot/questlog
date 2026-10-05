package com.questlog.app.data.repository

import android.util.Base64
import com.funkatronics.encoders.Base58
import com.questlog.app.core.model.Game
import com.solana.networking.HttpNetworkDriver
import com.solana.networking.HttpRequest
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.Message
import com.solana.transaction.Transaction
import com.solana.transaction.TransactionInstruction
import javax.inject.Inject
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

class ProofOfPlayRepository @Inject constructor(
    private val httpDriver: HttpNetworkDriver,
) {

    suspend fun buildUnsignedTransaction(game: Game, walletAddress: String): Result<ByteArray> =
        runCatching {
            val blockhash = latestBlockhash()
            val payer = SolanaPublicKey(Base58.decode(walletAddress))
            val instruction = TransactionInstruction(
                SolanaPublicKey(Base58.decode(MEMO_PROGRAM_ID)),
                listOf(AccountMeta(payer, true, true)),
                memoPayload(game).encodeToByteArray(),
            )
            val message = Message.Builder(
                mutableListOf(instruction),
                SolanaPublicKey(Base58.decode(blockhash)),
            ).build()
            Transaction(message).serialize()
        }

    suspend fun submitProof(signedTransaction: ByteArray): Result<String> = runCatching {
        val signature = sendTransaction(signedTransaction)
        awaitConfirmation(signature)
        signature
    }

    private suspend fun latestBlockhash(): String {
        val params = JSONArray().put(JSONObject().put("commitment", "finalized"))
        return rpcObject("getLatestBlockhash", params)
            .getJSONObject("value")
            .getString("blockhash")
    }

    private suspend fun sendTransaction(signedTransaction: ByteArray): String {
        val encoded = Base64.encodeToString(signedTransaction, Base64.NO_WRAP)
        val params = JSONArray()
            .put(encoded)
            .put(
                JSONObject()
                    .put("encoding", "base64")
                    .put("preflightCommitment", "finalized"),
            )
        return rpcResult("sendTransaction", params)
    }

    private suspend fun awaitConfirmation(signature: String) {
        val params = JSONArray().put(JSONArray().put(signature))
        val deadline = System.nanoTime() + CONFIRMATION_TIMEOUT_MS * 1_000_000L
        while (System.nanoTime() < deadline) {
            val status = rpcObject("getSignatureStatuses", params)
                .optJSONArray("value")
                ?.optJSONObject(0)
            val error = status?.opt("err")
            if (error != null && error != JSONObject.NULL) {
                error("Proof transaction failed on-chain")
            }
            if (status?.optString("confirmationStatus") == "finalized") {
                return
            }
            delay(POLL_INTERVAL_MS)
        }
        error("Timed out waiting for proof confirmation")
    }

    private suspend fun rpcObject(method: String, params: JSONArray): JSONObject =
        JSONObject(rpcResult(method, params))

    private suspend fun rpcResult(method: String, params: JSONArray): String {
        val payload = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", REQUEST_ID)
            .put("method", method)
            .put("params", params)
            .toString()
        val request = object : HttpRequest {
            override val url: String = DEVNET_RPC_URL
            override val method: String = "POST"
            override val properties: Map<String, String> =
                mapOf("Content-Type" to "application/json")
            override val body: String = payload
        }
        val response = JSONObject(httpDriver.makeHttpRequest(request))
        response.optJSONObject("error")?.let {
            error("RPC error: ${it.optString("message")}")
        }
        return response.opt("result")?.toString() ?: error("Empty RPC result for $method")
    }

    private fun memoPayload(game: Game): String = JSONObject()
        .put("v", 1)
        .put("app", "questlog")
        .put("title", game.title)
        .apply { game.rawgId?.let { put("rawgId", it) } }
        .put("completedAt", game.completedAt?.epochSeconds ?: 0L)
        .apply { game.personalRating?.let { put("rating", it) } }
        .toString()

    private companion object {
        const val DEVNET_RPC_URL = "https://api.devnet.solana.com"
        const val MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"
        const val REQUEST_ID = 1
        const val CONFIRMATION_TIMEOUT_MS = 30_000L
        const val POLL_INTERVAL_MS = 1_000L
    }
}
