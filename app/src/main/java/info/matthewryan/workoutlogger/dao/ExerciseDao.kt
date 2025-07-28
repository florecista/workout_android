package info.matthewryan.workoutlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert
    fun insert(exercise: Exercise): Long

    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    fun getExerciseByName(name: String): Exercise?

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    fun getExerciseById(id: Int): Exercise?

    @Query("SELECT * FROM exercise ORDER BY name")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise")
    fun getAllExercisesOnce(): List<Exercise>

    @Query("SELECT * FROM exercise")
    fun getAllExercises(): List<Exercise>

    @Update
    fun update(exercise: Exercise)

    @Query("DELETE FROM exercise")
    fun deleteAll()  // Correct return type

    @Query("DELETE FROM exercise WHERE factory = 0")
    fun deleteNonFactoryExercises()

    @Delete
    fun delete(exercise: Exercise)

}
