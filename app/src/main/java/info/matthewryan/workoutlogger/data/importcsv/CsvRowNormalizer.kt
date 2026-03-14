package info.matthewryan.workoutlogger.data.importcsv

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CsvRowNormalizer {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun normalize(row: CsvImportRow): NormalizedImportRow {

        val dateTime = LocalDateTime.parse(row.date, formatter)

        val timestamp = dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val sessionDate = dateTime
            .toLocalDate()
            .toString()

        val exerciseName = row.exerciseName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("Missing exercise name at CSV line ${row.lineNumber}")

        val reps = row.reps?.toIntOrNull()
            ?: error("Invalid reps at CSV line ${row.lineNumber}")

        val weightKg = row.weightKg?.toDoubleOrNull()
            ?: error("Invalid weight at CSV line ${row.lineNumber}")

        return NormalizedImportRow(
            lineNumber = row.lineNumber,
            timestamp = timestamp,
            sessionDate = sessionDate,
            workoutName = row.workoutName,
            exerciseName = exerciseName,
            reps = reps,
            weightKg = weightKg,
            notes = row.notes,
            durationSeconds = row.duration?.toIntOrNull()
        )
    }
}