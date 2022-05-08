package com.hollowlog.fitnesstracker

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

class ExerciseObject(
    var exerciseName: String?,
    var exerciseType: String?,
    var setList: List<SetObject>?
) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(SetObject)
    ) {
    }

    fun addNewSet(setObject: SetObject) {
        setList = setList?.plus(setObject)
    }

    fun removeSet(setObject: SetObject) {
        setList = setList?.filter { curr ->
            curr != setObject
        }
    }

    fun editSet(setObject: SetObject, position: Int) {
        setList?.get(position)?.reps = setObject.reps
        setList?.get(position)?.restTime = setObject.restTime
        setList?.get(position)?.weight = setObject.weight
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(exerciseName)
        parcel.writeString(exerciseType)
        parcel.writeTypedList(setList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExerciseObject> {
        override fun createFromParcel(parcel: Parcel): ExerciseObject {
            return ExerciseObject(parcel)
        }

        override fun newArray(size: Int): Array<ExerciseObject?> {
            return arrayOfNulls(size)
        }
    }
}