# AndroidKeyboard

> 在做一些人性化的交互设计的时候，能够获取Android 键盘的显示与隐藏状态对我们有很大的帮助，但是Android 官方文档中并没有明确的给出 ,键盘显示与隐藏的监听，但是我们还是可以通过计算当前应用的高度变化来获取键盘的状态。手机QQ对于键盘的处理就非常好，有着以下的几点人性化设计：

![QQ效果图](http://i4.buimg.com/d16b180723bbcfcd.gif)

* 滑动时隐藏键盘
* 点击除键盘区域隐藏键盘
* 当滚动到底部时继续拉动弹出键盘

** 本文将一步一步讲解实现上述三种设计 **

#### 1.首先获取键盘显示隐藏状态

获取键盘显示状态的原理是：首先利用getRootView().getHeight() 获取屏幕高度，在利用getWindowVisibleDisplayFrame 获取应用显示区域，但是这个区域不包含虚拟按键的区域（虚拟键盘、手机底部虚拟按键），我们根据前者与后者之差与状态栏高度的比较来判断键盘的显示与隐藏状态。
	 
	 // 软键盘的显示状态
    private boolean ShowKeyboard;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            // 应用可以显示的区域。此处包括应用占用的区域，包括标题栏不包括状态栏
            Rect r = new Rect();
            layoutMain.getWindowVisibleDisplayFrame(r);
            // 键盘最小高度
            int minKeyboardHeight = 150;
            // 获取状态栏高度
            int statusBarHeight = getStatusBarHeight(mContext);
            // 屏幕高度,不含虚拟按键的高度
            int screenHeight = layoutMain.getRootView().getHeight();
            // 在不显示软键盘时，height等于状态栏的高度
            int height = screenHeight - (r.bottom - r.top);


            if (ShowKeyboard) {
                // 如果软键盘是弹出的状态，并且height小于等于状态栏高度，
                // 说明这时软键盘已经收起
                if (height - statusBarHeight < minKeyboardHeight) {
                    ShowKeyboard = false;
                    Toast.makeText(mContext,"键盘隐藏了",Toast.LENGTH_SHORT).show();
                }
            } else {
                // 如果软键盘是收起的状态，并且height大于状态栏高度，
                // 说明这时软键盘已经弹出
                if (height - statusBarHeight > minKeyboardHeight) {
                    ShowKeyboard = true;
                    Toast.makeText(mContext,"键盘显示了",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    
** 需要特别指出的是，在某些手机上，比如华为mate8，底部会出现一个可以随时隐藏与显示的一行虚拟按键（Android杂乱生态的无奈🙂），所以我们要定义一个最小键盘高度。 ** 
	
	//给最外层布局添加布局变化监听
	layoutMain.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);


这样根据ShowKeyboard值的变化 就能判断键盘的显示与隐藏了。
### 2.实现点击其他区域隐藏键盘
这里利用的就是焦点，点击其他区域，键盘会失去焦点，这个时候我们可以强制隐藏键盘，因为EditText 焦点已经缺失，某些隐藏键盘的方法可能失效。

	        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layoutMain.getWindowToken(), 0);
                }
            }
        });
      
### 3.实现滑动隐藏键盘，以及滑动到底部拉出键盘

这里主要实现方法是监听用户手势，同时判断键盘状态，判断webView是否滑动到底部了。
	
	@Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        //判断滑动距离，以及速度，如果键盘显示中，隐藏键盘。
        if (Math.abs(e2.getY() - e1.getY()) > flingHeight && Math.abs(velocityY) > flingspeed) {
            if (ShowKeyboard) {
                closeInputMethod(mContext);
            }
        }
        float r= mWebView.getHeight();
        // WebView总高度
        float webViewContentHeight = mWebView.getContentHeight()*mWebView.getScale();
        // WebView的现高度
        float webViewCurrentHeight = (mWebView.getHeight() + mWebView.getScrollY());
		// 这里是判断 键盘是否显示 滑动距离，以及是否滑动到底部。
        if (!ShowKeyboard && e1.getY() - e2.getY() > getScreenHeight(mContext)  / 10 && webViewContentHeight - webViewCurrentHeight <= 10) {

            popInputMethod(inputText, mContext);
        }
        return false;
    }
    
 注意手势监听，不要遗忘以下操作：

	public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener 
	
	mGestureDetector = new GestureDetector(mContext, this);

	 @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }
   
最后看一下实现的效果图：
![实现效果图](http://i4.buimg.com/e6674a91804df4e4.gif)


### <font color ="red"> 源码地址：[AndroidKeyboard](https://github.com/zzzlw/AndroidKeyboard) 

###  技术博客：[Wells'Note](http://www.zhangliwei.date) </font>
