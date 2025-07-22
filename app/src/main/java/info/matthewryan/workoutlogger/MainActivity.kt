package info.matthewryan.workoutlogger

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import info.matthewryan.workoutlogger.databinding.ActivityMainBinding
import info.matthewryan.workoutlogger.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val exerciseDao by lazy { db.exerciseDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔸 Apply saved theme preference before setContentView
        val prefs = getSharedPreferences("settings", 0)
        when (prefs.getString("app_theme", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup BottomNavigationView and NavController
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_exercises,
                R.id.navigation_progress,
                R.id.navigation_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_exercises -> {
                    navController.navigate(R.id.navigation_exercises)
                    true
                }
                R.id.navigation_progress -> {
                    navController.navigate(R.id.navigation_progress)
                    true
                }
                R.id.navigation_history -> {
                    navController.navigate(R.id.navigation_history)
                    true
                }
                R.id.navigation_config -> {
                    navController.navigate(R.id.navigation_config)
                    true
                }
                else -> false
            }
        }

        preloadExercises()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    isEnabled = false
                    onBackPressed()
                } else {
                    navController.popBackStack()
                }
            }
        })
    }

    private fun preloadExercises() {
        lifecycleScope.launch {
            val exercises = withContext(Dispatchers.IO) {
                exerciseDao.getAllExercises()
            }

            if (exercises.isEmpty()) {
                val defaultExercises = listOf(
                    Exercise(name = "Abdominal Twists", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Abmat Crunches", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Back Extension", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Back Squat", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Barbell Chest Squat", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Bench Press", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Bicep Curl", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Bulgarian Split Squat", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Cable Side Deltoid Pulls", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Deadlift", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Dumbbell Bench Press", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Dumbbell Chin Row", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Dumbbell Side Raises", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Dumbbell Shoulder Press", factory = true, isUnilateral = true, isTimed = false, duration = null),
                    Exercise(name = "Front Dumbbell Raise", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Front Squat", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Hanging Bent Knee Leg Raises", factory = false, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Lat Pulldown", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Leg Curl", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Leg Extension", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Leg Press", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Neck Curl", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Neck Extension", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Overhead Squat", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Parallel Dips", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Pull-Up", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Push-Up", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Rack Pull", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Reverse Grip Lat Pulldown", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Rope Pulls To Face", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Seated Row", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Trapbar Deadlift", factory = true, isUnilateral = false, isTimed = false, duration = null),
                    Exercise(name = "Triceps Extension", factory = true, isUnilateral = false, isTimed = false, duration = null)
                )

                withContext(Dispatchers.IO) {
                    defaultExercises.forEach { exerciseDao.insert(it) }
                }
            }
        }
    }

    private fun deleteAllExercises() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                exerciseDao.deleteAll()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
