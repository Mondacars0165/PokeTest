data class PokemonDetailsResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val abilities: List<PokemonAbility>,
    // Agrega más campos según la estructura de la respuesta de la API
)

data class PokemonType(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String,
    val url: String
)

data class PokemonAbility(
    val ability: Ability,
    val isHidden: Boolean,
    val slot: Int
)

data class Ability(
    val name: String,
    val url: String
)
