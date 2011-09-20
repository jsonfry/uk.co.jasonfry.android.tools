package uk.co.jasonfry.android.tools.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import uk.co.jasonfry.android.tools.util.AnimationUtil;
import uk.co.jasonfry.android.tools.ui.SwipeView;

public class BounceSwipeView extends SwipeView
{
	private static final int ANIMATION_DURATION = 120;
	private static final int FRAME_DURATION = 30;
	private static final int NUMBER_OF_FRAMES = ANIMATION_DURATION/FRAME_DURATION;
	
	private static final boolean BOUNCING_ON_LEFT = true;
	private static final boolean BOUNCING_ON_RIGHT = false;
	
	private boolean mAtEdge = false;
	private float mAtEdgeStartPosition;
	private float mAtEdgePreviousPosition;
	private int mPaddingLeft;
	private int mPaddingRight;
	private OnTouchListener mOnTouchListener;
	private int mPaddingStartValue;
	Handler mEaseAnimationFrameHandler;
	private int mCurrentAnimationFrame;
	private int mPaddingChange;
	private boolean mBouncingSide;
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	private boolean mBounceEnabled = true;
	
	public BounceSwipeView(Context context) 
	{
		super(context);
		mContext = context;
		initBounceSwipeView();
	}
	
	public BounceSwipeView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		initBounceSwipeView();
	}
	
	public BounceSwipeView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		mContext = context;
		initBounceSwipeView();
	}
	
	private void initBounceSwipeView()
	{
		super.setOnTouchListener(new BounceViewOnTouchListener());
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		mEaseAnimationFrameHandler = new Handler()
		{
			public void handleMessage(Message msg) 
			{
				int newPadding = AnimationUtil.quadraticOutEase(mCurrentAnimationFrame, mPaddingStartValue, -mPaddingChange, NUMBER_OF_FRAMES);
				
				if(mBouncingSide == BOUNCING_ON_LEFT)
				{
					BounceSwipeView.super.setPadding(newPadding, getPaddingTop(), getPaddingRight(), getPaddingBottom());
				}
				else if(mBouncingSide == BOUNCING_ON_RIGHT)
				{
					BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), newPadding, getPaddingBottom());
				}
				
				mCurrentAnimationFrame++;
				if(mCurrentAnimationFrame <= NUMBER_OF_FRAMES)
				{
					mEaseAnimationFrameHandler.sendEmptyMessageDelayed(0, FRAME_DURATION);
				}
			}
		};
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom)
	{
		mPaddingLeft = left;
		mPaddingRight = right;
		super.setPadding(left,top,right,bottom);
	}
	
	@Override
	public void setOnTouchListener(View.OnTouchListener onTouchListener)
	{
		mOnTouchListener = onTouchListener;
	}
	
	public void setBounceEnabled(boolean enabled)
	{
		mBounceEnabled = enabled;
	}
	
	public boolean getBounceEnabled()
	{
		return mBounceEnabled;
	}
	
	private class BounceViewOnTouchListener implements View.OnTouchListener
	{
		public boolean onTouch(View view, MotionEvent ev) 
		{
			if(mOnTouchListener!=null && mOnTouchListener.onTouch(view, ev))
			{
				return true;
			}
			
			if(mBounceEnabled)
			{
				switch(ev.getAction())
				{
					case MotionEvent.ACTION_MOVE :
						int maxScrollAmount = (getPageCount()-1)*getPageWidth()-getPageWidth()%2;
						if(getScrollX()==0 && !mAtEdge || getScrollX()==maxScrollAmount && !mAtEdge)
						{
							mAtEdge = true;
							mAtEdgeStartPosition = ev.getX();
							mAtEdgePreviousPosition = ev.getX();
						}
						else if(getScrollX()==0)
						{
							mAtEdgePreviousPosition = ev.getX();
							mBouncingSide=BOUNCING_ON_LEFT;
							BounceSwipeView.super.setPadding((int) (mAtEdgePreviousPosition-mAtEdgeStartPosition)/2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
							return true;
						}
						else if(getScrollX()>=maxScrollAmount)
						{
							mAtEdgePreviousPosition = ev.getX(); 
							mBouncingSide=BOUNCING_ON_RIGHT;

							int newRightPadding = (int) (mAtEdgeStartPosition-mAtEdgePreviousPosition)/2;
							if(newRightPadding>=mPaddingRight)
							{
								BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), newRightPadding, getPaddingBottom());
							}
							else
							{
								BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), mPaddingRight, getPaddingBottom());
							}

							scrollTo((int) (maxScrollAmount+(mAtEdgeStartPosition-mAtEdgePreviousPosition)/2), getScrollY());
							return true;
						}
						else
						{
							mAtEdge = false;
						}
						break;

					case MotionEvent.ACTION_UP : 
						if(mAtEdge)
						{
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
	
	private void doBounceBackEaseAnimation()
	{
		if(mBouncingSide == BOUNCING_ON_LEFT)
		{
			mPaddingChange = getPaddingLeft() - mPaddingLeft;
			mPaddingStartValue = getPaddingLeft();
		}
		else if(mBouncingSide == BOUNCING_ON_RIGHT)
		{
			mPaddingChange = getPaddingRight() - mPaddingRight;
			mPaddingStartValue = getPaddingRight();
		}
		
		mCurrentAnimationFrame = 0;
		
		mEaseAnimationFrameHandler.removeMessages(0);
		mEaseAnimationFrameHandler.sendEmptyMessage(0);
	}
	
	/*
	 * Animation that can be used to show you are at the end when using some 
	 * kind of control to go to the next page, rather than swiping
	 *
	 */
	public void doAtEdgeAnimation()
	{
		if(getCurrentPage()==0)
		{
			mBouncingSide = BOUNCING_ON_LEFT;
			BounceSwipeView.super.setPadding(getPaddingLeft()+50, getPaddingTop(), getPaddingRight(), getPaddingBottom());
		}
		else if(getCurrentPage()==getPageCount()-1)
		{
			mBouncingSide = BOUNCING_ON_RIGHT;
			BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight()+50, getPaddingBottom());
			scrollTo(getScrollX()+50,getScrollY());
		}
		
		doBounceBackEaseAnimation();
	}
}