package uk.co.jasonfry.android.tools.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import uk.co.jasonfry.android.tools.util.AnimationUtil;

public class BounceScrollView extends ScrollView {

	private static final int ANIMATION_DURATION = 120;
	private static final int FRAME_DURATION = 30;
	private static final int NUMBER_OF_FRAMES = ANIMATION_DURATION/FRAME_DURATION;
	private static final int FRICTION = 3;
	
	private static final boolean BOUNCING_ON_TOP = true;
	private static final boolean BOUNCING_ON_BOTTOM = false;
	
	private boolean mAtEdge = false;
	private float mAtEdgeStartPosition;
	private float mAtEdgePreviousPosition;
	private int mPaddingTop;
	private int mPaddingBottom;
	private OnTouchListener mOnTouchListener;
	private int mPaddingStartValue;
	private Handler mEaseAnimationFrameHandler;
	private int mCurrentAnimationFrame;
	private int mPaddingChange;
	private boolean mBouncingSide;
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	private boolean mBounceEnabled = true;
	private int mTopOffset = 0; //use this to hide bits at the top
	
	public BounceScrollView(Context context) {
		super(context);
		mContext = context;
		initBounceScrollView();
	}
	
	public BounceScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initBounceScrollView();
	}
	
	public BounceScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initBounceScrollView();
	}
	
	private void initBounceScrollView() {
		setOverScrollMode(View.OVER_SCROLL_NEVER);
		super.setOnTouchListener(new BounceViewOnTouchListener());
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		mEaseAnimationFrameHandler = new Handler() {
			public void handleMessage(Message msg) {
				int newPadding = AnimationUtil.quadraticOutEase(mCurrentAnimationFrame, mPaddingStartValue, -mPaddingChange, NUMBER_OF_FRAMES);
				
				if (mBouncingSide == BOUNCING_ON_TOP) {
					BounceScrollView.super.setPadding(getPaddingLeft(), newPadding, getPaddingRight(), getPaddingBottom());
				} else if (mBouncingSide == BOUNCING_ON_BOTTOM) {
					BounceScrollView.super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), newPadding);
				}
				
				mCurrentAnimationFrame++;
				if (mCurrentAnimationFrame <= NUMBER_OF_FRAMES) {
					mEaseAnimationFrameHandler.sendEmptyMessageDelayed(0, FRAME_DURATION);
				}
			}
		};
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		mPaddingTop = top;
		mPaddingBottom = bottom;
		super.setPadding(left, top, right, bottom);
	}
	
	@Override
	public void setOnTouchListener(View.OnTouchListener onTouchListener) {
		mOnTouchListener = onTouchListener;
	}
	
	public void setBounceEnabled(boolean enabled) {
		mBounceEnabled = enabled;
	}
	
	public boolean getBounceEnabled() {
		return mBounceEnabled;
	}
	
	private class BounceViewOnTouchListener implements View.OnTouchListener {
		public boolean onTouch(View view, MotionEvent ev) {
			if(mOnTouchListener!=null && mOnTouchListener.onTouch(view, ev)) {
				return true;
			}
			
			if(mBounceEnabled) {
				switch(ev.getAction()) {
					case MotionEvent.ACTION_MOVE :
						int maxScrollAmount = getChildAt(getChildCount()-1).getBottom()-getHeight();
						
						if(getScrollY()==0 && !mAtEdge || getScrollY()==maxScrollAmount && !mAtEdge) {
							mAtEdge = true;
							mAtEdgeStartPosition = ev.getY();
							mAtEdgePreviousPosition = ev.getY();
						} else if(getScrollY()==0 && ev.getY() > mAtEdgeStartPosition) {
						    mAtEdgePreviousPosition = ev.getY();
							mBouncingSide=BOUNCING_ON_TOP;
							BounceScrollView.super.setPadding(getPaddingLeft(), (int) (mAtEdgePreviousPosition-mAtEdgeStartPosition)/FRICTION, getPaddingRight(), getPaddingBottom());
							return true;
						} else if(getScrollY()>=maxScrollAmount) {
						    mAtEdgePreviousPosition = ev.getY(); 
							mBouncingSide=BOUNCING_ON_BOTTOM;

							int newBottomPadding = (int) (mAtEdgeStartPosition-mAtEdgePreviousPosition)/FRICTION;
							if(newBottomPadding>=mPaddingBottom) {
								BounceScrollView.super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), newBottomPadding);
							} else {
								BounceScrollView.super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), mPaddingBottom);
							}

							scrollTo(getScrollX(), (int) (maxScrollAmount+(mAtEdgeStartPosition-mAtEdgePreviousPosition)/FRICTION));
							return true;
						} else {
							mAtEdge = false;
						}
						break;

					case MotionEvent.ACTION_UP : 
						if(mAtEdge) {
							mAtEdge = false;
							mAtEdgePreviousPosition = 0;
							mAtEdgeStartPosition = 0;
							doBounceBackEaseAnimation();
							return true;
						}
						break;
				}
			}
			return false;
		}
	}
	
	private void doBounceBackEaseAnimation() {
		if(mBouncingSide == BOUNCING_ON_TOP) {
			mPaddingChange = getPaddingTop() - mPaddingTop;
			mPaddingStartValue = getPaddingTop();
		} else if(mBouncingSide == BOUNCING_ON_BOTTOM) {
			mPaddingChange = getPaddingBottom() - mPaddingBottom;
			mPaddingStartValue = getPaddingBottom();
		}
		
		mCurrentAnimationFrame = 0;
		
		mEaseAnimationFrameHandler.removeMessages(0);
		mEaseAnimationFrameHandler.sendEmptyMessage(0);
	}
}