package com.karrar.movieapp.data.repository

import androidx.paging.Pager
import com.karrar.movieapp.data.local.database.entity.ActorEntity
import com.karrar.movieapp.data.local.database.entity.SearchHistoryEntity
import com.karrar.movieapp.data.local.database.entity.WatchHistoryEntity
import com.karrar.movieapp.data.local.database.entity.movie.AdventureMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.MatchVibesMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.MysteryMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.NowStreamingMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.PopularMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.TrendingMovieEntity
import com.karrar.movieapp.data.local.database.entity.movie.UpcomingMovieEntity
import com.karrar.movieapp.data.remote.response.AddListResponse
import com.karrar.movieapp.data.remote.response.AddMovieDto
import com.karrar.movieapp.data.remote.response.BaseListResponse
import com.karrar.movieapp.data.remote.response.CreatedListDto
import com.karrar.movieapp.data.remote.response.CreditsDto
import com.karrar.movieapp.data.remote.response.DailyTrendingDto
import com.karrar.movieapp.data.remote.response.MovieDto
import com.karrar.movieapp.data.remote.response.MyListsDto
import com.karrar.movieapp.data.remote.response.RatedMoviesDto
import com.karrar.movieapp.data.remote.response.SavedListDto
import com.karrar.movieapp.data.remote.response.actor.ActorDto
import com.karrar.movieapp.data.remote.response.actor.ActorGalleryDto
import com.karrar.movieapp.data.remote.response.actor.ActorMoviesDto
import com.karrar.movieapp.data.remote.response.actor.ActorSocialMediaDto
import com.karrar.movieapp.data.remote.response.genre.GenreDto
import com.karrar.movieapp.data.remote.response.movie.MovieDetailsDto
import com.karrar.movieapp.data.remote.response.movie.RatingDto
import com.karrar.movieapp.data.remote.response.review.ReviewsDto
import com.karrar.movieapp.data.remote.response.trailerVideosDto.TrailerDto
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    suspend fun getMovieGenreList(): List<GenreDto>?

    suspend fun getDailyTrending(): BaseListResponse<DailyTrendingDto>

    suspend fun getMovieTrailer(movieId: Int): TrailerDto?

    suspend fun getActorDetails(actorId: Int): ActorDto?

    suspend fun getActorMovies(actorId: Int): ActorMoviesDto?

    suspend fun getGalleryActor(actorId: Int): ActorGalleryDto?

    suspend fun getActorSocialMedia(actorId: Int): ActorSocialMediaDto?

    suspend fun getAllLists(sessionId: String): List<CreatedListDto>?

    suspend fun getListDetails(listId: Int): MyListsDto?

    suspend fun getSavedListDetails(listId: Int): List<SavedListDto>?

    suspend fun createList(sessionId: String, name: String): AddListResponse?

    suspend fun addMovieToList(sessionId: String, listId: Int, movieId: Int): AddMovieDto?

    suspend fun clearWatchHistory()

    suspend fun insertSearchItem(item: SearchHistoryEntity)

    suspend fun deleteSearchItem(item: SearchHistoryEntity)

    suspend fun insertMovie(movie: WatchHistoryEntity)

    fun getAllWatchedMovies(): Flow<List<WatchHistoryEntity>>

    suspend fun deleteRecentlyViewedItemById(id: Int)

    suspend fun getAllMovies(): Pager<Int, MovieDto>

    suspend fun getMovieByGenre(genreID: Int): Pager<Int, MovieDto>

    suspend fun getPopularMovies(): Flow<List<PopularMovieEntity>>

    suspend fun getTrendingMovies(): Flow<List<TrendingMovieEntity>>

    suspend fun getNowStreamingMovies(): Flow<List<NowStreamingMovieEntity>>

    suspend fun getUpcomingMovies(): Flow<List<UpcomingMovieEntity>>

    suspend fun getAdventureMovies(): Flow<List<AdventureMovieEntity>>

    suspend fun getMysteryMovies(): Flow<List<MysteryMovieEntity>>

    suspend fun getTrendingActors(): Flow<List<ActorEntity>>

    suspend fun getTrendingMoviesPager(): Pager<Int, MovieDto>

    suspend fun getNowPlayingMoviesPager(): Pager<Int, MovieDto>

    suspend fun getUpcomingMoviesPager(): Pager<Int, MovieDto>

    suspend fun getSimilarMoviePager(): Pager<Int, MovieDto>

    suspend fun getMovieReviewsPager(): Pager<Int, ReviewsDto>

    suspend fun getActorMoviesPager(actorId: Int): Pager<Int, MovieDto>

    suspend fun getAdventureMoviesPager(): Pager<Int, MovieDto>

    suspend fun getMysteryMoviesPager(): Pager<Int, MovieDto>

    suspend fun searchForMoviePager(query: String): Pager<Int, MovieDto>

    suspend fun searchForActorPager(query: String): Pager<Int, ActorDto>

    suspend fun getAllSearchHistory(): Flow<List<SearchHistoryEntity>>

    suspend fun getActorData(): Pager<Int, ActorDto>

    suspend fun getMovieDetails(movieId: Int): MovieDetailsDto?

    suspend fun getMovieCast(movieId: Int): CreditsDto?

    suspend fun getSimilarMovie(movieId: Int): List<MovieDto>?

    suspend fun getMovieReviews(movieId: Int): List<ReviewsDto>?

    suspend fun setRating(movieId: Int, value: Float): RatingDto?

    suspend fun deleteRating(movieId: Int): RatingDto?

    suspend fun getRatedMovie(): List<RatedMoviesDto>?

    suspend fun getMovieCastAndCrew(movieId: Int): CreditsDto?
    suspend fun clearAllRecentSearch()

    suspend fun getMatchedMovies(
        page: Int,
        genres: String?,
        runtimeGte: Int?,
        runtimeLte: Int?,
        releaseDateGte: String?,
        releaseDateLte: String?
    ): Flow<List<MatchVibesMovieEntity>>

    suspend fun getCurrentMatchVibesMovies(): Flow<List<MatchVibesMovieEntity>>

    suspend fun getMatchVibesMoviesPager(): Pager<Int, MatchVibesMovieEntity>
}