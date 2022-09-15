package com.example.notepaging3.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.notepaging3.beans.github.Repo
import com.example.notepaging3.page3.RepoPagingSource
import com.example.notepaging3.retrofit.service.GitHubService
import kotlinx.coroutines.flow.Flow

/**
 * Create by SunnyDay /09/06 17:42:19
 */
object Repository {
    private const val PAGE_SIZE = 50
    private val gitHubService = GitHubService.create()
    fun getPageData(): Flow<PagingData<Repo>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            RepoPagingSource(gitHubService)
        }).flow
    }

}