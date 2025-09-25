package com.karrar.movieapp.domain.usecases.tvShowDetails

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.karrar.movieapp.data.repository.SeriesRepository
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.domain.mappers.ListMapper
import com.karrar.movieapp.domain.mappers.SeriesMapperContainer
import com.karrar.movieapp.domain.mappers.series.TVShowMapper
import com.karrar.movieapp.domain.models.Actor
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.domain.models.Crew
import com.karrar.movieapp.domain.models.MediaDetailsReviews
import com.karrar.movieapp.domain.models.Season
import com.karrar.movieapp.domain.models.TvShowDetails
import com.karrar.movieapp.domain.usecases.GetReviewsUseCase
import com.karrar.movieapp.utilities.Constants.MAX_NUM_REVIEWS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTvShowDetailsUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository,
    private val seriesMapperContainer: SeriesMapperContainer,
    private val tvShowMapper: TVShowMapper,
    private val getTVShowsReviews: GetReviewsUseCase
) {

    suspend fun getTvShowDetails(tvShowId: Int): TvShowDetails {
        return seriesRepository.getTvShowDetails(tvShowId)?.let {
            seriesMapperContainer.tvShowDetailsMapper.map(it)
        } ?: TvShowDetails()
    }

    suspend fun getSeriesCast(tvShowId: Int): List<Actor> {
        return ListMapper(seriesMapperContainer.actorMapper)
            .mapList(seriesRepository.getTvShowCastAndCrew(tvShowId)?.cast)
    }

    suspend fun getSeriesCrew(tvShowId: Int): List<Crew> {
        return ListMapper(seriesMapperContainer.seriesCrewMapper)
            .mapList(seriesRepository.getTvShowCastAndCrew(tvShowId)?.crew)
    }

    suspend fun getSimilarTvShow(tvShowId: Int): List<Media> {
        return seriesRepository.getSimilarTvShow(tvShowId)?.let {
            it.map { tvShowMapper.map(it) }
        } ?: throw Throwable("Not Success")
    }

    suspend fun getSimilarTvShowPager(tvShowId: Int): Flow<PagingData<Media>> {
        return wrapper({ seriesRepository.getSimilarTvShowPager(tvShowId) }, tvShowMapper::map)
    }

    suspend fun getSeasons(tvShowId: Int): List<Season> {
        val allSeasons = ListMapper(seriesMapperContainer.seasonMapper)
            .mapList(seriesRepository.getTvShowDetails(tvShowId)?.season)
        return allSeasons
            .sortedByDescending { it.seasonNumber }
            .take(3)
    }


    suspend fun getTvShowReviews(tvShowId: Int): MediaDetailsReviews {
        val reviews = getTVShowsReviews(MediaType.TV_SHOW, tvShowId)
        return MediaDetailsReviews(reviews.take(MAX_NUM_REVIEWS), reviews.size > MAX_NUM_REVIEWS)
    }


    suspend fun getTvShowRated(tvShowID: Int): Float {
        val result = seriesRepository.getRatedTvShow()
        return result?.let {
            it.find { it.id == tvShowID }?.rating ?: 0F
        } ?: throw Throwable("Error")
    }

    private suspend fun <T : Any> wrapper(
        data: suspend () -> Pager<Int, T>,
        mapper: (T) -> Media,
    ): Flow<PagingData<Media>> {
        return data().flow.map { pager -> pager.map { mapper(it) } }
    }
}