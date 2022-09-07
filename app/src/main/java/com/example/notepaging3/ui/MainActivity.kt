package com.example.notepaging3.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepaging3.R
import com.example.notepaging3.page3.adapter.RepoAdapter
import com.example.notepaging3.vm.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    private val repoAdapter = RepoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = repoAdapter
        }

        lifecycleScope.launch {
            viewModel.getPagingData().collect {
                repoAdapter.submitData(it)//触发分页的核心功能
            }
        }
        repoAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading ->{
                    progress_bar.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                }
                is LoadState.Loading ->{
                    progress_bar.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }
                is LoadState.Error ->{
                    val state = it.refresh as LoadState.Error
                    progress_bar.visibility = View.GONE
                    Toast.makeText(this, "Load Error: ${state.error.message}", Toast.LENGTH_SHORT).show()
                    Log.d("My-TAG","Load Error: ${state.error.message}")
                }
            }
        }

    }
}