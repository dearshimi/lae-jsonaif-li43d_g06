package pt.isel.sample

import pt.isel.JsonTokens

class JsonToDate {

    companion object {
        fun convert(date: String): Date {  // date = 1998-11-17
            val split = date.split("-")
            return Date(split[2].toInt(), split[1].toInt(), split[0].toInt())
        }
    }
}
