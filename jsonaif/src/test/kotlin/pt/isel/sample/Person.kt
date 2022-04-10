import pt.isel.JsonConvert
import pt.isel.JsonToDate

data class Person (val id: Int, @JsonProperty("nome") val name: String, @JsonConvert(JsonToDate::class) var birth: Date? = null, var sibling: Person? = null)

