
###### 1、接口信息

```kotlin
https://api.github.com/search/repositories?sort=stars&q=Android&per_page=5&page=1
```

这个接口会返回GitHub上所有Android相关的开源库，以Star数量排序。如上有两个重要的参数：

- page：表示当前请求的是第n页
- per_page：表示每页返回m条数据

这个接口请求的数据返回信息较多，我们整理下，选取想要的字段信息：

```json lines
{
  "items": [
    {
      "id": 31792824,
      "name": "flutter",
      "description": "Flutter makes it easy and fast to build beautiful apps for mobile and beyond.",
      "stargazers_count": 112819,
    },
    {
      "id": 14098069,
      "name": "free-programming-books-zh_CN",
      "description": ":books: 免费的计算机编程类中文书籍，欢迎投稿",
      "stargazers_count": 76056,
    },
    {
      "id": 111583593,
      "name": "scrcpy",
      "description": "Display and control your Android device",
      "stargazers_count": 44713,
    },
    {
      "id": 12256376,
      "name": "ionic-framework",
      "description": "A powerful cross-platform UI toolkit for building native-quality iOS, Android, and Progressive Web Apps with HTML, CSS, and JavaScript.",
      "stargazers_count": 43041,
    },
    {
      "id": 55076063,
      "name": "Awesome-Hacking",
      "description": "A collection of various awesome lists for hackers, pentesters and security researchers",
      "stargazers_count": 42876,
    }
  ]
}

```

根据json信息写实体类

```kotlin
/**
 * Create by SunnyDay /09/06 16:31:43
 */
data class RepoResponse(
    @SerializedName("items")
    val items: List<Repo> = emptyList()
)
```

```kotlin
/**
 * Create by SunnyDay /09/06 16:32:35
 */
data class Repo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("stargazers_count") val starCount: Int
)
```

最后就是网络接口请求的封装了，使用经典的retrofit框架：
```groovy
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
```

```kotlin
/**
 * Create by SunnyDay /09/06 16:36:12
 */
interface GitHubService {
    
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
     * 请求分页数据。
     * @param page     请求哪一页（如请求第一页）
     * @param perPage  请求页对应的数据（如请求第一页，请求5条数据）
     * */
    @GET("search/repositories?sort=stars&q=Android")
    suspend fun searchRepos(@Query("page") page: Int, @Query("per_page") perPage: Int): RepoResponse
}
```

###### 2、Paging3核心组件

Paging 3有几个非常关键的核心组件，我们需要分别在这几个核心组件中按部就班地实现分页逻辑。

（1）PagingSource

最重要的组件就是PagingSource，我们需要自定义一个子类去继承PagingSource，然后重写load()函数，并在这里提供对应当前页数的数据。

```kotlin
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
```

好了，接下来要提供获取数据的方法，按照mvvm我们需要搞个Repository

```kotlin
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
```

将Repository编写完成之后，我们还需要再定义一个ViewModel，因为Activity是不可以直接和Repository交互的，要借助ViewModel才可以。新建一个MainViewModel类：

```kotlin
/**
 * Create by SunnyDay /09/06 17:58:10
 */
class MainViewModel : ViewModel() {
    /**
     * cachedIn用于将服务器返回的数据在viewModelScope这个作用域内进行缓存，
     * 假如手机横竖屏发生了旋转导致Activity重新创建，Paging 3就可以直接读取缓存中的数据，而不用重新发起网络请求了。
     * */
    fun getPagingData(): Flow<PagingData<Repo>> {
        return Repository.getPageData().cachedIn(viewModelScope)
    }
}
```

ojbk 代码完成一大半了，在业务逻辑处调用下getPagingData方法即可，这时便会通过Repository调用RepoPagingSource内的数据了。

RepoPagingSource内部主要做了几件事：

- 请求网络数据
- 自动load数据（需要结合RecyclerView）

（2）PagingDataAdapter

好了，上述中我们知道paging3需要结合RecyclerView使用。下面我们定义一个RecyclerView的子项布局。新建repo_item.xml，代码如下所示：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="10dp"
    android:orientation="vertical">

    <TextView
        android:id="@+id/name_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:maxLines="1"
        android:ellipsize="end"
        android:textColor="#5194fd"
        android:textSize="20sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/description_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:maxLines="10"
        android:ellipsize="end" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:gravity="end"
        tools:ignore="UseCompoundDrawables">

        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginEnd="5dp"
            android:src="@drawable/ic_star"
            tools:ignore="ContentDescription" />

        <TextView
            android:id="@+id/star_count_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical" />

    </LinearLayout>

</LinearLayout>
```
接下来定义RecyclerView的适配器，但是注意，这个适配器也比较特殊，必须继承自PagingDataAdapter

```kotlin
/**
 * Create by SunnyDay /09/06 18:07:03
 *
 * 使用paging3时RecyclerView的适配器必须使用PagingDataAdapter
 * PagingDataAdapter构造必须传递一个DiffUtil.ItemCallback用来管理数据变化
 */
class RepoAdapter: PagingDataAdapter<Repo, RepoAdapter.ViewHolder>(COMPARATOR) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Repo>() {
            override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                return oldItem == newItem
            }
        }
    }

    /**
     * 注意这里获取数据源的写法，常规的是从List集合中获取，这个list从外部传递。
     * 这里使用了getItem直接获取differ中数据
     * */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo = getItem(position)
        if (repo != null) {
            holder.name.text = repo.name
            holder.description.text = repo.description
            holder.starCount.text = repo.starCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repo_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name_text)
        val description: TextView = itemView.findViewById(R.id.description_text)
        val starCount: TextView = itemView.findViewById(R.id.star_count_text)
    }

}
```

####### 3、使用

修改activity_main.xml布局，在里面定义一个RecyclerView和一个ProgressBar：

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />

</FrameLayout>
```

然后修改MainActivity中的代码，如下所示：

```kotlin
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
```

