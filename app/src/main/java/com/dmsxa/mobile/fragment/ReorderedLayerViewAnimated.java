

package com.dmsxa.mobile.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import com.dmsxa.maplibui.fragment.LayersListAdapter;
import com.dmsxa.maplibui.fragment.ReorderedLayerView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

import static com.dmsxa.maplib.util.Constants.NOT_FOUND;


public class ReorderedLayerViewAnimated
        extends ReorderedLayerView
{
    private final int MOVE_DURATION = 150;


    public ReorderedLayerViewAnimated(Context context)
    {
        super(context);
    }


    public ReorderedLayerViewAnimated(
            Context context,
            AttributeSet attrs)
    {
        super(context, attrs);
    }


    public ReorderedLayerViewAnimated(
            Context context,
            AttributeSet attrs,
            int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReorderedLayerViewAnimated(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public  void handleCellSwitch()
    {
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewForID(mBelowItemId);
        View mobileView = getViewForID(mMobileItemId);
        View aboveView = getViewForID(mAboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {

            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighborViewsForID(mMobileItemId);
                return;
            }

            LayersListAdapter adapter = (LayersListAdapter) getAdapter();
            if (null != adapter) {
                adapter.swapElements(originalItem, getPositionForView(switchView));
            }

            mDownY = mLastEventY;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViewsForID(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener()
                    {
                        public boolean onPreDraw()
                        {
                            observer.removeOnPreDrawListener(this);

                            View switchView = getViewForID(switchItemID);
                            AnimatorProxy switchViewProxy = AnimatorProxy.wrap(switchView);

                            mTotalOffset += deltaY;

                            int switchViewNewTop = switchView.getTop();
                            int delta = switchViewStartTop - switchViewNewTop;

                            switchViewProxy.setTranslationY(delta);

                            ObjectAnimator animator =
                                    ObjectAnimator.ofFloat(switchViewProxy, "translationY", 0);
                            animator.setDuration(MOVE_DURATION);
                            animator.start();

                            return true;
                        }
                    });
        }
    }


    @Override
    public  void touchEventsEnded()
    {

        final View mobileView = getViewForID(mMobileItemId);
        if (mCellIsMobile || mIsWaitingForScrollFinish) {

            LayersListAdapter adapter = (LayersListAdapter) getAdapter();
            adapter.endDrag();

            mCellIsMobile = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = NOT_FOUND;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(
                    mHoverCell, "bounds", sBoundEvaluator, mHoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator)
                        {
                            invalidate();
                        }
                    });
            hoverViewAnimator.addListener(
                    new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationStart(Animator animation)
                        {
                            setEnabled(false);
                        }


                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mAboveItemId = NOT_FOUND;
                            mMobileItemId = NOT_FOUND;
                            mBelowItemId = NOT_FOUND;
                            mobileView.setVisibility(VISIBLE);
                            mHoverCell = null;
                            setEnabled(true);
                            invalidate();
                        }
                    });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }


    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its final location when the
     * user lifts his finger by modifying the BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>()
    {
        public Rect evaluate(
                float fraction,
                Rect startValue,
                Rect endValue)
        {
            return new Rect(
                    interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }


        public int interpolate(
                int start,
                int end,
                float fraction)
        {
            return (int) (start + fraction * (end - start));
        }
    };
}
