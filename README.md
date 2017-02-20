## SeatView

### 1.	效果图

![](https://ww1.sinaimg.cn/large/006tKfTcgy1fcvsn2dtudj30ce0kydhc.jpg)



### 2. 使用

#### 布局文件

```xml
<io.github.lh911002.seatview.seat.SeatView
    android:id="@+id/select_seat_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

#### 初始化代码

```java
this.select_seat_view = (SeatView) findViewById(R.id.select_seat_view);
this.select_seat_view.setSeatClickListener(this);
this.select_seat_view.initSeatView("Github", new SeatImages(getResources()),querySeatMap());
```