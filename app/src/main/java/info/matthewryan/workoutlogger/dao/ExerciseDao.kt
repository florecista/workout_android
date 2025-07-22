package info.matthewryan.workoutlogger.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import info.matthewryan.workoutlogger.model.Exercise

@Dao
interface ExerciseDao {

    @Insert
    fun insert(exercise: Exercise)

    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    fun getExerciseByName(name: String): Exercise?

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    fun getExerciseById(id: Int): Exercise?

    @Query("SELECT * FROM exercise")
    fun getAllExercises(): List<Exercise>  // Correct return type

    @Query("DELETE FROM exercise")
    fun deleteAll()  // Correct return type

    @Query("DELETE FROM exercise WHERE factory = 0")
    fun deleteNonFactoryExercises()

}
