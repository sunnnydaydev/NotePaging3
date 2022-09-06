package com.example.notepaging3.page3

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.notepaging3.beans.github.Repo
import com.example.notepaging3.retrofit.service.GitHubService

/**
 * Create by SunnyDay /09/06 16:50:58
 * 1、自定义类继承PagingSource即可，注意这里有两个泛型：
 * 第一个表示页数
 * 第二个表示页面上的每一项数据
 */
class RepoPagingSource(private val gitHubService: GitHubService) : PagingSource<Int, Repo>() {

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return try {
            // 当前页数，key可能为null，为null时默认设置为第一页
            val page = params.key ?: 1
            // 获取每一页包含多少条数据
            val pageSize = params.loadSize
            val repoResponse = gitHubService.searchRepos(page, pageSize)
            val repoItems = repoResponse.items
            val preKey = if (page > 1) page - 1 else null
            val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
            //接收3个参数：数据列表，上一页，下一页。
            LoadResult.Page(repoItems, preKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}