package com.karrar.movieapp.domain.mappers

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.local.database.entity.ActorEntity
import com.karrar.movieapp.data.local.mappers.ActorMapper
import com.karrar.movieapp.data.remote.response.actor.ActorDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActorMapperTest {

    private var actorMapper = ActorMapper()

    @BeforeAll
    fun setUp() {
        actorMapper = ActorMapper()
    }

    @Test
    fun should_ReturnActorMapper_when_EnterActorDTO() {
        val actorDTO = ActorDto(
            id = 1,
            name = "name",
            profilePath = "profilePath",
            biography = "biography",
            birthday = "birthday",
            deathday = "deathday",
            placeOfBirth = "placeOfBirth",
            popularity = 1.0,
            alsoKnownAs = listOf(),
            knownForDepartment = "knownForDepartment",
            gender = 1,
            adult = true,
            homepage = "homepage",
            imdbId = "imdbId",
            characterName = "Iron Man"
        )

        val actor = actorMapper.map(actorDTO)

        val expected = ActorEntity(
            id = 1,
            name = "name",
            imageUrl ="profilePath",
            characterName = "Iron Man"
        )


        assertEquals(expected, actor)
    }

}