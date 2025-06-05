package com.example.bikedoctor.data.model

import android.os.Parcel
import android.os.Parcelable

data class CostApproval(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listLaborCosts: List<LaborCost>? = emptyList(),
    val reviewed: Boolean?
)

data class CostApprovalPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listLaborCosts: List<LaborCost>?,
    val reviewed: Boolean? = false
)

data class LaborCost(
    val nameProduct: String?,
    val descriptionProduct: String?,
    val price: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        (parcel.readValue(Int::class.java.classLoader) as? Int).toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameProduct)
        parcel.writeString(descriptionProduct)
        parcel.writeValue(price)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LaborCost> {
        override fun createFromParcel(parcel: Parcel): LaborCost {
            return LaborCost(parcel)
        }

        override fun newArray(size: Int): Array<LaborCost?> {
            return arrayOfNulls(size)
        }
    }
}