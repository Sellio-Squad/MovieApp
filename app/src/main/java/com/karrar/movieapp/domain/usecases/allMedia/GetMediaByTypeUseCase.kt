package com.karrar.movieapp.domain.usecases.allMedia

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.data.repository.SeriesRepository
import com.karrar.movieapp.domain.enums.AllMediaType
import com.karrar.movieapp.domain.mappers.movie.MatchVibesMovieMovieMapper
import com.karrar.movieapp.domain.mappers.movie.MovieMapper
import com.karrar.movieapp.domain.mappers.series.TVShowMapper
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMediaByTypeUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val movieMapper: MovieMapper,
    private val tvShowMapper: TVShowMapper,
    private val matchVibesMovieMovieMapper: MatchVibesMovieMovieMapper
) {

    suspend operator fun invoke(type: AllMediaType, actorId: Int = 0): Flow<PagingData<Media>> {
        return when (type) {
            AllMediaType.ACTOR_MOVIES -> {
                wrapper({ movieRepository.getActorMoviesPager(actorId) }, movieMapper::map)
            }
            AllMediaType.LATEST,
            AllMediaType.AIRING_TODAY,
            -> {
                wrapper(seriesRepository::getAiringTodayTvShowPager, tvShowMapper::map)
            }
            AllMediaType.ON_THE_AIR -> {
                wrapper(seriesRepository::getTopRatedTvShowPager, tvShowMapper::map)
            }
            AllMediaType.POPULAR -> {
                wrapper(seriesRepository::getPopularTvShowPager, tvShowMapper::map)
            }
            AllMediaType.TOP_RATED -> {
                wrapper(seriesRepository::getTopRatedTvShowPager, tvShowMapper::map)
            }
            AllMediaType.TRENDING -> {
                wrapper(movieRepository::getTrendingMoviesPager, movieMapper::map)
            }
            AllMediaType.RECENTLY_RELEASED -> {
                wrapper(movieRepository::getNowPlayingMoviesPager, movieMapper::map)
            }
            AllMediaType.UPCOMING -> {
                wrapper(movieRepository::getUpcomingMoviesPager, movieMapper::map)
            }
            AllMediaType.MYSTERY -> {
                wrapper({ movieRepository.getMovieByGenre(Constants.MYSTERY_ID) }, movieMapper::map)
            }
            AllMediaType.ADVENTURE -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.ADVENTURE_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.LATE_NIGHT_THRILLS -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.HORROR_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.MIND_BENDING_STORIES -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.SCIENCE_FICTION_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.CINEMATIC_MASTERPIECES -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.DRAMA_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.FAMILY_NIGHT_PICKS -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.FAMILY_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.BASED_ON_TRUE_EVENTS -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.HISTORY_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.FEEL_GOOD_FAVORITES -> {
                wrapper(
                    { movieRepository.getMovieByGenre(Constants.COMEDY_ID) },
                    movieMapper::map
                )
            }

            AllMediaType.MATCHES_YOUR_VIBE -> {
                wrapper(
                    { movieRepository.getMatchVibesMoviesPager() },
                    matchVibesMovieMovieMapper::map
                )
            }

            AllMediaType.LASTEST_SEASONS -> TODO()
            AllMediaType.BEHIND_THE_SCENES -> TODO()
            AllMediaType.YOU_MIGHT_ALSO_LIKE_SERIES -> TODO()
            AllMediaType.YOU_MIGHT_ALSO_LIKE_MOVIES -> {
                wrapper(movieRepository::getSimilarMoviePager,movieMapper::map)
            }
            AllMediaType.TOP_REVIEWS_SERIES -> TODO()
            AllMediaType.TOP_REVIEWS_MOVIES -> TODO()
//                {
//                wrapper(movieRepository::getMovieReviewsPager,  )
//            }
        }
    }

    private suspend fun <T : Any> wrapper(
        data: suspend () -> Pager<Int, T>,
        mapper: (T) -> Media,
    ): Flow<PagingData<Media>> {
        return data().flow.map { pager -> pager.map { mapper(it) } }
    }
}