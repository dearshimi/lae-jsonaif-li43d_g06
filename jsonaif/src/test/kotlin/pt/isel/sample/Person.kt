import pt.isel.JsonConvert
import pt.isel.JsonProperty
import pt.isel.sample.JsonToDate
import pt.isel.sample.Date


data class Person (val id: Int, @property:JsonProperty("nome") @param:JsonProperty("nome") val name: String, @property:JsonConvert(
    JsonToDate::class) @param:JsonConvert(JsonToDate::class) var birth: Date? = null, var sibling: Person? = null)

