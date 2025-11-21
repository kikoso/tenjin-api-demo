package com.tenjin.sdk

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface TenjinApi {
    @GET("v0/event")
    suspend fun sendEvent(@QueryMap params: Map<String, String>): Response<Unit>
}
