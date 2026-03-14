package info.matthewryan.workoutlogger.data.importcsv

import info.matthewryan.workoutlogger.AppDatabase
import java.io.InputStream

class CsvImportCoordinator(
    private val db: AppDatabase
) {

    private val parser = CsvParser()
    private val normalizer = CsvRowNormalizer()
    private val planner = CsvImportPlanner()
    private val executor = CsvImportExecutor(db)

    suspend fun importCsv(inputStream: InputStream): CsvImportReport {

        // Step 1 — Parse CSV
        val rawRows = parser.parse(inputStream)

        // Step 2 — Normalize rows
        val normalizedRows = rawRows.map { row ->
            normalizer.normalize(row)
        }

        // Step 3 — Build import plan
        val plan = planner.plan(normalizedRows)

        // Step 4 — Execute import
        return executor.execute(plan)
    }
}