package com.aplikasiq.submitpertama

import FavoritEntity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aplikasiq.submitpertama.databinding.ActivityFavoritBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Favorit : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritBinding
    private lateinit var rvFavorit: RecyclerView
    private var list = ArrayList<FavoritEntity>()


    companion object {
        const val EXTRA_STATE = "EXTRA_STATE"

        const val ALERT_DIALOG_CLOSE = 10

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorit)

        binding = ActivityFavoritBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Favorit List"
            loadNotesAsync()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.setting_menu, menu)
        supportActionBar?.title = "List Favorit"
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_page -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                return true
            }
             R.id.setting_page -> {
             val i = Intent(this, SettingTheme::class.java)
               startActivity(i)
                return true
            }
            else -> return true
        }
    }

    private fun loadNotesAsync() {
        lifecycleScope.launch {
            binding.progressbar2.visibility = View.VISIBLE
            val favoritHelper = FavoritHelper.getInstance(applicationContext)
            favoritHelper.open()
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = favoritHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            binding.progressbar2.visibility = View.INVISIBLE
            val favorit = deferredNotes.await()
            if (favorit.size > 0) {
                Log.d("cekadapter", "onCreate:"+favorit)
                rvFavorit = findViewById(R.id.rv_favorit)
                rvFavorit.setHasFixedSize(true)

                list.addAll(favorit)
                showRecyclerList()

            } else {
                val pesan = Toast.makeText(this@Favorit,"Daftar Favorit Kosong",Toast.LENGTH_LONG)
                pesan.setGravity(Gravity.TOP,0,0)
                pesan.show()
            }
            favoritHelper.close()
        }
    }

    private fun showRecyclerList() {
        rvFavorit.layoutManager = LinearLayoutManager(this)

        var listFavoritAdapter = ListFavoritAdapter(list)
        rvFavorit.adapter = listFavoritAdapter
        listFavoritAdapter.setOnItemClickCallback(object : ListFavoritAdapter.OnItemClickCallback {
            override fun onItemClicked(data: FavoritEntity) {
                showSelectedFavorit(data)
            }
        })

    }
    private fun showSelectedFavorit(favorit: FavoritEntity) {
        val DetailIntent = Intent(this, DetailActivity::class.java)
        DetailIntent.putExtra(DetailActivity.EXTRA_FAVORIT, favorit)
        DetailIntent.putExtra(DetailActivity.FROM_SEARCH, "false")
        startActivity(DetailIntent)
    }
    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }
    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Note"
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } 
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}