package com.karrar.movieapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.karrar.movieapp.data.local.database.daos.ActorDao
import com.karrar.movieapp.data.local.database.daos.MovieDao
import com.karrar.movieapp.data.local.database.daos.SeriesDao
import com.karrar.movieapp.data.local.database.entity.ActorEntity
import com.karrar.movieapp.data.local.database.entity.SearchHistoryEntity
import com.karrar.movieapp.data.local.database.entity.WatchHistoryEntity
import com.karrar.movieapp.data.local.database.entity.WatchList
import com.karrar.movieapp.data.local.database.entity.movie.AdventureMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.MatchVibesMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.MysteryMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.NowStreamingMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.PopularMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.TrendingMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.UpcomingMovieEntity
import com.karrar.movieapp.data.local.database.entity.series.AiringTodaySeriesEntity
import com.karrar.movieapp.data.local.database.entity.series.OnTheAirSeriesEntity
import com.karrar.movieapp.data.local.database.entity.series.TopRatedSeriesEntity

@Database(
    entities = [WatchList::class, SearchHistoryEntity::class, WatchHistoryEntity::class,PopularMovieEntity::class,
        ActorEntity::class, TrendingMovieEntity::class, NowStreamingMovieEntity::class,UpcomingMovieEntity::class,
        MysteryMovieEntity::class,AdventureMovieEntity::class, AiringTodaySeriesEntity::class,
        OnTheAirSeriesEntity::class, TopRatedSeriesEntity::class, MatchVibesMovieEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MovieDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun actorDao(): ActorDao
    abstract fun seriesDao(): SeriesDao
}