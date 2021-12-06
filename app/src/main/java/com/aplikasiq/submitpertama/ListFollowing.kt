package com.aplikasiq.submitpertama

import Users
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListFollowing : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var rvFollowing: RecyclerView
    private var list = ArrayList<Users>()
    private var Apilist = ArrayList<Users>()
    private var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_following, container, false)
        progressBar = view.findViewById<ProgressBar>(R.id.progressbar) as ProgressBar

        val activity = activity as DetailActivity?
//        val myDataFromActivity = activity!!.getMyData()
        var following = ""
        if (activity != null) {
            if (activity.getMyData() != null){
                val myDataFromActivity = activity.getMyData()
                if (myDataFromActivity != null) {
                    following = "https://api.github.com/users/" + myDataFromActivity + "/following"
                }
            }
        }

        val client = AsyncHttpClient()
        client.addHeader("Authorization", "token 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")
        client.addHeader("User-Agent", "request")
        client.get(following, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray,
            ) {
                progressBar!!.visibility = View.INVISIBLE
                val result = String(responseBody)
                try {
                    val data = JSONArray(result)
                    Apilist.clear()
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
                    rvFollowing = view.findViewById<RecyclerView>(R.id.rv_following)
                    rvFollowing.setHasFixedSize(true)

                    list.addAll(Apilist)
                    showRecyclerList()
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
        return view
    }

    private fun showRecyclerList() {
        rvFollowing.layoutManager = LinearLayoutManager(activity)
        var ListUserAdapter = ListUserAdapter(list)
        rvFollowing.adapter = ListUserAdapter
        ListUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Users) {
                showSelectedHero(data)
            }
        })
    }

    private fun showSelectedHero(user: Users) {
        Toast.makeText(activity, user.username, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ListFollowing().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}