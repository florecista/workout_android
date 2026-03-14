package info.matthewryan.workoutlogger.data.importcsv

import java.io.InputStream

class CsvParser {

    fun parse(input: InputStream): List<CsvImportRow> {

        val rows = mutableListOf<CsvImportRow>()

        input.bufferedReader().useLines { lines ->

            lines.drop(1).forEachIndexed { index, line ->

                val parts = line.split(",")

                rows.add(
                    CsvImportRow(
                        lineNumber = index + 2,
                        date = parts.getOrNull(0) ?: "",
                        workoutName = parts.getOrNull(1),
                        exerciseName = parts.getOrNull(2),
                        reps = parts.getOrNull(3),
                        weightKg = parts.getOrNull(4),
                        weightLb = parts.getOrNull(5),
                        notes = parts.getOrNull(6),
                        duration = parts.getOrNull(7)
                    )
                )
            }
        }

        return rows
    }
}