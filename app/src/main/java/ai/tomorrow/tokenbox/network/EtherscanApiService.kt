package ai.tomorrow.tokenbox.network

import ai.tomorrow.tokenbox.data.BalanceResponse
import ai.tomorrow.tokenbox.data.ResultResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api-ropsten.etherscan.io/"

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object pointing to the desired URL
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface EtherscanApiService {

    @GET("api")
    fun getHistory(
        @Query("module") module: String,
        @Query("action") action: String,
        @Query("address") address: String,
        @Query("startblock") startblock: Long,
        @Query("endblock") endblock: Long,
        @Query("sort") sort: String,
        @Query("apikey") apikey: String
    ):
        Deferred<ResultResponse>

    @GET("api")
    fun getBalance(
        @Query("module") module: String,
        @Query("action") action: String,
        @Query("address") address: String,
        @Query("tag") tag: String,
        @Query("apikey") apikey: String
    ):
        Deferred<BalanceResponse>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object EtherscanApi {
    val retrofitService: EtherscanApiService by lazy { retrofit.create(EtherscanApiService::class.java) }
}