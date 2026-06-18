package com.pisowifi.pos.data.repository

import com.pisowifi.pos.data.dao.AreaDao
import com.pisowifi.pos.data.dao.HarvestDao
import com.pisowifi.pos.data.dao.PisowifiDao
import com.pisowifi.pos.data.dao.SaleDao
import com.pisowifi.pos.data.database.AppDatabase
import com.pisowifi.pos.data.entity.Area
import com.pisowifi.pos.data.entity.HarvestRecord
import com.pisowifi.pos.data.entity.PisowifiDevice
import com.pisowifi.pos.data.entity.SaleRecord

class PisoRepository(private val database: AppDatabase) {
    private val areaDao: AreaDao = database.areaDao()
    private val pisowifiDao: PisowifiDao = database.pisowifiDao()
    private val saleDao: SaleDao = database.saleDao()
    private val harvestDao: HarvestDao = database.harvestDao()

    fun getAllAreas() = areaDao.getAll()
    suspend fun addArea(area: Area) = areaDao.insert(area)
    suspend fun updateArea(area: Area) = areaDao.update(area)
    suspend fun deleteArea(area: Area) = areaDao.delete(area)

    fun getAllPisowifi() = pisowifiDao.getAll()
    suspend fun getPisowifiById(id: Int) = pisowifiDao.getById(id)
    suspend fun addPisowifi(device: PisowifiDevice) = pisowifiDao.insert(device)
    suspend fun updatePisowifi(device: PisowifiDevice) = pisowifiDao.update(device)
    suspend fun deletePisowifi(device: PisowifiDevice) = pisowifiDao.delete(device)

    suspend fun addSale(sale: SaleRecord) = saleDao.insert(sale)
    suspend fun deleteSale(sale: SaleRecord) = saleDao.delete(sale)
    fun getTotalSales(start: Long, end: Long) = saleDao.getTotalSales(start, end)
    fun getTotalSalesByPisowifi(pisowifiId: Int, start: Long, end: Long) = saleDao.getTotalSalesByPisowifi(pisowifiId, start, end)
    fun getSalesSummaryByPisowifi(start: Long, end: Long) = saleDao.getSalesSummaryByPisowifi(start, end)

    suspend fun addHarvest(record: HarvestRecord) = harvestDao.insert(record)
    suspend fun updateHarvest(record: HarvestRecord) = harvestDao.update(record)
    suspend fun updateShareGiven(id: Int, given: Boolean) = harvestDao.updateShareGiven(id, given)
    suspend fun deleteHarvest(id: Int) = harvestDao.deleteById(id)
    fun getHarvestsWithDetails() = harvestDao.getAllWithDetails()
    fun getPendingShares() = harvestDao.getPendingShares()
    suspend fun getSalesSince(pisowifiId: Int, since: Long) = harvestDao.getSalesSince(pisowifiId, since)

    suspend fun updateAreaShareStatus(areaId: Int, given: Boolean) = areaDao.updateShareStatus(areaId, given)
    fun getAreasPendingShare() = areaDao.getPendingShares()
    fun getAreasCompletedShare() = areaDao.getCompletedShares()
    suspend fun getTotalSalesByArea(areaId: Int) = areaDao.getTotalSalesByArea(areaId)
    suspend fun getTotalHarvestShareByArea(areaId: Int) = areaDao.getTotalHarvestShareByArea(areaId)

    suspend fun clearAllSales() = saleDao.clearAll()
    suspend fun clearAllHarvests() = harvestDao.clearAll()
}
