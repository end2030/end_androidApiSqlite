import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoritEntity (
    var id: Int = 0,
    var title: String? = null,
    var description: String? = null,
    var date: String? = null

): Parcelable