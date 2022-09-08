
###### 1、接口信息

```kotlin
https://api.github.com/search/repositories?sort=stars&q=Android&per_page=5&page=1
```

这个接口会返回GitHub上所有Android相关的开源库，以Star数量排序。如上有两个重要的参数：

- page：表示当前请求的是第几页
- per_page：表示每页返回5条数据

这个接口请求的数据返回信息较多，我们梳理下，选取想要的信息：

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

###### Paging3核心组件

Paging 3有几个非常关键的核心组件，我们需要分别在这几个核心组件中按部就班地实现分页逻辑。


