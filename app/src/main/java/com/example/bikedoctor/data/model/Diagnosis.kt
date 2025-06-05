package com.example.bikedoctor.data.model

import android.os.Parcel
import android.os.Parcelable

data class Diagnosis(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listDiagnostic: List<Diagnostic>? = emptyList(),
    val reviewed: Boolean?
)

data class DiagnosisPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listDiagnostic: List<Diagnostic>?,
    val reviewed: Boolean? = false
)

data class Diagnostic(
    val error: String?,
    val detailOfError: String?,
    val timeSpent: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(error)
        parcel.writeString(detailOfError)
        parcel.writeValue(timeSpent)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Diagnostic> {
        override fun createFromParcel(parcel: Parcel): Diagnostic {
            return Diagnostic(parcel)
        }

        override fun newArray(size: Int): Array<Diagnostic?> {
            return arrayOfNulls(size)
        }
    }
}