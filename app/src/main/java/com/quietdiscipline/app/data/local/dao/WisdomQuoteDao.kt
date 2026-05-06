package com.quietdiscipline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quietdiscipline.app.data.local.entity.WisdomQuote

@Dao
interface WisdomQuoteDao {

    @Query("SELECT * FROM wisdom_quotes WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteByCategory(category: String): WisdomQuote?

    @Query("SELECT * FROM wisdom_quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): WisdomQuote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<WisdomQuote>)

    @Query("SELECT COUNT(*) FROM wisdom_quotes")
    suspend fun getCount(): Int
}
