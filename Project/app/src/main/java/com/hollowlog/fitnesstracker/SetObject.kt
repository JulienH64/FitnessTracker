package com.hollowlog.fitnesstracker

import android.os.Parcel
import android.os.Parcelable

class SetObject(var reps: Int, var weight: Double, var restTime: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(reps)
        parcel.writeDouble(weight)
        parcel.writeString(restTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SetObject> {
        override fun createFromParcel(parcel: Parcel): SetObject {
            return SetObject(parcel)
        }

        override fun newArray(size: Int): Array<SetObject?> {
            return arrayOfNulls(size)
        }
    }

}