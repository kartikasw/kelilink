package com.kartikasw.kelilink.core.data.repository

import com.kartikasw.kelilink.core.data.helper.NetworkBoundResource
import com.kartikasw.kelilink.core.data.helper.Response
import com.kartikasw.kelilink.core.data.mapper.toListEntity
import com.kartikasw.kelilink.core.data.mapper.toListFlowModel
import com.kartikasw.kelilink.core.data.mapper.toModel
import com.kartikasw.kelilink.core.data.source.local.LocalDataSource
import com.kartikasw.kelilink.core.data.source.remote.RemoteDataSource
import com.kartikasw.kelilink.core.data.source.remote.response.MenuResponse
import com.kartikasw.kelilink.core.domain.Resource
import com.kartikasw.kelilink.core.domain.model.*
import com.kartikasw.kelilink.core.domain.repository.OrderRepository
import com.kartikasw.kelilink.core.util.AppExecutors
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource,
    private val appExecutors: AppExecutors
): OrderRepository {

    override fun getStoreById(id: String): Flow<Resource<Store>> =
        flow {
            emit(Resource.Loading())
            when(val response = remote.getStoreById(id).first()) {
                is Response.Success -> {
                    emit(Resource.Success(response.data.toModel()))
                }
                is Response.Empty -> {
                    emit(Resource.Success(null))
                }
                is Response.Error -> {
                    emit(Resource.Error(response.errorMessage))
                }
            }
        }

    override fun getMenuById(menuId: String): Flow<Resource<Menu>> =
        local.selectMenuById(menuId).map { Resource.Success(it.toModel()) }

    override fun getMenuByStoreId(storeId: String): Flow<Resource<List<Menu>>> =
        object : NetworkBoundResource<List<Menu>, List<MenuResponse>>() {
            override fun loadFromDB(): Flow<List<Menu>?> =
                local.selectAllMenu().toListFlowModel()

            override fun shouldFetch(data: List<Menu>?): Boolean =
                data == null || data.isEmpty()

            override suspend fun createCall(): Flow<Response<List<MenuResponse>>> =
                remote.getMenuByStoreId(storeId)

            override suspend fun saveCallResult(data: List<MenuResponse>) {
                local.insertAllMenu(data.toListEntity())
            }

        }.asFlow()

    override fun getOrderedMenu(): Flow<Resource<List<Menu>>> =
        local.selectOrderedMenu().toListFlowModel().map { Resource.Success(it) }

    override fun updateAmountAndTotalPriceMenu(id: String, amount: Int, price: Int) {
        appExecutors.diskIO().execute {
            local.updateAmountAndTotalPriceMenu(id, amount, price)
        }
    }

    override fun updateNoteMenu(id: String, note: String) {
        appExecutors.diskIO().execute {
            local.updateNoteMenu(id, note)
        }
    }

    override fun deleteAllMenu() {
        appExecutors.diskIO().execute {
            local.deleteAllMenu()
        }
    }

    override fun makeOrder(invoice: Invoice, orders: List<Order>): Flow<Resource<Invoice>> =
        flow {
            emit(Resource.Loading())
            when(val response = remote.makeOrder(invoice, orders).first()) {
                is Response.Success -> {
                    emit(Resource.Success(response.data.toModel()))
                }
                is Response.Empty -> {
                    emit(Resource.Success(null))
                }
                is Response.Error -> {
                    emit(Resource.Error(response.errorMessage))
                }
            }
        }

    override fun listenToOrder(invoiceId: String, storeId: String): Flow<Resource<Invoice>> =
        flow {
            emit(Resource.Loading())
            when(val response = remote.listenToOrder(invoiceId, storeId).first()) {
                is Response.Success -> {
                    emit(Resource.Success(response.data.toModel()))
                }
                is Response.Error -> {
                    emit(Resource.Error(response.errorMessage))
                }
                else -> {}
            }
        }

    override fun deleteOrder(invoiceId: String): Flow<Resource<Unit>> =
        flow {
            emit(Resource.Loading())
            when(val response = remote.deleteOrder(invoiceId).first()) {
                is Response.Success -> {
                    emit(Resource.Success(null))
                }
                is Response.Error -> {
                    emit(Resource.Error(response.errorMessage))
                }
                else -> {}
            }
        }

    override fun sendFcm(data: Fcm): Flow<Resource<Unit>> =
        flow {
            emit(Resource.Loading())
            when(val response = remote.sendFcm(data).first()) {
                is Response.Success -> {
                    emit(Resource.Success(null))
                }
                is Response.Error -> {
                    emit(Resource.Error(response.errorMessage))
                }
                else -> {}
            }
        }

}