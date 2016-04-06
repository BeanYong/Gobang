package com.anasit.beanyong.gobang;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by BeanYong on 2016/4/1.
 */
public class GobangView extends View {
    /**
     * 保存与恢复View时使用的常量
     */
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAMEOVER = "instance_gameover";
    private static final String INSTANCE_BLACKLIST = "instance_blacklist";
    private static final String INSTANCE_WHITELIST = "instance_whitelist";
    /**
     * 上下文对象
     */
    private Context mContext;
    /**
     * 五子棋棋盘宽高
     */
    private int mPanelWidth;
    /**
     * 每个格子的宽高
     */
    private float mGridWidth;
    /**
     * 最大边数
     */
    private final static int MAXLINE = 14;
    /**
     * Canvas绘图使用的画笔
     */
    private Paint mPaint;
    /**
     * 黑色棋子
     */
    private Bitmap mBlackPiece;
    /**
     * 白色棋子
     */
    private Bitmap mWhitePiece;
    /**
     * 棋子缩放的比例
     */
    private final float RATIOPEICE = 3 * 1.0f / 4;
    /**
     * 白色棋子集合
     */
    private ArrayList<Point> mWhitePieceList;
    /**
     * 黑色棋子集合
     */
    private ArrayList<Point> mBlackPieceList;
    /**
     * 是否是黑色落子
     */
    private boolean mIsBlack = true;
    /**
     * 游戏是否已经结束，游戏结束后不允许落子
     */
    public static boolean mIsGameOver = false;

    public GobangView(Context context) {
        this(context, null);
    }

