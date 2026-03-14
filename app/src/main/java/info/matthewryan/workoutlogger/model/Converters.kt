package info.matthewryan.workoutlogger.model

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromExerciseType(value: ExerciseType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toExerciseType(value: String?): ExerciseType? {
        return value?.let { ExerciseType.valueOf(it) }
    }
}