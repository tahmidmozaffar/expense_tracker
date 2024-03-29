package com.remotearthsolutions.expensetracker.databaseutils.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCategory(categoryModel: CategoryModel): Long

    @Insert
    fun addAllCategories(vararg categoryModels: CategoryModel)

    @get:Query("Select * from category")
    val allCategories: Single<List<CategoryModel>>

    @Delete
    fun deleteCategory(categoryModel: CategoryModel)

    @Query("DELETE FROM category")
    fun deleteAll()

    @Update
    fun updateCategory(categoryModel: CategoryModel?)

    @get:Query("SELECT COUNT(category_name) FROM category")
    val dataCount: LiveData<Int>
}