package edu.nd.pmcburne.hello.network

import com.squareup.moshi.Json

data class NetworkPlacemark(
    val id: Int,
    val name: String,
    @param:Json(name = "tag_list") val tagList: List<String>,
    val description: String,
    @param:Json(name = "visual_center") val visualCenter: NetworkVisualCenter
)

data class NetworkVisualCenter(
    val latitude: Double,
    val longitude: Double
)


