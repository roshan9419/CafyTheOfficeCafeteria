package datamodels

import android.os.Parcel
import android.os.Parcelable

data class MenuItem (
    var imageUrl: String = "IMAGE_URL",
    var itemName: String = "ITEM NAME",
    var itemPrice: Float = 0.0F,
    var itemShortDesc: String = "ITEM DESC",
    var itemTag: String = "ITEM TAG",
    var itemStars: Float = 5.0F,
    var quantity: Int = 0) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readFloat(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readFloat(),
            parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageUrl)
        parcel.writeString(itemName)
        parcel.writeFloat(itemPrice)
        parcel.writeString(itemShortDesc)
        parcel.writeString(itemTag)
        parcel.writeFloat(itemStars)
        parcel.writeInt(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuItem> {
        override fun createFromParcel(parcel: Parcel): MenuItem {
            return MenuItem(parcel)
        }

        override fun newArray(size: Int): Array<MenuItem?> {
            return arrayOfNulls(size)
        }
    }
}