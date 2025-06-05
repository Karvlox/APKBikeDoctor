package com.example.bikedoctor.data.model

import android.os.Parcel
import android.os.Parcelable

data class SpareParts(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val clientName: String?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listSpareParts: List<SparePart>? = emptyList(),
    val reviewed: Boolean?
)
data class SparePartsPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listSpareParts: List<SparePart>?,
    val reviewed: Boolean? = false
)

data class SparePart(
    val nameSparePart: String?,
    val detailSparePart: String?,
    val price: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameSparePart)
        parcel.writeString(detailSparePart)
        parcel.writeValue(price)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SparePart> {
        override fun createFromParcel(parcel: Parcel): SparePart {
            return SparePart(parcel)
        }

        override fun newArray(size: Int): Array<SparePart?> {
            return arrayOfNulls(size)
        }
    }
}