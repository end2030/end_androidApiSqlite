import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    var username: String,
    var nama: String,
    var photos: String,
    var follower: String,
    var following: String,

    ) : Parcelable


