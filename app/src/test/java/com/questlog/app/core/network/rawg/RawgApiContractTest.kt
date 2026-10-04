package com.questlog.app.core.network.rawg

import com.google.gson.GsonBuilder
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Contract tests for the RAWG wire format (search has no description; detail has
 * description_raw; released "YYYY-MM-DD" -> Int? year; platforms nested -> List<String>).
 */
class RawgApiContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: RawgApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(RawgApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(body: String) {
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))
    }

    @Test
    fun search_results_parse_and_carry_no_description() = runTest {
        enqueue(
            """
            {"results":[
              {"id":718135,"name":"Palworld","released":"2024-01-19",
               "background_image":"https://img/x.jpg","rating":4.5,
               "platforms":[{"platform":{"id":4,"name":"PC"}}]}
            ]}
            """.trimIndent(),
        )
        val response = api.searchGames("palworld", 20, "testkey")
        assertEquals(1, response.results.size)
        val preview = response.results.first().toPreview()
        assertEquals("Palworld", preview.title)
        assertEquals(2024, preview.releaseYear)
        assertEquals(listOf("PC"), preview.platforms)
        assertEquals(4.5, preview.rawgRating!!, 0.0001)
        assertNull("search response carries no description", preview.description)
    }

    @Test
    fun detail_maps_description_raw_to_domain_description() = runTest {
        enqueue(
            """
            {"id":718135,"name":"Palworld","released":"2024-01-19",
             "description_raw":"A creature-collecting survival game.",
             "platforms":[{"platform":{"id":4,"name":"PC"}}]}
            """.trimIndent(),
        )
        val game = api.getGameDetail(718135, "testkey").toDomain()
        assertEquals("A creature-collecting survival game.", game.description)
    }

    @Test
    fun released_yyyy_mm_dd_maps_to_year() = runTest {
        enqueue("""{"id":1,"name":"Fallout","released":"1997-09-29"}""")
        assertEquals(1997, api.getGameDetail(1, "k").toDomain().releaseYear)
    }

    @Test
    fun missing_or_malformed_released_maps_to_null_without_crashing() = runTest {
        enqueue("""{"id":1,"name":"Unknown"}""")
        assertNull(api.getGameDetail(1, "k").toDomain().releaseYear)

        enqueue("""{"id":2,"name":"Soon","released":"TBA"}""")
        assertNull(api.getGameDetail(2, "k").toDomain().releaseYear)
    }

    @Test
    fun nested_platforms_map_to_name_list() = runTest {
        enqueue(
            """
            {"id":1,"name":"X","platforms":[
              {"platform":{"id":4,"name":"PC"}},
              {"platform":{"id":187,"name":"PlayStation 5"}}
            ]}
            """.trimIndent(),
        )
        assertEquals(
            listOf("PC", "PlayStation 5"),
            api.getGameDetail(1, "k").toDomain().platforms,
        )
    }

    @Test
    fun empty_results_list_parses() = runTest {
        enqueue("""{"results":[]}""")
        assertEquals(0, api.searchGames("nothing", 20, "k").results.size)
    }
}
