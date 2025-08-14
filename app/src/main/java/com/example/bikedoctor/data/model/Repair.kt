package com.example.bikedoctor.data.model

import android.os.Parcel
import android.os.Parcelable

data class Repair(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listReparations: List<Reparation>? = emptyList(),
    val reviewed: Boolean?
)

data class RepairPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listReparations: List<Reparation>?,
    val reviewed: Boolean? = false
)


data class Reparation(
    val nameReparation: String?,
    val descriptionReparation: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameReparation)
        parcel.writeString(descriptionReparation)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Reparation> {
        override fun createFromParcel(parcel: Parcel): Reparation {
            return Reparation(parcel)
        }

        override fun newArray(size: Int): Array<Reparation?> {
            return arrayOfNulls(size)
        }
    }
}