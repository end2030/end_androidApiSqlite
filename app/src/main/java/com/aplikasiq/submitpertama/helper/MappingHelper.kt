import android.database.Cursor

object MappingHelper {

    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<FavoritEntity> {
        val notesList = ArrayList<FavoritEntity>()
        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.noteColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.noteColumns.TITLE))
                val description = getString(getColumnIndexOrThrow(DatabaseContract.noteColumns.DESCRIPTION))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.noteColumns.DATE))
                notesList.add(FavoritEntity(id, title, description, date))
            }
        }
        return notesList
    }
}