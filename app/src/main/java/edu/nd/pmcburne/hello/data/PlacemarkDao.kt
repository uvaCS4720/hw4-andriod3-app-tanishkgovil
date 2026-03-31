package edu.nd.pmcburne.hello.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacemarkDao {
    @Upsert
    suspend fun upsertPlacemarks(placemarks: List<PlacemarkEntity>)

    @Query("DELETE FROM placemark_tags WHERE placemarkId IN (:placemarkIds)")
    suspend fun deleteTagsForPlacemarks(placemarkIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<PlacemarkTagEntity>)

    @Query("SELECT DISTINCT tag FROM placemark_tags ORDER BY tag ASC")
    fun observeAllTags(): Flow<List<String>>

    @Query(
        """
        SELECT p.*
        FROM placemarks p
        INNER JOIN placemark_tags t ON t.placemarkId = p.id
        WHERE t.tag = :tag
        ORDER BY p.name ASC
        """
    )
    fun observePlacemarksForTag(tag: String): Flow<List<PlacemarkEntity>>

    @Transaction
    suspend fun upsertPlacemarksAndTags(
        placemarks: List<PlacemarkEntity>,
        tags: List<PlacemarkTagEntity>
    ) {
        upsertPlacemarks(placemarks)
        if (placemarks.isNotEmpty()) {
            deleteTagsForPlacemarks(placemarks.map { it.id })
        }
        if (tags.isNotEmpty()) {
            insertTags(tags)
        }
    }
}

