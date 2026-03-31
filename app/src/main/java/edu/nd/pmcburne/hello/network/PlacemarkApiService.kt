package edu.nd.pmcburne.hello.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://www.cs.virginia.edu/"

interface PlacemarkApiService {
    @GET("~wxt4gm/placemarks.json")
    suspend fun getPlacemarks(): List<NetworkPlacemark>
}

object PlacemarkApi {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: PlacemarkApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PlacemarkApiService::class.java)
    }
}

