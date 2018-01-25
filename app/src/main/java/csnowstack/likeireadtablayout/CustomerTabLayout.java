package csnowstack.likeireadtablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

/**
 * bug还是有的
 *
 * 不想改呀
 * 抄自 TabLayout
 */

public class CustomerTabLayout extends View {


    /**
     * 指示器所占 text的大小
     */
    @FloatRange(from = 0.1f, to = 1)
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorSize {
    }

    private @IndicatorSize float mIndicatorSize = 0.8f;
    private int mIndicatorHeight;


    /**
     * 选中和未选中的颜色
     */
    private int mColorSelected, mColorNormal;

    /**
     * tabItem 的padding
     * 绘制文字的时候要用到
     * mTabPaddingLeft,mTabPaddingRight
     * <p>
     * <p>
     * mTabPaddingTop,mTabPaddingBottom
     * 不指定高度的时候,会用这个计算高度
     * 反正最终都会默认居中文字(阴险笑)
     */
    private int mTabPaddingLeft, mTabPaddingRight, mTabPaddingTop, mTabPaddingBottom;

    /**
     * 文字相关
     */
    private int mTextSize;
    private List<Float> mTextWidth;
    private List<String> mTitles;

    private Paint mPaint;
    /**
     * 文字所占高度
     */
    private float mTextHeight;

    /**
     * 当前view 的宽度和高度
     */
    private int mWidth, mWidthMax, mHeight;

    /**
     * 文字居中的baseLine的位置
     */
    private float mBaseLine;
    private Paint.FontMetrics mFontMetrics;
    /**
     * 每个指示器应该在的x轴
     */
    private List<Integer> mIndicatorStartXs;

    /**
     * 每个Tab所属范围
     */
    private List<Float> mIndexRangeX;


    /**
     * 平滑滚动
     */
    private Scroller mScroller;

    /**
     * 当前选中的position
     */
    private int mCurrentSelected;

    /**
     * 下一个要选中的position
     */
    private int mNextSelected = -1;

    private int mTouchSlop;

    /**
     * 是否正在移动
     */
    private boolean mBeingMove;
    private float mLastTouchX;

    /**
     * 当前position的"居中"的位置
     */
    private float mCenterX;

    /**
     * 所占当前view的比例
     */
    private static final float CENTER_RATE = 3 / 7f;

    /**
     * 是否是由 scroller请求滑动
     */
    private boolean mIsScrollerRequest;

    /**
     * 拖动进行的进度
     */
    private float mDragProportion;

    /**
     * 需要阴影的item
     */
    private int mPressPosition = -1;

    private int mScreenWidth;

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabLayoutOnPageChangeListener mPageChangeListener;
    private AdapterChangeListener mAdapterChangeListener;
    private TabLayout.OnTabSelectedListener mCurrentVpSelectedListener;
    private final ArrayList<TabLayout.OnTabSelectedListener> mSelectedListeners = new ArrayList<>();

    private DataSetObserver mPagerAdapterObserver;

