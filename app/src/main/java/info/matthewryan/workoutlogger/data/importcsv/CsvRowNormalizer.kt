package info.matthewryan.workoutlogger.data.importcsv

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CsvRowNormalizer {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun normalize(row: CsvImportRow): NormalizedImportRow {

        val timestamp = parseTimestamp(row.date)

        val sessionDate = LocalDateTime
            .parse(row.date, formatter)
            .toLocalDate()
            .toString()

        return NormalizedImportRow(
            lineNumber = row.lineNumber,
            timestamp = timestamp,
            sessionDate = sessionDate,
            workoutName = row.workoutName,
            exerciseName = row.exerciseName!!.trim(),
            reps = row.reps!!.toInt(),
            weightKg = row.weightKg!!.toDouble(),
            notes = row.notes,
            durationSeconds = row.duration?.toIntOrNull()
        )
    }

    private fun parseTimestamp(date: String): Long {

        return LocalDateTime
            .parse(date, formatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}