# Paging3

简介

一款分页库，数据源可直接来源网络或者数据库。这款分页库必须结合RecyclerView使用。这个框架的宗旨是契合安卓推荐的应用架构
，流畅的集成其他jetpack组件，提供一流的kotlin支持。

优势

- 分页数据的内存中缓存
- 内置的请求重复信息删除功能
- 可配置的RecyclerView适配器：划到底部自动加载数据。
- 支持kotlin#flow、Rxjava、LiveData
- 内置对错误处理功能的支持：包括刷新和重试功能。

依赖

```groovy
dependencies {
  def paging_version = "3.1.1"
  // must lib
  implementation "androidx.paging:paging-runtime:$paging_version"
  // optional - RxJava2 support
  implementation "androidx.paging:paging-rxjava2:$paging_version"
  // optional - RxJava3 support
  implementation "androidx.paging:paging-rxjava3:$paging_version"
  // optional - Guava ListenableFuture support
  implementation "androidx.paging:paging-guava:$paging_version"
  // optional - Jetpack Compose integration
  implementation "androidx.paging:paging-compose:1.0.0-alpha16"
}
```

[官方文档](https://developer.android.google.cn/topic/libraries/architecture/paging/v3-overview)

[参考文章](https://blog.csdn.net/guolin_blog/article/details/114707250?spm=1001.2014.3001.5506)