    public CustomerTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public CustomerTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomerTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomerTabLayout, 0, 0);

        mColorNormal = a.getColor(R.styleable.CustomerTabLayout_tab_color_normal, Color.BLACK);
        mColorSelected = a.getColor(R.styleable.CustomerTabLayout_tab_color_selected, Color.BLUE);

        mTabPaddingLeft = a.getDimensionPixelOffset(R.styleable.CustomerTabLayout_tab_padding_left, 0);


        mTabPaddingRight = a.getDimensionPixelOffset(R.styleable.CustomerTabLayout_tab_padding_right, 0);

        mTabPaddingTop = a.getDimensionPixelOffset(R.styleable.CustomerTabLayout_tab_padding_top, 0);

        mTabPaddingBottom = a.getDimensionPixelOffset(R.styleable.CustomerTabLayout_tab_padding_bottom, 0);


        mTextSize = a.getDimensionPixelSize(R.styleable.CustomerTabLayout_tab_text_size, 35);

        mIndicatorSize = a.getFloat(R.styleable.CustomerTabLayout_tab_indicator_size, .8f);
        mIndicatorHeight = a.getDimensionPixelOffset(R.styleable.CustomerTabLayout_tab_indicator_height, 20);

        a.recycle();
        init(context);


    }


    /**
     * 初始化
     * 不加个注释不舒服
     */
    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColorSelected);
        mPaint.setTextSize(mTextSize);

        mTitles = new ArrayList<>();
        mTextWidth = new ArrayList<>();
        mIndicatorStartXs = new ArrayList<>();
        mIndexRangeX = new ArrayList<>();

        mFontMetrics = mPaint.getFontMetrics();

        mTextHeight = mFontMetrics.bottom - mFontMetrics.top;
        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();

        mScreenWidth=displayMetrics.widthPixels;
    }


    public void fillTexts(List<String> titles) {
        mTitles.clear();
        mTextWidth.clear();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            mTitles.add(title);

            mTextWidth.add(mPaint.measureText(title));
        }
    }

    public void setupWithViewPager(ViewPager viewPager, boolean autoRefresh) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mPageChangeListener);
            }
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (mCurrentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(mCurrentVpSelectedListener);
            mCurrentVpSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            mPageChangeListener.reset();
            viewPager.addOnPageChangeListener(mPageChangeListener);

            // Now we'll add a tab selected listener to set ViewPager's current item
            mCurrentVpSelectedListener = new TabLayout.ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mCurrentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            mAdapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            selectPosition(viewPager.getCurrentItem());
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null;
            setPagerAdapter(null, false);
        }


    }


    /**
     * 计算宽高
     */
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width, height;
        //包裹的时候
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            height = (int) (mTextHeight + mTabPaddingTop + mTabPaddingBottom);
        } else {//指定的时候
            height = heightSize;
        }


        if (mTitles.size() == 0) {
            mWidthMax = widthSize;
        } else {
            setAllTextWidth();
        }

        //不能超过给定的高度
        //包裹
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            width = mWidthMax;
        } else {
            width = widthSize;
        }
        setMeasuredDimension(width, height);

    }

    private void setAllTextWidth() {
        int spaceWidth = mTitles.size() * (mTabPaddingLeft + mTabPaddingRight);
        float textWidth = 0;

        mTextWidth.clear();

        for (int i = 0, n = mTitles.size(); i < n; i++) {
            float current = mPaint.measureText(mTitles.get(i));
            textWidth += current;
            mTextWidth.add(current);
        }

        mWidthMax = (int) (spaceWidth + textWidth);

    }


    /**
     * 计算baseLine应该在的位置
     */
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mBaseLine = (mHeight / 2 - mTextHeight / 2) - mFontMetrics.top;
        mCenterX = (mWidth > mScreenWidth ?mScreenWidth : mWidth) * CENTER_RATE;
    }


    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTitles.size() == 0) {
            return;
        }

        drawTexts(canvas);
        drawIndicator(canvas);
        drawShadow(canvas);

    }


    /**
     * 绘制文字
     */
    private void drawTexts(Canvas canvas) {
        int startX = mTabPaddingLeft;

        mIndicatorStartXs.clear();
        mIndexRangeX.clear();
        for (int i = 0, n = mTitles.size(); i < n; i++) {
            mPaint.setColor(i == mCurrentSelected ? mColorSelected : mColorNormal);

            //保留指示器的所有x点
            mIndicatorStartXs.add((int) (startX + mTextWidth.get(i) / 2f * (1 - mIndicatorSize)));
            //保留点击范围,判断点了哪个item
            mIndexRangeX.add(startX + mTextWidth.get(i) + mTabPaddingRight);


            canvas.drawText(mTitles.get(i), startX, mBaseLine, mPaint);
            startX += mTabPaddingLeft + mTabPaddingRight + mTextWidth.get(i);

        }

    }


    /**
     * 绘制指示器
     */
    private void drawIndicator(Canvas canvas) {
        mPaint.setColor(mColorSelected);

        if (mNextSelected != mCurrentSelected && mNextSelected < mTitles.size() && mNextSelected >= 0) {//需要indicator动画

            //当前比例
            float proportion = mIsScrollerRequest ?
                    mScroller.timePassed() / 1f / mScroller.getDuration() : mDragProportion;

            //scroller 滚动的话
            if (mIsScrollerRequest && proportion >= 1) {
                mCurrentSelected = mNextSelected;
                mNextSelected = -1;
                invalidate();
                return;
            }


            //当前指示器的位置
            float indicatorStartX = mIndicatorStartXs.get(mCurrentSelected);
            float indicatorStartXRight = indicatorStartX + mTextWidth.get(mCurrentSelected) * mIndicatorSize;
            //最终的位置
            float indicatorEndX = mIndicatorStartXs.get(mNextSelected);
            float indicatorEndXRight = indicatorEndX + mTextWidth.get(mNextSelected) * mIndicatorSize;


            float left, right;
            if (proportion <= .5f) {//0~0.5, 指示器开始点移动到next的x
                proportion = 2 * proportion;

                if (mCurrentSelected > mNextSelected) {//右边的点向左边的
                    left = indicatorStartX - (indicatorStartX - indicatorEndX) * proportion;

                    right = indicatorStartXRight;

                } else {//左边点向右边
                    left = indicatorStartX;
                    right = indicatorStartXRight + (indicatorEndXRight - indicatorStartXRight) * proportion;
                }

            } else {//0.5到1 ,指示器由结束点移动到next的结束点

                proportion = (proportion - 0.5f) * 2;

                if (mCurrentSelected > mNextSelected) {//右边的点向左边的
                    left = indicatorEndX;

                    right = indicatorStartXRight -
                            (indicatorStartXRight - indicatorEndXRight) * proportion;

                } else {//左边点向右边
                    left = indicatorStartX + (indicatorEndX - indicatorStartX) * proportion;
                    right = indicatorEndXRight;

                }


            }

            canvas.drawRect(left,
                    mHeight - mIndicatorHeight,
                    right,
                    mHeight, mPaint);

        } else {


            canvas.drawRect(mIndicatorStartXs.get(mCurrentSelected), mHeight - mIndicatorHeight,
                    mIndicatorStartXs.get(mCurrentSelected) + mTextWidth.get(mCurrentSelected) * mIndicatorSize,
                    mHeight, mPaint);
        }


    }


    /**
     * 绘制阴影
     */
    private void drawShadow(Canvas canvas) {
        if (mPressPosition == -1)
            return;

        float right = mIndexRangeX.get(mPressPosition);
        float left = mPressPosition == 0 ? 0 : mIndexRangeX.get(mPressPosition - 1);

        mPaint.setColor(Color.parseColor("#33FFFFFF"));
        canvas.drawRect(left, 0, right, mHeight, mPaint);
    }


    /**
     * 处理点击事件,判断点击的位置
     * 确认选中了哪一个
     *
     * @return true 想都不用想,自己又不需要直接滑动,直接返回
     */
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (mTitles.size() == 0) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = event.getX();
                mBeingMove = false;
                mPressPosition = getSelectPosition(event);
                //绘制点击阴影的效果
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE://移动的不管
                if (mWidthMax < mWidth) {//没有超出View,滑动个屁
                    break;
                }


                float currentX = event.getX();

                float difference = currentX - mLastTouchX;
                if (!mBeingMove) {//不是正在move
                    if (Math.abs(difference) > mTouchSlop) {//开始move
                        mBeingMove = true;
                        difference += difference < 0 ? mTouchSlop : -mTouchSlop;

                    } else {//未开始移动,且小于touchSlop
                        break;
                    }

                }


                if (getScrollX() - difference <= 0) {//不能继续向右滑动
                    scrollBy(-getScrollX(), 0);

                } else if (getScrollX() - difference >= (mWidthMax - mWidth)) {//不能继续向左滑动
                    scrollBy(mWidthMax - mWidth - getScrollX(), 0);
                } else {
                    scrollBy((int) -difference, 0);
                }


                mLastTouchX = currentX;
                break;
            case MotionEvent.ACTION_CANCEL://不管
            case MotionEvent.ACTION_UP:

                mPressPosition = -1;//重置需要阴影的item

                //移动途中弹起的不触发点击事件
                if (mBeingMove) {
                    mBeingMove = false;
                    invalidate();
                    return false;
                }

                int selectPosition = getSelectPosition(event);
                if (selectPosition != -1)
                    selectPosition(selectPosition);


                break;

        }


        return true;
    }


    /**
     * 根据坐标获选当前选中的那个position
     */
    private int getSelectPosition(MotionEvent event) {
        float x = event.getX();
        for (int i = 0, n = mIndexRangeX.size(); i < n; i++) {
            if (x + getScrollX() < mIndexRangeX.get(i)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 点击了第几个position
     */
    private void selectPosition(int position) {
        if (mCurrentSelected == position)
            return;

        mIsScrollerRequest = true;

        mNextSelected = position;
        mViewPager.setCurrentItem(mNextSelected);
        dispatchScroll(mNextSelected);


    }


    /**
     * 判断当前position是否需要scroll
     */
    private void dispatchScroll(int position) {

        //原本位置
        float x = mIndicatorStartXs.get(position);
        //拖动的距离
        int scrollX = getScrollX();

        //当前相对于 view左边的位置
        float currentX = x - scrollX;

        //应该移动的距离
        float move = mCenterX - currentX;

        //已经要到最左了,只能移动到头
        if (scrollX - move < 0) {
            mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);

            //已经要到最右了,只能移动到尾
        } else if (scrollX - move > (mWidthMax - mWidth)) {
            mScroller.startScroll(getScrollX(), 0, mWidthMax - mWidth - getScrollX(), 0, 500);

        } else {//正常移动到中心
            mScroller.startScroll(getScrollX(), 0, (int) -move, 0, 500);

        }

        invalidate();


    }

    /**
     * scroller 滑动所需
     */
    @Override public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }


    /**
     * Remove the given {@link TabLayout.OnTabSelectedListener} that was previously added via
     *
     * @param listener listener to remove
     */
    public void removeOnTabSelectedListener(@NonNull TabLayout.OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }


    /**
     * 抄自
     * http://www.wangyuwei.me/2017/12/09/%E4%BD%BF%E7%94%A8%E7%B3%BB%E7%BB%9FTabLayout%E7%9A%84app%E5%BF%AB%E6%9D%A5%E4%BF%AEBug/
     */
    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<CustomerTabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;
        private boolean isTouchState;

        public TabLayoutOnPageChangeListener(CustomerTabLayout tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;

            if (state == SCROLL_STATE_DRAGGING) {
                isTouchState = true;
            } else if (state == SCROLL_STATE_IDLE) {
                isTouchState = false;
            }
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final CustomerTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null &&isTouchState) {
                tabLayout.setScrollPosition(position, positionOffset);
            }
        }


        @Override
        public void onPageSelected(final int position) {
            final CustomerTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                tabLayout.selectPosition(position);
            }
        }

        void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }
    }


    /**
     * 按进度来 设置
     */
    private void setScrollPosition(int position, float positionOffset) {

        //下一个item
        if (mNextSelected == position && (mCurrentSelected < position || //0~1
                (mCurrentSelected > position && positionOffset == 0))) {//1~0
            mCurrentSelected = mNextSelected;
            mNextSelected = -1;
            invalidate();
            return;
        }


        if (mCurrentSelected > position) {//1~0
            mNextSelected = position;
            mDragProportion = 1 - positionOffset;
        } else {//0~1
            mNextSelected = position + 1;
            mDragProportion = positionOffset;
        }


        mIsScrollerRequest = false;

        invalidate();
    }


    public int getTabCount() {
        return mTitles.size();
    }

    private int getSelectedTabPosition() {
        return mCurrentSelected;
    }

    /**
     * changes.
     * <p>
     * <p>Components that add a listener should take care to remove it when finished via
     *
     * @param listener listener to add
     */
    public void addOnTabSelectedListener(@NonNull TabLayout.OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }

        mPagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }


    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }


    void populateFromPagerAdapter() {

        if (mPagerAdapter != null) {

            final int adapterCount = mPagerAdapter.getCount();



            if (mViewPager != null && adapterCount > 0) {
                List<String> titles = new ArrayList<>();
                for (int i = 0; i < adapterCount; i++) {
                    titles.add(mPagerAdapter.getPageTitle(i).toString());
                }
                mCurrentSelected=0;

                fillTexts(titles);
                setAllTextWidth();

                invalidate();

            }
        }
    }

}
