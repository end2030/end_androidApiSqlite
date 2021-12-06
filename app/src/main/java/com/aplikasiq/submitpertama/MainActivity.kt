package com.aplikasiq.submitpertama

import Users
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class MainActivity : AppCompatActivity() {
    private lateinit var rvUser: RecyclerView
    private var list = ArrayList<Users>()
    private var Apilist = ArrayList<Users>()

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "List User Github"
        progressBar = findViewById<ProgressBar>(R.id.progressbar)
        progressBar!!.visibility = View.INVISIBLE

        val pref = SettingPreferences.getInstance(dataStore)
        val mainViewModel = ViewModelProvider(this, ViewModelFactory(pref)).get(
            MainViewModel::class.java
        )
        mainViewModel.getThemeSettings().observe(this,
            { isDarkModeActive: Boolean ->
                if (isDarkModeActive) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            })
    }

    private fun showRecyclerList() {
        rvUser.layoutManager = LinearLayoutManager(this)

        var ListUserAdapter = ListUserAdapter(list)
        rvUser.adapter = ListUserAdapter
        ListUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Users) {
                showSelectedHero(data)
            }
        })

    }

    private fun showSelectedHero(user: Users) {
        val DetailIntent = Intent(this@MainActivity, DetailActivity::class.java)
        DetailIntent.putExtra(DetailActivity.EXTRA_USER, user)
        DetailIntent.putExtra(DetailActivity.FROM_SEARCH, "true")
        startActivity(DetailIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.seach_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search_menu).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                progressBar!!.visibility = View.VISIBLE
                val client = AsyncHttpClient()
                client.addHeader("Authorization", "token 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")
                client.addHeader("User-Agent", "request")
                val url = "https://api.github.com/search/users?q=" + query
                client.get(url, object : AsyncHttpResponseHandler() {
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<Header>,
                        responseBody: ByteArray,
                    ) {
                        progressBar!!.visibility = View.INVISIBLE
                        val result = String(responseBody)
                        try {
                            val responseObject = JSONObject(result)
                            val total_count = responseObject.getInt("total_count")
                            if (total_count == 0) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "User Tidak Ditemukan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            val items = responseObject.getString("items")
                            Apilist.clear()
                            val data = JSONArray(items)
                            for (i in 0 until data.length()) {
                                var jsonobject = data.getJSONObject(i)
                                var dataUser = Users(
                                    jsonobject.getString("login"),
                                    jsonobject.getString("type"),
                                    jsonobject.getString("avatar_url"),
                                    jsonobject.getString("followers_url"),
                                    jsonobject.getString("following_url")
                                )
                                Apilist.add(dataUser)
                            }
                            list.clear()
                            rvUser = findViewById(R.id.rv_user)
                            rvUser.setHasFixedSize(true)

                            list.addAll(Apilist)
                            showRecyclerList()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<Header>,
                        responseBody: ByteArray,
                        error: Throwable
                    ) {
                        progressBar!!.visibility = View.INVISIBLE
                        val errorMessage = when (statusCode) {
                            401 -> "$statusCode : Bad Request"
                            403 -> "$statusCode : Forbidden"
                            404 -> "$statusCode : Not Found"
                            else -> "$statusCode : ${error.message}"
                        }
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting_page -> {
             val i = Intent(this, SettingTheme::class.java)
               startActivity(i)
                return true
            }
            R.id.favorit_page -> {
                val i = Intent(this, Favorit::class.java)
                startActivity(i)
                return true
            }
            else -> return true
        }
    }
}