package info.matthewryan.workoutlogger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.matthewryan.workoutlogger.dao.ActivityDao
import info.matthewryan.workoutlogger.dao.ExerciseDao
import info.matthewryan.workoutlogger.dao.SessionDao
import info.matthewryan.workoutlogger.model.Activity
import info.matthewryan.workoutlogger.model.Converters
import info.matthewryan.workoutlogger.model.Exercise
import info.matthewryan.workoutlogger.model.Session

@Database(
    entities = [Exercise::class, Activity::class, Session::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun activityDao(): ActivityDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
