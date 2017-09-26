package supernatant.com.myapplication;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2017/9/13.
 */

public class SupernatantView extends View {
    private Paint supernatantPatin;                     //暂时只用作画Bitmap用了  改了好多需求
    private Paint guidePaint;                           //画圆圈  矩形  圆角矩形用到
    private Paint textRectPaint;                        //文字  "下一步"  画边框矩形用到
    private TextPaint textPaint;                        //文字 "下一步" 用到
    private List<View> views = new ArrayList<>();       //需要显示View 引导存放的集合
    private List<Integer> shapes = new ArrayList<>();   //需要view 显示的引导形状集合
    private float device_w;                             //屏幕宽度
    private float device_h;                             //屏幕高度
    private int flag = 0;                               //当前点击次数  用于动画
    private Context context;                            //上下文
    private int maskColor = 0x99000000;                 // 蒙版层颜色
    private Canvas mCanvas;                             //画布
    private int statusBarHeight;                        //手机状态栏高度
    private Boolean isShow = false;                     //是否显示引导层
    private Integer GUIDE_SHAPE;                        //遗弃  原用来存放当前引导层形状

    private int[] location = new int[2];                //view  在屏幕的坐标
    private Bitmap supernatantBitmap;                   //画出一个根据屏幕大小的 bitmap  用于后期   图像渲染模式所用   一共16种  可以百度  图像渲染模式

    private ValueAnimator viewAnimation;                //值动画   引导形状动画所用
    private float animatedValue;                        //值动画 存放的  0-1 的值
    private boolean isStartAnimation = false;           //动画是否开始
    private Bitmap pointerBitmap;                       //箭头形状的 bitmap
    private List<String> hintText = new ArrayList<>();  //提示文字集合
    private RectF setUpRectF;                           //下一步 外边框的 rectF
    private String setUp = "下 一 步";                 //下一步


    public SupernatantView(Context context) {
        this(context, null);
    }

    public SupernatantView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SupernatantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        //初始化画笔
        initPaint();
        //初始化一些常用值  屏幕宽高
        initNumValue();
        //重新计算屏幕宽高 上一个方法有漏洞
        cal(context);

        //状态栏高度
        statusBarHeight = SupernatantManager.getStatusBarHeight();

        //创建一个屏幕大小的bitmap
        supernatantBitmap = SupernatantManager.createBitmapSafely((int) device_w, (int) device_h, Bitmap.Config.ARGB_8888, 2);

