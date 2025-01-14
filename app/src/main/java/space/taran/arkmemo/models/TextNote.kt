package space.taran.arkmemo.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import space.taran.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    val content: Content,
    @IgnoredOnParcel
    val meta: ResourceMeta? = null
): Parcelable
{
    @Parcelize
    data class Content(
        val title: String,
        val data: String
        ): Parcelable
}