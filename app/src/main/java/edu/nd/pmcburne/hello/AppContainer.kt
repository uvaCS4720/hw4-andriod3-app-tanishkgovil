package edu.nd.pmcburne.hello

import android.content.Context
import edu.nd.pmcburne.hello.data.AppDatabase
import edu.nd.pmcburne.hello.data.PlacemarkRepository
import edu.nd.pmcburne.hello.network.PlacemarkApi

object AppContainer {
    @Volatile
    private var repository: PlacemarkRepository? = null

    fun getRepository(context: Context): PlacemarkRepository {
        return repository ?: synchronized(this) {
            repository ?: PlacemarkRepository(
                apiService = PlacemarkApi.service,
                dao = AppDatabase.getInstance(context).placemarkDao()
            ).also { created ->
                repository = created
            }
        }
    }
}

