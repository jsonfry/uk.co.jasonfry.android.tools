package uk.co.jasonfry.android.tools.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import uk.co.jasonfry.android.tools.util.AnimationUtil;

public class BounceScrollView extends ScrollView {

	public static final int BOUNCE_MODE_ENABLED = 0;
	public static final int BOUNCE_MODE_DISABLED = 1;
	public static final int BOUNCE_MODE_TOP = 2;
	public static final int BOUNCE_MODE_BOTTOM = 3;
	
	private static final int ANIMATION_DURATION = 120;
	private static final int FRAME_DURATION = 30;
	private static final int NUMBER_OF_FRAMES = ANIMATION_DURATION/FRAME_DURATION;
	private static final int DEFAULT_FRICTION = 3;
	
	public static final boolean BOUNCING_ON_TOP = true;
	public static final boolean BOUNCING_ON_BOTTOM = false;
	
	private boolean mAtEdge = false;
	private float mAtEdgeStartPosition;
	private float mAtEdgePreviousPosition;
	private int mPaddingTop;
	private int mPaddingBottom;
	private float mTranslationTop;
	private float mTranslationBottom;
	private BounceListener mBounceListener;
	private OnTouchListener mOnTouchListener;
	private int mPaddingStartValue;
	private Handler mEaseAnimationFrameHandler;
	private int mCurrentAnimationFrame;
	private int mPaddingChange;
	private boolean mLastBouncingSide;
	private Context mContext;
	private boolean mBouncingTopEnabled = true;
	private boolean mBouncingBottomEnabled = true;
	private boolean mBouncing = false;
	private int mFriction = DEFAULT_FRICTION;
	
	
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
		
