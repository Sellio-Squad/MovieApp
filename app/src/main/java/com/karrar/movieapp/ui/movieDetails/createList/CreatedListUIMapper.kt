package com.karrar.movieapp.ui.movieDetails.createList

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.CreatedList
import com.karrar.movieapp.ui.movieDetails.saveMovie.uiState.CreatedListUIState
import jakarta.inject.Inject

class CreatedListUIMapper @Inject constructor() : Mapper<CreatedList, CreatedListUIState> {

    override fun map(input: CreatedList): CreatedListUIState {
        return CreatedListUIState(
            listID = input.id,
            name = input.name,
            mediaCounts = input.itemCount
        )
    }
}