        //初始化
        mCanvas = new Canvas(supernatantBitmap);
        mCanvas.drawColor(maskColor);

//        用于显示的箭头
        pointerBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.right);

    }

    private void initPaint() {
        supernatantPatin = new Paint(Paint.ANTI_ALIAS_FLAG);                        //Bitmap画笔(箭头)初始化操作
        supernatantPatin.setFilterBitmap(true);
        supernatantPatin.setDither(true);


        textRectPaint = createPaint(Color.WHITE, 255, 20, Paint.Style.STROKE, 2);      //"下一步" 文字外边框初始化

        guidePaint = createPaint(Color.TRANSPARENT, 0, 0, Paint.Style.FILL, 0);     //引导层形状初始化操作
        //设置边界模糊效果
        guidePaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

        textPaint = new TextPaint();                                                //提示信息文字初始化操作
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(35f);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);        //文字加粗
        textPaint.setTypeface(font);

    }

    private void initNumValue() {
        //获取屏幕信息
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        device_w = wm.getDefaultDisplay().getWidth();
        device_h = wm.getDefaultDisplay().getHeight();
    }

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawSetup();                                        //绘制 下一步

        canvas.drawBitmap(supernatantBitmap, 0, 0, null);   //画布清空

        if (isStartAnimation) {                             //是否开启动画

            animation();                                    //执行动画操作

        } else {

            drawSupernatant();                              //动画完成 绘制当前view的引导层
        }
    }

    /**
     * 画出下一步的位置
     */
    private void drawSetup() {
        setUpRectF = new RectF(device_w - 200, 20, device_w - 20, 90);  //因为 下一步这三个字不会变我也就写死了  不做动态计算了
        mCanvas.drawRect(setUpRectF, textRectPaint);                    //绘制矩形


        Rect textRectF = new Rect();
        textPaint.getTextBounds(setUp, 0, 3, textRectF);                   //计算文字的宽高
        //绘制文字到矩形的正中间
        mCanvas.drawText(setUp, setUpRectF.left + (setUpRectF.right - setUpRectF.left - textRectF.width()) / 4,
                setUpRectF.top + (setUpRectF.bottom - setUpRectF.top - textRectF.height()) / 2 + textRectF.height(), textPaint);

    }

    /**
     * 动画完成后绘制图形
     */
    private void drawSupernatant() {
        if (!isShow) {                                                  //动画是否执行完毕
            if (null != views) {                                        //views集合是否为空  一般都不 null 写在前面

                View view = views.get(flag);                            //获取当前view
                view.getLocationOnScreen(location);                     //计算view 在屏幕的位置
                int width = view.getWidth();                            //view的宽度
                int height = view.getHeight();                          //view的高度
                int max = Math.max(width, height);                      //view的宽度和高度  取最大的一个值  用来画圆形的引导层

                RectF rectF = getViewRectF(view);                       //获取当前view 的  RectF (存放的是上下左右的边距)

                guidePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));  //设置图形渲染模式  自行百度  一共16种 太麻烦了 就不详细讲了

                if (shapes.size() != 0 && shapes.size() >= flag) {      //判断当前view  用户有没有设置引导层形状 如果没有 我们自己计算
                    if (shapes.get(flag).equals(Contans.GUIDE_CIRCLE)) {//如果用户自己设置了引导层形状 判断是不是圆形

                        //绘制圆形 以当前view 的中心点作为圆形  max作为半径画圆形
                        mCanvas.drawCircle(location[0] + width / 2, (location[1] - statusBarHeight) + height / 2, max / 2, guidePaint);

                        GUIDE_SHAPE = Contans.GUIDE_CIRCLE;             //标记当前引导层形状  但是并没有用到 因为我换了一种方法做的动画

                    } else if (shapes.get(flag).equals(Contans.GUIDE_RECT)) {//如果用户自己设置了引导层形状 判断是不是矩形

                        mCanvas.drawRoundRect(rectF, 0, 0, guidePaint);      //如果圆角半径为 0 0  不就是矩形了么

                        GUIDE_SHAPE = Contans.GUIDE_RECT;

                    } else if (shapes.get(flag).equals(Contans.GUIDE_ROUND_RECT)) {//如果用户自己设置了引导层形状 判断是不是圆角矩形

                        mCanvas.drawRoundRect(rectF, 10, 10, guidePaint);           // 10 10 位圆角半径

                        GUIDE_SHAPE = Contans.GUIDE_ROUND_RECT;

                    }
                }


                if (hintText.size() != 0 && hintText.size() > flag) {      //判断用户有没有设置提示信息文字 如果没有设置则不显示
                    //当前view 的  xy 坐标
                    int x = location[0];                                        //当前view 的 X 轴坐标
                    int y = location[1];                                        //当前view 的 Y 轴坐标
                    Rect bitmapSrcRectF = SupernatantManager.getBitmapSrcRectF(pointerBitmap);      //用于画箭头的大小
                    Rect bitmapDesRectF = SupernatantManager.getBitmapDesRectF(x, y + view.getHeight(), pointerBitmap);     //用于画箭头的屏幕所在的位置

                    //为什么要保存和移动画布? 因为 默认箭头是指向右上角的  因为我只有这一个图形
                    //所以我打算进行旋转操作 因为旋转不是旋转的图形 而是旋转的画布(系统就这么规定的) \
                    // 所以要保存和移动 具体的可以百度一下

                    mCanvas.save();                                             //保存当前的 画布

                    mCanvas.translate(x + (int) (width * 2),                  //移动画布到
                            y + (bitmapDesRectF.bottom - bitmapDesRectF.top) - pointerBitmap.getHeight() / 2);  //移动到当前view 的右下角 这样旋转之后才会是在view的正下方

                    mCanvas.rotate(135);                                        //因为是在右上角 所以旋转135   正数为顺时针  负数为逆时针

                    //箭头太大 缩小 0.5 倍
                    mCanvas.scale(0.5f, 0.5f, (float) pointerBitmap.getWidth(), (float) pointerBitmap.getHeight());

                    mCanvas.drawBitmap(pointerBitmap, bitmapSrcRectF, bitmapSrcRectF, supernatantPatin);        //绘制箭头

                    mCanvas.restore();                                          //重置到最近的一次 sava   记住 restore和sava是成对出现的  否则报错

                    mCanvas.save();                                             //第二次保存 用于绘制文字

                    //这个是用于文字换行  默认文字绘制只会显示一行 用这个方法可以让文字根据你设置的最大长度进行换行
                    StaticLayout text = SupernatantManager.getStaticLayout(hintText.get(flag), textPaint, (int) device_w - x - 100);

                    //文字绘制你需要先移动画布 再能在只能位置进行绘制  所以刚才我们要保存
                    //y坐标 + view 高度 + 箭头缩小后的高度
                    mCanvas.translate(x, y + height + pointerBitmap.getHeight() / 2);

                    text.draw(mCanvas);                                         //文字绘制

                    mCanvas.restore();                                          //画布重置

                    // TODO: 2017/9/25 显示文字 在提示信息下面 "我知道了" 太麻烦  不想写了  投机取巧 在右上角放一个下一步  大体意思知道就行  以下是完成一半的效果哈哈哈哈
//                RectF rectF1 = new RectF(x + width1/4,bitmapDesRectF.bottom+height1+height+20,x+width1/4*3,bitmapDesRectF.bottom+height1+height+50);
//                mCanvas.drawRoundRect(rectF1,0,0,textRectPaint);
//                mCanvas.drawText("我知道了",rectF1.right - rectF1.left/3,rectF1.bottom-10,textPaint);*/

                    guidePaint.setXfermode(null);
                }
            }
        }
    }

    /**
     * 开始动画
     */
    private void animation() {

        GUIDE_SHAPE = Contans.GUIDE_CIRCLE;                             //测试时候用的 没什么用

        isResetOriginalState(false);                                    //是否重置引导层

        View startView = views.get(flag - 1);                           //获取上一个view

        View endView = null;
        RectF endViewRectF = null;

        if (views.size() > 2) {
            endView = views.get(flag);                                  //获取下一个view  做动画
            endViewRectF = getViewTopRectF(endView, shapes.get(flag));  //根据 引导层形状   获取结束view的 rectF
        }


        RectF startRectF = getViewTopRectF(startView, shapes.get(flag - 1));//根据传入的View  获取当前需要展示的[引导层形状

        //获取开始view 的引导层形状 到 结束view引导层形状 根据值动画animatedValue 进行渐变 返回一个新的 rectF  (conversion)
        RectF conversion = new RectF();
        conversion.left = startRectF.left + (endViewRectF.left - startRectF.left) * animatedValue;
        conversion.top = startRectF.top + (endViewRectF.top - startRectF.top) * animatedValue;
        conversion.right = startRectF.right + (endViewRectF.right - startRectF.right) * animatedValue;
        conversion.bottom = startRectF.bottom + (endViewRectF.bottom - startRectF.bottom) * animatedValue;

        //
        drawRectFAnima(conversion, startView, shapes.get(flag - 1), shapes.get(flag));

    }

    /**
     * 根据传入的rectF 和 开始的view 结束的view  进行过渡效果绘制
     *
     * @param rectF      过渡用的rectF
     * @param startView  开始的view
     * @param startShape 开始引导层形状
     * @param endShape   结束引导层形状
     */
    private void drawRectFAnima(RectF rectF, View startView, Integer startShape, Integer endShape) {
        float startMax = 0;                                                     //如果开始或者结束时引导层是圆形 则需要计算圆的半径
        float endMax = 0;


        //本来打算三元表达式 但是怕后期写注释 还得解释 我就注释掉了  大体是下面这个 我也没做测试 就是手写出来没报错误 我就放上来了  大致意思就是这个 哈哈哈
//        startMax =startShape.equals(Contans.GUIDE_CIRCLE)?Math.max(startView.getWidth(), startView.getHeight()):
//                startShape.equals(Contans.GUIDE_RECT)?0:5;
//        endMax =startShape.equals(Contans.GUIDE_CIRCLE)?Math.max(startView.getWidth(), startView.getHeight()):
//                startShape.equals(Contans.GUIDE_RECT)?0:5;


        if (startShape.equals(Contans.GUIDE_CIRCLE)) {                          //如果开始的view 引导层是圆形 则计算圆形半径

            startMax = Math.max(startView.getWidth(), startView.getHeight());   //半径取值为 宽或者高的其中最大值  下同

        } else if (startShape.equals(Contans.GUIDE_RECT)) {                     //如果为矩形 则置为0

            startMax = 0;

        } else if (startShape.equals(Contans.GUIDE_ROUND_RECT)) {               //如果是圆角矩形则置为 5

            startMax = 5;

        }
        if (endShape.equals(Contans.GUIDE_CIRCLE)) {                            //如果结束view  的引导层是圆形 则计算结束view 引导层的圆的半径

            endMax = Math.max(startView.getWidth(), startView.getHeight());     //结束view 引导层圆的半径

        } else if (endShape.equals(Contans.GUIDE_RECT)) {

            endMax = 0;

        } else if (endShape.equals(Contans.GUIDE_ROUND_RECT)) {

            endMax = 5;

        }

        //根据rectF  就可以计算动画绘制的最终效果   如果结束view 引导层是圆形 则用drawRoundRect宽高都是 startMax  圆角半径是startMax  则效果就是圆形
        //这也是投机取巧的一种 因为用drawCircle  太麻烦  效果跟这个一样 所以索性用这个  代码也少了好多
        mCanvas.drawRoundRect(rectF, startMax + (endMax - startMax) * animatedValue, startMax + (endMax - startMax) * animatedValue, guidePaint);
    }

    /**
     * 根据传入的view 和形状  计算rectF的值
     *
     * @param view  传入的view
     * @param shape 要在view 上画的图形 (圆 矩 圆角矩)
     * @return
     */
    private RectF getViewTopRectF(View view, Integer shape) {
        RectF ViewRectF = getViewRectF(view);
        view.getLocationOnScreen(location);
        int startWidth = view.getWidth();
        int startHeight = view.getHeight();
        int max = Math.max(startWidth, startHeight);                         //计算圆  直径


        if (shape.equals(Contans.GUIDE_CIRCLE)) {                           //引导层是圆形

            location[0] = location[0] + startWidth / 2;                     //获取当前view 的中心点 因为 location是view的左上角
            location[1] = location[1] + startHeight / 2 - statusBarHeight;  //其中高度 包括了 状态栏 需要 减去

            ViewRectF.left = location[0] - max / 2;                         //按照半径上下左右同时扩大半径的距离
            ViewRectF.top = location[1] - max / 2;
            ViewRectF.right = location[0] + max / 2;
            ViewRectF.bottom = location[1] + max / 2;

        } else if (shape.equals(Contans.GUIDE_RECT)) {

        } else if (shape.equals(Contans.GUIDE_ROUND_RECT)) {


        }
        return ViewRectF;                                                   //如果是矩形和圆角矩形则不需要改变

    }


    /**
     * 获取view 所在的 rectF
     * 吧上下左右  - 10   + 10  是让他有一个间距
     *
     * @param view
     * @return
     */
    private RectF getViewRectF(View view) {
        RectF rectF = new RectF();

        rectF.left = view.getLeft() - 10;
        rectF.top = view.getTop() - 10;
        rectF.right = view.getRight() + 10;
        rectF.bottom = view.getBottom() + 10;

        return rectF;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();//获取当前点击X坐标
                float y = event.getY();//获取当前点击Y坐标
                //判断点击的位置是否是 下一步 的 位置
                if (x >= setUpRectF.left && x <= setUpRectF.right && y >= setUpRectF.top && y <= setUpRectF.bottom) {
                    //判断是否动画结束
                    if (!isStartAnimation) {
                        //判断集合 是否符合点击  防止索引越界
                        if (views.size() != flag + 1) {
                            flag++;
                            isShow = false;
                            isResetOriginalState(isShow);
                            startAnimator();

                        } else {  ///点击结束  引导层消失显示正常布局
                            isShow = true;
                            isStartAnimation = false;
                            isResetOriginalState(isShow);
                            dissmis();
//                            flag = 0;
//                            isShow = false;
//                            animatedValue = 0;
//                            isStartAnimation = false;
//                            isResetOriginalState(isShow);
                        }
                        invalidate();
                    }
                }
                break;
        }
