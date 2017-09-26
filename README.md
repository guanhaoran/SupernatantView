##SupernatantView
- 如果我英文还可以的话这个应该叫做漂浮在上层的view---引导层
- 今天闲来无事看了网上的一些引导层案例总感觉如果不是很舒服,就是类似于很死板的显示和消失
- 我在想能不能弄点动画上去看着舒服一些 所以就有了以下的gif  gif有点短但是没办法太长了github传不上去  不知道是不是我操作不太对

![Markdown](http://i2.bvimg.com/609137/081dc854bddf65d1.gif)

- 刚开始点击屏幕是没有反应的 因为我把点击事件拦截了 后面代码可以看得到


- 因为我们产品汪没有这样的需求 所以我有的地方写的不是很严谨  包括指示箭头在下方如果下方距离不够箭头显示的时候 我也没有做判断 只是把大体的意思写出来了  大家如果想更改 可以看里边的注释 (偷偷告诉你们里边注释贼多 不怕你看不懂)


- step 1

	继承view 重写构造方法 和 onTouchEvent 和 draw 方法

 		public SupernatantView(Context context) {
      		  this(context, null);
  		  }
	
  		  public SupernatantView(Context context, AttributeSet attrs) {
   	   		  this(context, attrs, 0);
  		  }

  		  public SupernatantView(Context context, AttributeSet attrs, int defStyleAttr) {
       		 super(context, attrs, defStyleAttr);

  		  }

	onDraw 方法

			@Override
	    protected void onDraw(Canvas canvas) {
       	 super.onDraw(canvas);
    	}

	onTouchEvent方法

			 @Override
  		  public boolean onTouchEvent(MotionEvent event) {
  
		//        return super.onTouchEvent(event);
        return true;  //拦截点击事件
   		 }

- step 2

	初始化画笔方法 单独抽取出来

		private Paint createPaint(int paintColor, int alpha, int textSize, Paint.Style style, int linWidth) {
        Paint paint = new Paint();                  //初始化画笔
        paint.setAntiAlias(true);                   //抗锯齿
        paint.setDither(true);                      //防抖动
        paint.setStrokeCap(Paint.Cap.ROUND);        //笔触风格为圆角
        paint.setStrokeJoin(Paint.Join.ROUND);      //结合处形状为圆角
        paint.setColor(paintColor);                 //设置颜色
        paint.setAlpha(alpha);                      //设置透明度
        paint.setTextSize(textSize);                //设置文字大小
        paint.setStyle(style);                      //设置样式
        paint.setStrokeWidth(linWidth);             //设置边宽度
        return paint;
  		  }	


- step 3

	初始化各种数值 包括 屏幕的宽高 状态栏

	 	 public static int[] getScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return new int[]{metrics.widthPixels, metrics.heightPixels};
   		 }

		 public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
 		   }

- step 4

	一些基本的方法包括获取view 的屏幕坐标,获取view 的 RectF 具体在代码里会有详细的注释 给你先看个图片 没错就是这么详细  哦哈哈哈~~~

![Markdown](http://i1.bvimg.com/609137/39aa1853297c3c15.png)

- step 5 

	就是重写onDraw方法了 再加上计算一些动态数据之类的  代码太多就不贴出来了 大家如果感兴趣可以看看 


---

QQ 765307272