package com.example

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testApiCallWithApplication() = kotlinx.coroutines.runBlocking {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    com.example.data.remote.ApiClient.getOkHttpClient(context)
    try {
      val items = com.example.data.remote.ApiClient.apiService.getGiveaways(platform = null)
      println("API SUCCESS WITH APPLICATION: received ${items.size} items")
      assertTrue(items.isNotEmpty())
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
  }
}
