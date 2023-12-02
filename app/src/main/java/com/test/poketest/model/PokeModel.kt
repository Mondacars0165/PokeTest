import com.google.gson.annotations.SerializedName



data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String,
    val imageUrl: String
)
data class PokemonDetails(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerializedName("base_experience")
    val baseExperience: Int,
    val sprites: Sprites,
    val types: List<Type>,
    val abilities: List<Ability>,
    val moves: List<Move>
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String,
    @SerializedName("front_shiny")
    val frontShiny: String,
    // Nuevos atributos para las imágenes
    @SerializedName("back_default")
    val backDefault: String,
    @SerializedName("back_shiny")
    val backShiny: String
)

data class Type(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String,
    val url: String
)

data class Ability(
    val ability: AbilityInfo,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
)

data class AbilityInfo(
    val name: String,
    val url: String
)

data class Move(
    val move: MoveInfo,
    @SerializedName("version_group_details")
    val versionGroupDetails: List<VersionGroupDetail>
)

data class MoveInfo(
    val name: String,
    val url: String
)

data class VersionGroupDetail(
    @SerializedName("level_learned_at")
    val levelLearnedAt: Int,
    @SerializedName("version_group")
    val versionGroup: VersionGroup,
    @SerializedName("move_learn_method")
    val moveLearnMethod: MoveLearnMethod
)

data class VersionGroup(
    val name: String,
    val url: String
)

data class MoveLearnMethod(
    val name: String,
    val url: String
)