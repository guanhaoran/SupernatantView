package supernatant.com.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by pc on 2017/9/26.
 */

public class SupernatantManager {

    /**
     * 让绘制的文字自动换行
     *
     * @param text      需要绘制的文字
     * @param textPaint 文字画笔
     * @param textWidth 一行文字的宽段 超过这个宽度自动换行
     * @return
     */
    public static StaticLayout getStaticLayout(String text, TextPaint textPaint, int textWidth) {
        //参数4   layout的对其方式，有ALIGN_CENTER， ALIGN_NORMAL， ALIGN_OPPOSITE 三种。
        //参数5  相对行间距，相对字体大小，1.5f表示行间距为1.5倍的字体高度。
        //参数6 在基础行距上添加多少,实际间距为spacingmult spacingadd 的和
        //参数7  不知道干啥用的
        StaticLayout staticLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        return staticLayout;
    }
    /**
     * |
     * 获取Bitmap 需要绘制的大小
     *
     * @param bitmap
     * @return
     */
    public static Rect getBitmapSrcRectF(Bitmap bitmap) {

        return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

    }
    /**
     * 获取Bitmap 需要绘制的区域
     *
     * @param left
     * @param top
     * @param bitmap
     * @return
     */
    public static Rect getBitmapDesRectF(int left, int top, Bitmap bitmap) {

        return new Rect(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());

    }
    /**
     * 获取屏幕尺寸
     *
     * @param context context
     * @return 屏幕尺寸像素值，下标为0的值为宽，下标为1的值为高
     */
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
    /**
     * 创建一个bitmap
     * @param width
     * @param height
     * @param config
     * @param retryCount
     * @return
     */
    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }
}
