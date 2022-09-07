package com.example.notepaging3.retrofit.service

import com.example.notepaging3.beans.github.RepoResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Create by SunnyDay /09/06 16:36:12
 */
interface GitHubService {

    /**
     * todo:作者这里的代码可以转化为单例。跑起来后可以自己优化下
     * */
    companion object {
        private const val BASE_URL = "https://api.github.com/"
        fun create(): GitHubService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubService::class.java)
        }
    }
    /**
     * 请求分页数据
     * @param page     请求哪一页（如请求第一页）
     * @param perPage  请求页对应的数据（如请求第一页，请求5条数据）
     * */
    @GET("search/repositories?sort=stars&q=Android")
    suspend fun searchRepos(@Query("page") page: Int, @Query("per_page") perPage: Int): RepoResponse
}