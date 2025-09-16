package com.karrar.movieapp.ui.mappers


import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Crew
import com.karrar.movieapp.ui.models.CrewUIState
import javax.inject.Inject

class CrewUIStateMapper @Inject constructor() : Mapper<Crew, CrewUIState> {
    override fun map(input: Crew): CrewUIState {
        return CrewUIState(
            name = input.crewMemberName,
            job = input.crewMemberJob
        )
    }
}