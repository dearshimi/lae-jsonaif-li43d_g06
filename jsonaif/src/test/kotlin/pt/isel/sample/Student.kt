package pt.isel.sample

import pt.isel.JsonProperty

data class Student (var nr: Int = 0, @JsonProperty("nome")var name: String? = null,var birth : Date? = null)
