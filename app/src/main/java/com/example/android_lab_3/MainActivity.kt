package com.example.android_lab_3
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var searchButton: Button
    private lateinit var keywordEditText: EditText
    private lateinit var progressBar: ProgressBar

    private var newsList: MutableList<News> = mutableListOf()
    private var currentPage = 1
    private var isLoading = false

    companion object {
        private const val BASE_URL = "https://newsdata.io/api/1"
        private const val API_KEY = "pub_35368f36bf4337e4d700b7247eda34bd5b013"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchButton = findViewById(R.id.searchButton)
        keywordEditText = findViewById(R.id.keywordEditText)
        progressBar = findViewById(R.id.progressBar)

        adapter = NewsAdapter(newsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {
            currentPage = 1
            newsList.clear()
            searchNews()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && currentPage < 10) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++
                        searchNews()
                    }
                }
            }
        })
    }

    private fun searchNews() {
        isLoading = true
        showProgressBar()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsApi = retrofit.create(NewsApi::class.java)
        val keyword = keywordEditText.text.toString()

        val call = newsApi.getNews(API_KEY, keyword, currentPage)
        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    if (newsResponse?.results != null) {
                        newsList.addAll(newsResponse.results)
                        adapter.notifyDataSetChanged()
                    }
                }
                hideProgressBar()
                isLoading = false
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                hideProgressBar()
                isLoading = false
            }
        })
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    fun openNews(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}