# deprecated

# 控件介绍

该控件主要行为模式是模仿`SwipeRefreshLayout`类，只要在`ListView`或`RecyclerView`外套上该控件，便可以轻松使用。请注意：只有在手动下拉刷新时`onRefresh()`方法才会被回调。这一点和`SwipeRefreshLayout`是保持一致的。

XML中
```xml
<com.bzh.refresh.RefreshLayout
	android:id="@+id/refreshLayout"
	android:layout_width="match_parent"
	android:layout_height="0dp"
	android:layout_weight="1">

	<ListView
		android:id="@+id/listView"
		android:layout_width="match_parent"
		android:layout_height="match_parent"></ListView>

</com.bzh.refresh.RefreshLayout>

```

代码中
```java
final RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);

refreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
	@Override
	public void onRefresh() {
		Toast.makeText(MainActivity.this, "刷新回调被调用了", Toast.LENGTH_SHORT).show();
	}
});
```

# RefreshLayout思路

在`RefreshLayout`中，其职责非常明确:

1. 手指移动超过一定值，就拦截事件，并根据手指移动的距离来对`RefreshInnerLayout`的高度和对`ListView`或`RecyclerView`的(通过`setTransitionY()`)位置进行调整。
2. 如果`RefreshView`尚未完全显示，那么执行回弹动画
3. 如果`RefreshView`完全显示，并且超出其高度，然后松手，那么回弹到`RefreshView`的高度，然后通知`RefreshView`执行下一步动画切换。
4. 如果设置`setRefreshing(false)`，那么执行回弹到顶部的操作。

# RefreshView动画效果思路

根据设计的动画效果可以将整个动画分为6步

1. 画一个初始的默认矩形
2. 矩形向两侧扩展为两个梯形
3. 两个梯形由中间向外侧缩减
4. 绘制"M"的两个"腿"和两个"臂"(由无到有的动画效果)
5. 缩减"M"的两个"腿"到两个正方形小点，缩减两个"臂"到一个正方形小点
6. 初始化Loading的三个正方形小点的位置，然后依次更改其Y轴位置达到加载效果

整个动画的思路就是这样子，该动画编码的过程中，使用到的知识点也比较少（e.g:Path Rect 等）都是简单的绘制方法，其真正麻烦的在于很多角度需要计算，使用到了很多Math函数来计算角度/底边/斜边。

在Loading的动画效果中，使用了值动画(ValueAnimation)，让三个点依次执行值动画，然后不断的更改其Y轴位置和重绘。

动画效果1

![](https://github.com/biezhihua/YYTM/raw/master/gif/演示1.gif)

动画效果2

![](https://github.com/biezhihua/YYTM/raw/master/gif/演示2.gif)

动画效果3

![](https://github.com/biezhihua/YYTM/raw/master/gif/演示3.gif)

效果视频在gif目录下，大家可以下载查看。
