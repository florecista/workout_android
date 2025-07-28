package info.matthewryan.workoutlogger.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise",
    indices = [Index(value = ["name"], unique = true)]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val factory: Boolean,
    val isUnilateral: Boolean,
    val isTimed: Boolean,
    val duration: Int?, // Duration in seconds, nullable
    val type: ExerciseType = ExerciseType.STRENGTH
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        ExerciseType.valueOf(parcel.readString() ?: ExerciseType.STRENGTH.name) // <-- NEW
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeByte(if (factory) 1 else 0)
        parcel.writeByte(if (isUnilateral) 1 else 0)
        parcel.writeByte(if (isTimed) 1 else 0)
        parcel.writeValue(duration)
        parcel.writeString(type.name) // <-- NEW
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Exercise> {
            override fun createFromParcel(parcel: Parcel): Exercise {
                return Exercise(parcel)
            }

            override fun newArray(size: Int): Array<Exercise?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun toString(): String {
        return "Exercise{id=$id, name='$name', type=$type}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Exercise
        return id == other.id && name == other.name
    }

    override fun hashCode(): Int {
        return 31 * id + name.hashCode()
    }
}
