package com.example.nass.ui.seller

import com.example.nass.data.remote.ApiService
import com.example.nass.data.repository.ProductRepository
import com.example.nass.data.repository.SellerRepository
import com.example.nass.util.MainDispatcherRule
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SellerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun buildViewModel(): SellerViewModel =
        SellerViewModel(SellerRepository(api), ProductRepository(api))

    @Test
    fun init_loadsStats() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"totalAdded":10,"totalSold":4,"available":6}""")
        )

        val vm = buildViewModel()

        val settled = withTimeout(5_000) {
            vm.stats.first { it is Resource.Success || it is Resource.Error }
        }

        assertTrue(settled is Resource.Success)
        val stats = (settled as Resource.Success).data
        assertEquals(10, stats.totalAdded)
        assertEquals(4, stats.totalSold)
        assertEquals(6, stats.available)
    }

    @Test
    fun loadProducts_success_emitsList() = runBlocking {
        // VM init fires loadStats — enqueue a stats response first
        server.enqueue(
            MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"totalAdded":1,"totalSold":0,"available":1}""")
        )
        val vm = buildViewModel()
        withTimeout(5_000) { vm.stats.first { it is Resource.Success } }

        // Now enqueue the products response
        server.enqueue(
            MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """[{"id":1,"seller_id":7,"name":"Test","description":null,
                        "price":99.99,"category":"Clothing","image_url":null,"is_sold":0}]"""
                )
        )

        vm.loadProducts()
        val products = withTimeout(5_000) {
            vm.products.first { it is Resource.Success }
        }

        assertTrue(products is Resource.Success)
        assertEquals(1, (products as Resource.Success).data.size)
        assertEquals("Test", products.data[0].name)
    }

    @Test
    fun createProduct_success_refreshesStatsAndProducts() = runBlocking {
        // ---- init sequence ----
        server.enqueue(MockResponse().setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""{"totalAdded":0,"totalSold":0,"available":0}"""))
        val vm = buildViewModel()
        withTimeout(5_000) { vm.stats.first { it is Resource.Success } }

        // ---- create call ----
        server.enqueue(MockResponse().setResponseCode(201)
            .addHeader("Content-Type", "application/json")
            .setBody("""{"id":99,"seller_id":7,"name":"New","description":null,
                "price":49.99,"category":"Clothing","image_url":null,"is_sold":0}"""))

        // ---- refresh sequence after create ----
        server.enqueue(MockResponse().setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""{"totalAdded":1,"totalSold":0,"available":1}"""))
        server.enqueue(MockResponse().setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""[]"""))

        vm.createProduct(
            com.example.nass.data.model.CreateProductRequest(
                name = "New", description = null, price = 49.99,
                category = "Clothing", imageUrl = null
            )
        )

        val createResult = withTimeout(5_000) {
            vm.createState.first { it is Resource.Success || it is Resource.Error }
        }
        assertTrue(createResult is Resource.Success)

        // Wait for the auto-refresh to settle
        val refreshed = withTimeout(5_000) {
            vm.stats.first {
                it is Resource.Success && (it.data as? com.example.nass.data.model.SellerStats)?.totalAdded == 1
            }
        }
        assertTrue(refreshed is Resource.Success)
    }
}