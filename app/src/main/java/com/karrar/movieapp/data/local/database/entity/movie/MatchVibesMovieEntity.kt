package com.karrar.movieapp.data.local.database.entity.movie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MATCH_VIBES_MOVIE_TABLE")
data class MatchVibesMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val movieRate: Double,
    val genreName: List<String>,
)