    public GobangView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GobangView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        //初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xff000000);
        //初始化棋子
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
        //初始化棋子数组
        mWhitePieceList = new ArrayList<>();
        mBlackPieceList = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthSize = Math.min(widthSize, heightSize);
        mPanelWidth = widthSize;
        mGridWidth = mPanelWidth * 1.0f / MAXLINE;//网格宽度
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, (int) (RATIOPEICE * mGridWidth), (int) (RATIOPEICE * mGridWidth), false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, (int) (RATIOPEICE * mGridWidth), (int) (RATIOPEICE * mGridWidth), false);
        setMeasuredDimension(widthSize, widthSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGobangLine(canvas);//绘制棋盘的线条
        drawPieces(canvas);//绘制棋子
    }

    /**
     * 绘制棋子
     *
     * @param canvas 画布
     */
    private void drawPieces(Canvas canvas) {
        for (int i = 0, n = mBlackPieceList.size(); i < n; i++) {
            Point p = mBlackPieceList.get(i);
            canvas.drawBitmap(mBlackPiece, (int) ((p.x + (1 - RATIOPEICE) / 2) * mGridWidth), (int) ((p.y + (1 - RATIOPEICE) / 2) * mGridWidth), null);
        }

        for (int i = 0, n = mWhitePieceList.size(); i < n; i++) {
            Point p = mWhitePieceList.get(i);
            canvas.drawBitmap(mWhitePiece, (int) ((p.x + (1 - RATIOPEICE) / 2) * mGridWidth), (int) ((p.y + (1 - RATIOPEICE) / 2) * mGridWidth), null);
        }
    }

    /**
     * 绘制棋盘的线条
     *
     * @param canvas 画布
     */
    private void drawGobangLine(Canvas canvas) {
        float startX = mGridWidth * 0.5f;
        float endX = mPanelWidth - startX;
        for (int i = 0; i < MAXLINE; i++) {
            float y = mGridWidth * i + startX;
            canvas.drawLine(startX, y, endX, y, mPaint);
            canvas.drawLine(y, startX, y, endX, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver) {//游戏已经结束，不允许落子
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            Point p = generateValidPoint(event.getX(), event.getY());
            if (mBlackPieceList.contains(p) || mWhitePieceList.contains(p)) {
                return false;
            } else {
                if (mIsBlack) {//黑色落子
                    mBlackPieceList.add(p);
                    if (checkWin(p) == 1) {//检查是否产生了赢家
                        Toast.makeText(mContext, "黑棋赢了", Toast.LENGTH_LONG).show();
                        mIsGameOver = true;
                    } else if(checkWin(p) == 2){
                        Toast.makeText(mContext, "和棋", Toast.LENGTH_LONG).show();
                    }
                } else {//白色落子
                    mWhitePieceList.add(p);
                    if (checkWin(p) == 1) {//检查是否产生了赢家
                        Toast.makeText(mContext, "白棋赢了", Toast.LENGTH_LONG).show();
                        mIsGameOver = true;
                    } else if(checkWin(p) == 2){
                        Toast.makeText(mContext, "和棋", Toast.LENGTH_LONG).show();
                    }
                }
                mIsBlack = !mIsBlack;
            }
        }
        invalidate();//重绘UI
        return true;
    }

    /**
     * 检查是否产生了赢家
     *
     * @param point 当前落子
     * @return 是否已经产生赢家
     */
    private byte checkWin(Point point) {
        if(mWhitePieceList.size()+mBlackPieceList.size() == MAXLINE*MAXLINE){//检查是否为和棋
            return 2;
        }
        if (checkHorizontal(point)) {//检查水平方向是否五子连珠
            return 1;
        }
        if (checkVertical(point)) {//检查竖直方向是否五子连珠
            return 1;
        }
        if (checkLeftRight(point)) {//检查左高右低斜线方向是否五子连珠
            return 1;
        }
        if (checkRightLeft(point)) {//检查右高左低斜线方向是否五子连珠
            return 1;
        }
        return 0;
    }

    /**
     * 检查水平方向是否五子连珠
     *
     * @param point 当前落子
     * @return 是否已经产生赢家
     */
    private boolean checkHorizontal(Point point) {
        int count = 1;//当count=5时，产生赢家，游戏结束
        ArrayList<Point> pointList = null;//棋子集合
        if (mIsBlack) {//当前落子为黑色
            pointList = mBlackPieceList;
        } else {//当前落子为白色
            pointList = mWhitePieceList;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查左侧
            if (pointList.contains(new Point(point.x - i, point.y))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查右侧
            if (pointList.contains(new Point(point.x + i, point.y))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }
        return false;
    }

    /**
     * 检查竖直方向是否五子连珠
     *
     * @param point 当前落子
     * @return 是否已经产生赢家
     */
    private boolean checkVertical(Point point) {
        int count = 1;//当count=5时，产生赢家，游戏结束
        ArrayList<Point> pointList = null;//棋子集合
        if (mIsBlack) {//当前落子为黑色
            pointList = mBlackPieceList;
        } else {//当前落子为白色
            pointList = mWhitePieceList;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查下方
            if (pointList.contains(new Point(point.x, point.y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查上方
            if (pointList.contains(new Point(point.x, point.y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }
        return false;
    }

    /**
     * 检查左高右低斜线方向是否五子连珠
     *
     * @param point 当前落子
     * @return 是否已经产生赢家
     */
    private boolean checkLeftRight(Point point) {
        int count = 1;//当count=5时，产生赢家，游戏结束
        ArrayList<Point> pointList = null;//棋子集合
        if (mIsBlack) {//当前落子为黑色
            pointList = mBlackPieceList;
        } else {//当前落子为白色
            pointList = mWhitePieceList;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查左上方
            if (pointList.contains(new Point(point.x - i, point.y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查右下方
            if (pointList.contains(new Point(point.x + i, point.y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }
        return false;
    }

    /**
     * 检查右高左低斜线方向是否五子连珠
     *
     * @param point 当前落子
     * @return 是否已经产生赢家
     */
    private boolean checkRightLeft(Point point) {
        int count = 1;//当count=5时，产生赢家，游戏结束
        ArrayList<Point> pointList = null;//棋子集合
        if (mIsBlack) {//当前落子为黑色
            pointList = mBlackPieceList;
        } else {//当前落子为白色
            pointList = mWhitePieceList;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查左下方
            if (pointList.contains(new Point(point.x - i, point.y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }

        for (int i = 1; i < 5; i++) {//以当前落子为原点，检查右上方
            if (pointList.contains(new Point(point.x + i, point.y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (count >= 5) {
            return true;
        }
        return false;
    }

    /**
     * 生成有效的棋子点
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return 棋子点
     */
    private Point generateValidPoint(float x, float y) {
        return new Point((int) (x / mGridWidth), (int) (y / mGridWidth));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAMEOVER, mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_BLACKLIST, mBlackPieceList);
        bundle.putParcelableArrayList(INSTANCE_WHITELIST,mWhitePieceList);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            mIsGameOver = bundle.getBoolean(INSTANCE_GAMEOVER);
            mBlackPieceList = bundle.getParcelableArrayList(INSTANCE_BLACKLIST);
            mWhitePieceList = bundle.getParcelableArrayList(INSTANCE_WHITELIST);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 再来一局，暴露给Activity
     */
    public void restart(){
        mBlackPieceList.clear();
        mWhitePieceList.clear();
        mIsGameOver = false;
        invalidate();
    }
}
