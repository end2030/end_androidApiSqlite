package com.aplikasiq.submitpertama

import DatabaseContract
import DatabaseContract.noteColumns.Companion.DATE
import FavoritEntity
import FavoritHelper
import MappingHelper
import Users
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity(), View.OnClickListener {
    private var progressBar: ProgressBar? = null
    private lateinit var btn_share: Button
    private lateinit var dt_user: Users
    private lateinit var dt_favorit: FavoritEntity
    private var nama: String? = "nama"
    private var from: String? = "from"

    companion object {
        const val EXTRA_FAVORIT = "extra_favorit"
        const val EXTRA_USER = "extra_user"

        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val FROM_SEARCH = "false"

        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.tab_text_1,
            R.string.tab_text_2

        )
    }

    private lateinit var favoritHelper: FavoritHelper
    private var favorit: FavoritEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.title = "Detail User"
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail)

        val dv_photo: ImageView = findViewById(R.id.img_item_avatar)
        val dv_username: TextView = findViewById(R.id.tv_username)
        val dv_nama: TextView = findViewById(R.id.tv_d_name)
        val dv_following: TextView = findViewById(R.id.tv_following)
        val dv_follower: TextView = findViewById(R.id.tv_follower)
        val dv_company: TextView = findViewById(R.id.tv_company)
        val dv_blog: TextView = findViewById(R.id.tv_blog)
        val dv_location: TextView = findViewById(R.id.tv_item_location)

        var username: String
        from = intent.getStringExtra(FROM_SEARCH)!!
        if (from == "true") {
            dt_user = intent.getParcelableExtra(EXTRA_USER)!!
            username = dt_user.username
        } else {
            dt_favorit = intent.getParcelableExtra(EXTRA_FAVORIT)!!
            username = dt_favorit.description.toString()
        }

        btn_share = findViewById(R.id.bt_share)
        btn_share.setOnClickListener(this)

        progressBar = findViewById<ProgressBar>(R.id.progressbar) as ProgressBar

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()
        supportActionBar?.elevation = 0f

        val detail = "https://api.github.com/users/" + username
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "token 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")
        client.addHeader("User-Agent", "request")
        client.get(detail, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray,
            ) {
                progressBar!!.visibility = View.INVISIBLE
                val result = String(responseBody)
                try {
                    val data = JSONObject(result)
                    nama = data.getString("name")
                    dv_username.text = data.getString("login")
                    dv_nama.text = nama
                    Glide.with(dv_photo.context).load(data.getString("avatar_url"))
                        .into(dv_photo)
                    dv_following.text = data.getString("following")
                    dv_follower.text = data.getString("followers")
                    dv_company.text = data.getString("company")
                    dv_location.text = data.getString("location")
                    dv_blog.text = data.getString("blog")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray,
                error: Throwable
            ) {

                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error.message}"
                }
            }
        })

        favoritHelper = FavoritHelper.getInstance(applicationContext)
        favoritHelper.open()
        favorit = FavoritEntity()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.favorit_menu, menu)
        return true
    }

    override fun onClick(v: View?) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Hallo saya " + nama + " User Github")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_page -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.favorit_page -> {
                val i = Intent(this, Favorit::class.java)
                startActivity(i)
                return true
            }
            R.id.setting_page -> {
                val i = Intent(this, SettingTheme::class.java)
                startActivity(i)
                return true
            }
            R.id.favorit_tambah -> {
                if (from == "true") {

                    favorit?.title = dt_user.nama.trim()
                    favorit?.description = dt_user.username.trim()

                    val values = ContentValues()
                    values.put(DatabaseContract.noteColumns.TITLE, favorit?.title)
                    values.put(DatabaseContract.noteColumns.DESCRIPTION, favorit?.description)
                    favorit?.date = getCurrentDate()
                    values.put(DATE, getCurrentDate())
                    val result = favoritHelper.insert(values)

                    if (result > 0) {
                        favorit?.id = result.toInt()
                        setResult(RESULT_ADD, intent)

                        val favoritIntent = Intent(this, Favorit::class.java)
                        favoritIntent.putExtra(Favorit.EXTRA_STATE, favorit)
                        startActivity(favoritIntent)
                        val pesan =
                            Toast.makeText(
                                this,
                                "Berhasil Ditamahkan Ke Favorit",
                                Toast.LENGTH_LONG
                            )
                        pesan.setGravity(Gravity.TOP, 0, 0)
                        pesan.show()
                    } else {
                        Toast.makeText(this, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val pesan =
                        Toast.makeText(
                            this,
                            "Gagal Ditamahkan Ke Favorit. Username Sudah ada!",
                            Toast.LENGTH_LONG
                        )
                    pesan.setGravity(Gravity.TOP, 0, 0)
                    pesan.show()
                }
                return true
            }
            R.id.favorit_delete -> {
                if (from == "false") {
                    lifecycleScope.launch {
                        favoritHelper = FavoritHelper.getInstance(applicationContext)
                        favoritHelper.open()
                        val deferredNotes = async(Dispatchers.IO) {
                            val cursor = favoritHelper.queryAll()
                            MappingHelper.mapCursorToArrayList(cursor)
                        }
                        val favorit = deferredNotes.await()
                        if (favorit.size > 0) {

                            favoritHelper.deleteById(dt_favorit.id)
                            val favoritIntent = Intent(this@DetailActivity, Favorit::class.java)
                            favoritIntent.putExtra(Favorit.EXTRA_STATE, favorit)
                            startActivity(favoritIntent)
                            val pesan = Toast.makeText(
                                this@DetailActivity,
                                "Berhasil Dihapus dari Favorit",
                                Toast.LENGTH_LONG
                            )
                            pesan.setGravity(Gravity.TOP, 0, 0)
                            pesan.show()
                        }
                        favoritHelper.close()
                    }
                } else {
                    val pesan = Toast.makeText(
                        this@DetailActivity,
                        "Gagal Hapus data. User Belum tersimpan di Favorit",
                        Toast.LENGTH_SHORT
                    )
                    pesan.setGravity(Gravity.TOP, 0, 0)
                    pesan.show()
                }

                return true
            }
            else -> return true
        }
    }

    private fun getCurrentDate(): String? {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getMyData(): String {
        var result: String
        Log.d("cekfroms", "getMyData: " + from)
        if (from == "true") {
            result = dt_user.username
        } else {
            result = dt_favorit.description.toString()
        }
        Log.d("cfeksres", "getMyData: " + result)
        return result
    }
}