package pt.isel

class JsonToDate {

    companion object {

        fun Convert(date: JsonTokens): JsonTokens {
            date.popWordFinishedWith('"')
            val year = date.popWordFinishedWith('-')
            val month = date.popWordFinishedWith('-')
            val day = date.popWordFinishedWith('"')
            return JsonTokens("{ year: $year, month: $month, day: $day}")
        }
    }
}
