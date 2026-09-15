package com.example.nass.data.repository

import com.example.nass.data.remote.ApiService
import com.example.nass.util.Resource
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SellerRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: SellerRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repo = SellerRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getStats_parsesResponse() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"totalAdded":5,"totalSold":2,"available":3}""")
        )

        val result = repo.getStats()

        assertTrue(result is Resource.Success)
        val stats = (result as Resource.Success).data
        assertEquals(5, stats.totalAdded)
        assertEquals(2, stats.totalSold)
        assertEquals(3, stats.available)
    }

    @Test
    fun getStats_serverError_returnsError() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repo.getStats()

        assertTrue(result is Resource.Error)
        assertEquals(500, (result as Resource.Error).code)
    }

    @Test
    fun getMyProducts_parsesList() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    [
                      {
                        "id":1,"seller_id":7,"name":"Denim Jacket","description":"Vintage",
                        "price":349.99,"category":"Clothing",
                        "image_url":"https://example.com/j.jpg","is_sold":0
                      },
                      {
                        "id":2,"seller_id":7,"name":"Winter Coat","description":null,
                        "price":899.00,"category":"Clothing",
                        "image_url":null,"is_sold":1
                      }
                    ]
                    """.trimIndent()
                )
        )

        val result = repo.getMyProducts()

        assertTrue(result is Resource.Success)
        val products = (result as Resource.Success).data
        assertEquals(2, products.size)
        assertEquals("Denim Jacket", products[0].name)
        assertEquals(349.99, products[0].price, 0.001)
        assertEquals(1, products[1].isSold)
    }

    @Test
    fun getMyProducts_emptyList() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]")
        )

        val result = repo.getMyProducts()

        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data.isEmpty())
    }
}