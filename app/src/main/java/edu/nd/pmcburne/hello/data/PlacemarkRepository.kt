package edu.nd.pmcburne.hello.data

import edu.nd.pmcburne.hello.network.PlacemarkApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlacemarkRepository(
    private val apiService: PlacemarkApiService,
    private val dao: PlacemarkDao
) {
    fun observeAllTags(): Flow<List<String>> = dao.observeAllTags()

    fun observePlacemarksForTag(tag: String): Flow<List<CampusLocation>> {
        return dao.observePlacemarksForTag(tag).map { entities ->
            entities.map { it.toCampusLocation() }
        }
    }

    suspend fun synchronizeFromApi() {
        val placemarks = apiService.getPlacemarks()

        val placemarkEntities = placemarks.map { placemark ->
            PlacemarkEntity(
                id = placemark.id,
                name = placemark.name,
                description = placemark.description,
                latitude = placemark.visualCenter.latitude,
                longitude = placemark.visualCenter.longitude
            )
        }

        val tagEntities = placemarks.flatMap { placemark ->
            placemark.tagList.distinct().map { tag ->
                PlacemarkTagEntity(placemarkId = placemark.id, tag = tag)
            }
        }

        dao.upsertPlacemarksAndTags(placemarkEntities, tagEntities)
    }
}

