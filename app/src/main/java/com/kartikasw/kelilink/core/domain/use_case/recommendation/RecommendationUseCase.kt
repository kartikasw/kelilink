package com.kartikasw.kelilink.core.domain.use_case.recommendation

import android.location.Location
import com.kartikasw.kelilink.core.domain.Resource
import com.kartikasw.kelilink.core.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface RecommendationUseCase {
    fun getAllStore(): Flow<Resource<List<Store>>>
    fun getStoreByCategory(category: String): Flow<Resource<List<Store>>>

    suspend fun getCurrentLocation(): Location?
    suspend fun getAddressFromLocation(location: Location): List<String>
}