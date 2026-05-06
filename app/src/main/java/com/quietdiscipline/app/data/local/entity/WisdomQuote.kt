package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 激励名言
 */
@Entity(tableName = "wisdom_quotes")
data class WisdomQuote(
    @PrimaryKey
    val id: String,
    val content: String,
    val author: String,
    val category: String     // "waiting", "morning", "progress", "general"
)
