package com.example.android_lab_3
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("/news")
    fun getNews(
        @Query("apikey") apiKey: String,
        @Query("q") keyword: String,
        @Query("page") page: Int
    ): Call<NewsResponse>
}