//        return super.onTouchEvent(event);
        return true;  //拦截点击事件
    }


    public void startAnimator() {
        viewAnimation = new ValueAnimator().ofFloat(0, 1);                  //创建一个值动画
        viewAnimation.setDuration(1000);                                    //动画事件 1s
        viewAnimation.setInterpolator(new LinearInterpolator());            //设置插值器
        viewAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {                    //动画监听
                animatedValue = (float) animation.getAnimatedValue();       //获取当前值动画的 没毫秒的值  0 - 1
                isStartAnimation = animatedValue == 1 ? false : true;       //判断动画是否结束
                invalidate();                                               //更新view

            }
        });
        viewAnimation.start();                                              //动画开始
    }

    /**
     * 传入需要进行引导的viewID
     *
     * @param view
     * @return
     */
    public SupernatantView setShowView(View... view) {
        views.clear();
        for (View view1 : view) {
            views.add(view1);
        }
        return this;
    }

    /***
     * 设置每个view 展示的 形状 如果不设置 或者 设置的形状比传入的 view少 则默认显示适当的形状(根据view 的位置)
     *
     * @param showShape
     * @return
     */
    public SupernatantView setShowShape(Integer... showShape) {
        shapes.clear();
        for (int i : showShape) {
            shapes.add(i);
        }

        fillShpaes();
        return this;
    }

    /**
     * 设置提示用户文字,如果不写则什么都不显示
     *
     * @param strings
     * @return
     */
    public SupernatantView setHintText(String... strings) {
        hintText.clear();
        for (String string : strings) {
            hintText.add(string);
        }
        return this;
    }

    public void show() {
        FrameLayout contentParent =
                (FrameLayout) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        contentParent.removeView(this);
        contentParent.addView(this);
    }

    public void dissmis() {
        FrameLayout contentParent =
                (FrameLayout) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        contentParent.removeView(this);
    }

    /**
     * 如果显示的形状比view 图形少 则自动判断填充
     */
    private void fillShpaes() {
        if (views.size() > shapes.size()) {
            View view = null;
            int width, height, max, top, right, bottom, left;
            int dSize = views.size() - shapes.size();
            for (int i = 0; i < dSize; i++) {
                view = views.get(shapes.size() + i);
                view.getLocationOnScreen(location);
                width = view.getWidth();
                height = view.getHeight();
                max = Math.max(width, height);

                top = view.getTop();
                right = view.getRight();
                bottom = view.getBottom();
                left = view.getLeft();

                if (left + width / 2 >= max &&
                        top + height / 2 >= max &&
                        device_w - right + width / 2 >= max &&
                        device_h - statusBarHeight - bottom + height / 2 >= max) {
                    shapes.add(Contans.GUIDE_CIRCLE);
                } else {
                    shapes.add(Contans.GUIDE_RECT);
                }
            }
        }
    }

    /**
     * 是否重置到初始状态
     * true 无浮层
     * false 有浮层
     *
     * @param b
     */
    public void isResetOriginalState(boolean b) {
        guidePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(guidePaint);
        if (!b)
            mCanvas.drawColor(maskColor);
    }


    /**
     * 计算参数
     *
     * @param context
     */
    private void cal(Context context) {
        int[] screenSize = SupernatantManager.getScreenSize(context);
        device_w = screenSize[0];
        device_h = screenSize[1];
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            statusBarHeight = 44;
        }
    }

}
