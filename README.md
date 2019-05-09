# AndroidLock9View
Android版的九宫格(手势)，可在xml设置View属性，支持横竖屏、宽高限制

## 示例截图
<div align="center">
<img src="https://github.com/kenvin-workpace/AndroidLock9View/blob/master/screenshot/device-2019-05-09-154628.png" height="20%" width="20%" >
<img src="https://github.com/kenvin-workpace/AndroidLock9View/blob/master/screenshot/device-2019-05-09-154748.png" height="20%" width="20%" >
<img src="https://github.com/kenvin-workpace/AndroidLock9View/blob/master/screenshot/device-2019-05-09-154805.png" height="20%" width="20%" >
<img src="https://github.com/kenvin-workpace/AndroidLock9View/blob/master/screenshot/device-2019-05-09-154825.png" height="20%" width="20%" >
</div>

## attrs属性配置
```
<declare-styleable name="Lock9View">
        <!--圆圈&连接线失败颜色-->
        <attr name="circleColorFailed" format="color"/>
        <!--圆圈&连接线成功颜色-->
        <attr name="circleColorSuccess" format="color"/>
        <!--圆圈默认颜色-->
        <attr name="circleColorDefault" format="color"/>
        <!--圆圈&连接线选中颜色-->
        <attr name="circleColorSelected" format="color"/>
        <!--连接的宽度-->
        <attr name="lineWidth" format="dimension"/>
        <!--圆圈的宽度-->
        <attr name="circleLineWidth" format="dimension"/>
        <!--密码长度-->
        <attr name="passwordLength" format="integer"/>
</declare-styleable>
```
## View方法
```
// 重置手势
public void reset()
// 监听手势
public void setLock9ViewListener(ILock9ViewListener listener)
```
## 示例代码
```
// 成员变量
private TextView mTvReset;
private Lock9View mLock9View;
private String pwd; //伪代码，根据自己的业务逻辑来
```
1. 通过资源ID找到View
```
findViewById(R.id.tv_reset);
findViewById(R.id.lock9view);
```
2. 设置View的点击事件
```
// 监听手势
mLock9View.setLock9ViewListener(new Lock9View.ILock9ViewListener() {

    @Override
    public void onSuccess(String password) {
        Toast.makeText(MainActivity.this, MainActivity.this.pwd != null ? "登录成功" : "设置成功", Toast.LENGTH_SHORT).show();
        pwd = password;
    }

    @Override
    public boolean comparePassWord(String password) {
        return pwd != null && pwd.equals(password);
    }

    @Override
    public boolean isSettingPassWord() {
        return pwd == null;
    }

    @Override
    public void onFailed(int passwordLength) {
        Toast.makeText(MainActivity.this, passwordLength < 4 ? "请至少连接4个圆" : "登录失败", Toast.LENGTH_SHORT).show();
    }
});

// 重置手势
mTvReset.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mLock9View.reset();
    }
});
```
## XML布局
```
<com.hongzhenw.view.Lock9View
android:id="@+id/lock9view"
android:layout_width="match_parent"
android:layout_height="match_parent"
app:circleColorDefault="@android:color/darker_gray"
app:circleColorFailed="@color/colorAccent"
app:circleColorSelected="@android:color/darker_gray"
app:circleColorSuccess="@color/colorPrimary"
app:circleLineWidth="5dp"
app:lineWidth="1dp"
app:passwordLength="4"/>
```