		mEaseAnimationFrameHandler = new Handler() {
			public void handleMessage(Message msg) {
				float newPadding = AnimationUtil.quadraticOutEase(mCurrentAnimationFrame, mPaddingStartValue, -mPaddingChange, NUMBER_OF_FRAMES);
				
				if (mLastBouncingSide == BOUNCING_ON_TOP) {
					if(!api11OrLater()) {
					    BounceScrollView.super.setPadding(getPaddingLeft(), (int) newPadding, getPaddingRight(), mPaddingBottom- (int) newPadding);
					} else {
    					BounceScrollView.super.setTranslationY(newPadding);
					}
					onBounceTop(getCurrentTopBounceAmount(), false);
				} else if (mLastBouncingSide == BOUNCING_ON_BOTTOM) {
					if(!api11OrLater()) {
					    BounceScrollView.super.setPadding(getPaddingLeft(), mPaddingTop- (int) newPadding, getPaddingRight(), (int) newPadding);
                    } else {
                        BounceScrollView.super.setTranslationY(-newPadding);
                    }
                    onBounceTop(getCurrentBottomBounceAmount(), false);
				}
				
				mCurrentAnimationFrame++;
				if (mCurrentAnimationFrame <= NUMBER_OF_FRAMES) {
					mEaseAnimationFrameHandler.sendEmptyMessageDelayed(0, FRAME_DURATION);
				} else {
    				onAnimationBounceFinish();
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
	public void setTranslationY(float translation) {
    	if(translation>0) {
        	mTranslationTop = translation;
        	mTranslationBottom = 0;
    	} else if(translation<0) {
        	mTranslationTop = 0;
        	mTranslationBottom = translation;
    	} else { //translation == 0
        	mTranslationTop = 0;
        	mTranslationBottom = 0;
    	}
    	super.setTranslationY(translation);
	}
	
	public void setBounceListener(BounceListener bounceListener) {
    	mBounceListener = bounceListener;
	}
	
	public BounceListener getBounceListener() {
    	return mBounceListener;
	}
	
	private int getCurrentTopBounceAmount() {
    	if(!api11OrLater()) {
        	return getPaddingTop()-mPaddingTop;
    	} else {
        	return (int) (getTranslationY()-mTranslationTop);
    	}
	}
	
	private int getCurrentBottomBounceAmount() {
    	if(!api11OrLater()) {
        	return getPaddingBottom()-mPaddingBottom;
    	} else {
        	return (int)(-getTranslationY()-mTranslationBottom);
    	}
	}
	
	@Override
	public void setOnTouchListener(View.OnTouchListener onTouchListener) {
		mOnTouchListener = onTouchListener;
	}
	
	public void setBounceEnabled(boolean enabled) {
		if(enabled) {
    		setBounceMode(BOUNCE_MODE_ENABLED);
		} else {
    		setBounceMode(BOUNCE_MODE_DISABLED);
		}
	}
	
	public boolean getBounceEnabled() {
		return mBouncingTopEnabled || mBouncingBottomEnabled;
	}
	
	public void setBounceMode(int bounceMode) {
    	switch(bounceMode) {
        	case BOUNCE_MODE_ENABLED :
        	    mBouncingTopEnabled = true;
        	    mBouncingBottomEnabled = true;
        	    break;
            case BOUNCE_MODE_DISABLED :
                mBouncingTopEnabled = false;
                mBouncingBottomEnabled = false;
                break;
            case BOUNCE_MODE_TOP :
                mBouncingTopEnabled = true;
                mBouncingBottomEnabled = false;
                break;
            case BOUNCE_MODE_BOTTOM :
                mBouncingTopEnabled = false;
                mBouncingBottomEnabled = true;
                break;
    	}
	}
	
	public int getBounceMode() {
    	if(mBouncingTopEnabled && mBouncingBottomEnabled) {
        	return BOUNCE_MODE_ENABLED;
    	} else if(mBouncingTopEnabled) {
        	return BOUNCE_MODE_TOP;
    	} else if(mBouncingBottomEnabled) {
        	return BOUNCE_MODE_BOTTOM;
    	} else {
        	return BOUNCE_MODE_DISABLED;
    	}
	}
	
	public int getMaxBounceAmount() {
    	return (getHeight()/mFriction);
	}
	
	public void setFriction(int friction) {
    	mFriction = friction;
	}
	
	public int getFriction() {
    	return mFriction;
	}
	
	private class BounceViewOnTouchListener implements View.OnTouchListener {
		public boolean onTouch(View view, MotionEvent ev) {
			if(mOnTouchListener!=null && mOnTouchListener.onTouch(view, ev)) {
				return true;
			}
			
			if(mBouncingTopEnabled || mBouncingBottomEnabled) {
				switch(ev.getAction()) {
					case MotionEvent.ACTION_MOVE :
						int maxScrollAmount = getChildAt(getChildCount()-1).getBottom()-getHeight();
						
						if(getScrollY()==0 && !mAtEdge || getScrollY()==maxScrollAmount && !mAtEdge) {
							mAtEdge = true;
							mAtEdgeStartPosition = ev.getRawY(); //like a dinosaur RAWWWWWW
							mAtEdgePreviousPosition = ev.getRawY();
						} else if(mBouncingTopEnabled && getScrollY()==0 && ev.getRawY() > mAtEdgeStartPosition) {
						    mAtEdgePreviousPosition = ev.getRawY();
							mLastBouncingSide=BOUNCING_ON_TOP;
                            mBouncing = true;
                            
                            float newTopPadding = (mAtEdgePreviousPosition-mAtEdgeStartPosition)/mFriction;
                            if(!api11OrLater()) {
							    BounceScrollView.super.setPadding(getPaddingLeft(), (int) newTopPadding+mPaddingTop, getPaddingRight(), mPaddingBottom- (int) newTopPadding);
                            } else {
    							BounceScrollView.super.setTranslationY(newTopPadding+mTranslationTop);
							}
							onBounceTop(getCurrentTopBounceAmount(), true);
							return true;
						} else if(mBouncingBottomEnabled && getScrollY()>=maxScrollAmount) {
						    mAtEdgePreviousPosition = ev.getRawY(); 
							mLastBouncingSide=BOUNCING_ON_BOTTOM;
                            mBouncing = true;
                            
							float newBottomPadding = (mAtEdgeStartPosition-mAtEdgePreviousPosition)/mFriction;
							
							if(!api11OrLater()) {
    							if(newBottomPadding>=mPaddingBottom) {
    								BounceScrollView.super.setPadding(getPaddingLeft(), mPaddingTop - (int) newBottomPadding, getPaddingRight(), (int) (newBottomPadding+mTranslationBottom));
    							} else {
    								BounceScrollView.super.setPadding(getPaddingLeft(), mPaddingTop, getPaddingRight(), (int) mPaddingBottom);
    							}
                            } else {
    							if(newBottomPadding>=mTranslationBottom) {
    								BounceScrollView.super.setTranslationY((-newBottomPadding-mTranslationBottom)+mTranslationTop);
    							} else {
    								BounceScrollView.super.setTranslationY(-mTranslationBottom+mTranslationTop);
    							}
							}
							scrollTo(getScrollX(), (int) (maxScrollAmount+(mAtEdgeStartPosition-mAtEdgePreviousPosition)/mFriction));
							
							onBounceBottom(getCurrentBottomBounceAmount(), true);
							return true;
						} else {
							mAtEdge = false;
							return false;
						}
						break;

					case MotionEvent.ACTION_UP : 
						return doTouchFinished();

					case MotionEvent.ACTION_CANCEL :
						return doTouchFinished();
				}
			}
			return false;
		}
	}

	private boolean doTouchFinished() {
		if(mAtEdge) {
			mAtEdge = false;
			mAtEdgePreviousPosition = 0;
			mAtEdgeStartPosition = 0;
			if(mBouncing) {
			    onTouchBounceFinish();
				doBackAnimation();
				mBouncing = false;
			}
			return true;
		}
		return false;
	}
		
	private void doBackAnimation() {
    	int paddingStartValue = 0;
    	int paddingChangeValue = 0;
    	
    	if(mLastBouncingSide == BOUNCING_ON_TOP) {
			if(!api11OrLater()) {
                paddingStartValue = getPaddingTop();
            } else {
                paddingChangeValue = (int) (getTranslationY() - mTranslationTop);
                paddingStartValue = (int) getTranslationY();
            }
		} else if(mLastBouncingSide == BOUNCING_ON_BOTTOM) {
			if(!api11OrLater()) {
                paddingChangeValue = getPaddingBottom() - mPaddingBottom;
                paddingStartValue = getPaddingBottom();
            } else {
                paddingChangeValue = (int) (-getTranslationY() - mTranslationBottom + mTranslationTop);
                paddingStartValue = (int) -getTranslationY();
            }
		}
		animateBack(paddingStartValue, paddingChangeValue, mLastBouncingSide);
	}
	
	protected void animateBack(int start, int change, boolean lastBouncingSide) {
    	mPaddingStartValue = start;
    	mPaddingChange = change;
    	mLastBouncingSide = lastBouncingSide;
    	
    	mCurrentAnimationFrame = 0;
		mEaseAnimationFrameHandler.removeMessages(0);
		mEaseAnimationFrameHandler.sendEmptyMessage(0);
	}
	
	private boolean api11OrLater() {
    	return Build.VERSION.SDK_INT >= 11;
	}
	
	public interface BounceListener {
    	public void onBounceTop(int bounceAmount, boolean fromTouch);
    	public void onBounceBottom(int bounceAmount, boolean fromTouch);
    	public void onTouchBounceFinish();
    	public void onAnimationBounceFinish();
	}
	
	private void onBounceTop(int bounceAmount, boolean fromTouch) {
    	if (mBounceListener != null) {
        	mBounceListener.onBounceTop(bounceAmount, fromTouch);
    	}
	}
	
	private void onBounceBottom(int bounceAmount, boolean fromTouch) {
    	if (mBounceListener != null) {
        	mBounceListener.onBounceBottom(bounceAmount, fromTouch);
    	}
	}
    
    private void onTouchBounceFinish() {
        if (mBounceListener != null) {
        	mBounceListener.onTouchBounceFinish();
    	}
    }
    
    private void onAnimationBounceFinish() {
        if (mBounceListener != null) {
        	mBounceListener.onAnimationBounceFinish();
    	}	
    }
}