package pt.isel.sample

import pt.isel.JsonProperty

data class Person (val id: Int, @JsonProperty("nome") val name: String, val birth: Date? = null, var sibling: Person? = null)
