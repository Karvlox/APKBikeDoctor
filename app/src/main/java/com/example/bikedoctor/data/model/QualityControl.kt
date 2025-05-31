package com.example.bikedoctor.data.model

import android.os.Parcel
import android.os.Parcelable

data class QualityControl(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listControls: List<Control>? = emptyList(),
    val reviewed: Boolean?
)

data class QualityControlPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listControls: List<Control>?,
    val reviewed: Boolean? = false
)

data class Control(
    val controlName: String?,
    val detailsControl: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(controlName)
        parcel.writeString(detailsControl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Control> {
        override fun createFromParcel(parcel: Parcel): Control {
            return Control(parcel)
        }

        override fun newArray(size: Int): Array<Control?> {
            return arrayOfNulls(size)
        }
    }
}