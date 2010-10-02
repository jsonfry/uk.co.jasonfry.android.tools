/*
 * Copyright (C) 2010 Jason Fry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Jason Fry - jasonfry.co.uk
 * @version 1.0
 * 
 */

package uk.co.jasonfry.android.tools.ui;

import uk.co.jasonfry.android.tools.ui.PageControl.OnPageControlClickListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


public class SwipeView extends HorizontalScrollView
{
	private LinearLayout mLinearLayout;
	private Context mContext;
	private int SCREEN_WIDTH;
	private int motionStartX;
	private int distanceX;
	private int previousDirection;
	private int mCurrentPage;
	private int mPageWidth = 0;
	private boolean firstMotionEvent = true;
	private OnPageChangedListener mOnPageChangedListener = null;
	private SwipeOnTouchListener mSwipeOnTouchListener;
	
	private PageControl mPageControl = null;
	
	private static int DEFAULT_SWIPE_THRESHOLD = 70;
	

	public SwipeView(Context context) 
	{
		super(context);
		mContext = context;
		initSwipeView();
	}
	
	public SwipeView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		initSwipeView();
	}
	
	public SwipeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs,defStyle);
		mContext = context;
		initSwipeView();
	}
	
	private void initSwipeView()
	{
		Log.i("uk.co.jasonfry.android.tools.ui.SwipeView","Initialising SwipeView");
		mLinearLayout = new LinearLayout(mContext);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		super.addView(mLinearLayout, -1, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		setSmoothScrollingEnabled(true);
		setHorizontalFadingEdgeEnabled(false);
		setHorizontalScrollBarEnabled(false);
		
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		SCREEN_WIDTH = (int) (display.getWidth());
		// Default page width to whole screen width
		mPageWidth = SCREEN_WIDTH;
		mCurrentPage = 0;
		
		mSwipeOnTouchListener = new SwipeOnTouchListener();
		setOnTouchListener(mSwipeOnTouchListener);
	}
	
	// returns the X offset to be added to the LEFT margin of the first child and RIGHT margin of the last child
	public int calculatePageSize(MarginLayoutParams childLayoutParams) {
		// Screen Width - Child Width (including margins)
		this.mPageWidth = childLayoutParams.leftMargin + childLayoutParams.width + childLayoutParams.rightMargin;
		int gapTotalX = SCREEN_WIDTH - mPageWidth;
		int offsetX = (int) (gapTotalX / 2);
		return offsetX;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event)
	{
		return true;
	}
	
	@Override
	public void addView(View child)
	{
		addView(child,-1);
	}
	
	@Override
	public void addView (View child, int index)
	{
		ViewGroup.LayoutParams params;
		params = new LayoutParams(SCREEN_WIDTH, LayoutParams.FILL_PARENT);
		addView(child, index, params);
	}
	
	@Override
	public void addView (View child, ViewGroup.LayoutParams params)
	{
		addView (child, -1, new LayoutParams(SCREEN_WIDTH, params.height));
	}
	
	@Override
	public void addView (View child, int index, ViewGroup.LayoutParams params)
	{
		requestLayout();
		invalidate();
		mLinearLayout.addView(child, index, params);
	}
	
	/**
	 * Get the View object that contains all the children of this SwipeView. The same as calling getChildAt(0)
	 * A SwipeView behaves slightly differently from a normal ViewGroup, all the children of a SwipeView
	 * sit within a LinearLayout, which then sits within the SwipeView object. 
	 * 
	 * @return linearLayout The View object that contains all the children of this view
	 */
	public LinearLayout getChildContainer()
	{
		return mLinearLayout;
	}
	
	
	
	/**
	 * Get the swiping threshold distance to make the screens change
	 * 
	 * @return swipeThreshold The minimum distance the finger should move to allow the screens to change
	 */
	public int getSwipeThreshold()
	{
		return DEFAULT_SWIPE_THRESHOLD;
	}
	
	/**
	 * Set the swiping threshold distance to make the screens change
	 * 
	 * @param swipeThreshold The minimum distance the finger should move to allow the screens to change
	 */
	public void setSwipeThreshold(int swipeThreshold)
	{
		DEFAULT_SWIPE_THRESHOLD = swipeThreshold;
	}
	
	/**
	 * Get the current page the SwipeView is on
	 * 
	 * @return The current page the SwipeView is on
	 */
	public int getCurrentPage()
	{
		return mCurrentPage;
	}
	
	/**
	 * Return the number of pages in this SwipeView
	 * 
	 * @return Returns the number of pages in this SwipeView
	 */
	public int getPageCount()
	{
		return mLinearLayout.getChildCount();
	}
	
	/**
	 * Go directly to the specified page
	 * 
	 * @param page The page to scroll to
	 */
	public void scrollToPage(int page)
	{
		scrollTo(page*mPageWidth,0);
		if(mOnPageChangedListener!=null)
		{
			mOnPageChangedListener.onPageChanged(mCurrentPage, page);
		}
		if(mPageControl!=null)
		{
			mPageControl.setCurrentPage(page);
		}
		mCurrentPage = page;
	}
	
	/**
	 * Animate a scroll to the specified page
	 * 
	 * @param page The page to animate to
	 */
	public void smoothScrollToPage(int page)
	{
		smoothScrollTo(page*mPageWidth,0);
		if(mOnPageChangedListener!=null)
		{
			mOnPageChangedListener.onPageChanged(mCurrentPage, page);
		}
		if(mPageControl!=null)
		{
			mPageControl.setCurrentPage(page);
		}
		mCurrentPage = page;
	}
	
	/**
	 * Assign a PageControl object to this SwipeView. Call after adding all the children
	 * 
	 * @param pageControl The PageControl object to assign
	 */
	public void setPageControl(PageControl pageControl)
	{
		mPageControl = pageControl;
		
		pageControl.setPageCount(getPageCount());
		pageControl.setCurrentPage(mCurrentPage);
		pageControl.setOnPageControlClickListener(new OnPageControlClickListener() 
		{
			
			public void goForwards() 
			{
				smoothScrollToPage(mCurrentPage+1);
				
			}
			
			public void goBackwards() 
			{
				smoothScrollToPage(mCurrentPage-1);
				
			}
		});
	}
	
	/**
	 * Return the current PageControl object
	 * 
	 * @return Returns the current PageControl object
	 */
	public PageControl getPageControl()
	{
		return mPageControl;
	}
	
	/**
	 * Implement this listener to listen for page change events
	 * 
	 * @author Jason Fry - jasonfry.co.uk
	 *
	 */
	public interface OnPageChangedListener
	{
		/**
		 * Event for when a page changes
		 * 
		 * @param oldPage The page the view was on previously
		 * @param newPage The page the view has moved to
		 */
		public abstract void onPageChanged(int oldPage, int newPage);
	}
	
	/**
	 * Set the current OnPageChangedListsner
	 * 
	 * @param onPageChangedListener The OnPageChangedListener object
	 */
	public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener)
	{
		mOnPageChangedListener = onPageChangedListener;
	}
	
	/**
	 * Get the current OnPageChangeListsner
	 * 
	 * @return The current OnPageChangedListener
	 */
	public OnPageChangedListener getOnPageChangedListener()
	{
		return mOnPageChangedListener;
	}
	
	private class SwipeOnTouchListener implements View.OnTouchListener
	{

		public boolean onTouch(View v, MotionEvent event) 
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN :
					motionStartX = (int) event.getX();
					firstMotionEvent = false;
					return false;
					
				case MotionEvent.ACTION_MOVE :
					
					int newDistance = motionStartX - (int) event.getX();
					int newDirection;
					
					if(newDistance<0) //backwards
					{
						newDirection =  (distanceX+4 <= newDistance) ? 1 : -1;  //the distance +4 is to allow for jitter
					}
					else //forwards
					{
						newDirection =  (distanceX-4 <= newDistance) ? 1 : -1;  //the distance -4 is to allow for jitter
					}
					
					
					if(newDirection != previousDirection && !firstMotionEvent)//changed direction, so reset start point
					{
						motionStartX = (int) event.getX();
						distanceX = motionStartX - (int) event.getX();
					}
					else
					{
						distanceX = newDistance;
					}

					previousDirection = newDirection; //backwards -1, forwards is 1
					return false;
					
				case MotionEvent.ACTION_UP :
					float fingerUpPosition = getScrollX();
	                float numberOfPages = mLinearLayout.getMeasuredWidth() / mPageWidth;
	                float fingerUpPage = fingerUpPosition/mPageWidth;
	                float edgePosition = 0;
	                
	                if(previousDirection == 1) //forwards
	                {
	                	if(distanceX > DEFAULT_SWIPE_THRESHOLD)//if over then go forwards
		                {
		                	if(mCurrentPage<(numberOfPages-1))//if not at the end of the pages, you don't want to try and advance into nothing!
		                	{
		                		edgePosition = (int)(fingerUpPage+1)*mPageWidth;
		                	}
		                	else
		                	{
		                		edgePosition = (int)(fingerUpPage)*mPageWidth;
		                	}
		                }
		                else //return to start position
		                {
		                	if(Math.round(fingerUpPage)==numberOfPages-1)//if at the end
		                	{
		                		//need to correct for when user starts to scroll into 
		                		//nothing then pulls it back a bit, this becomes a 
		                		//kind of forwards scroll instead
		                		edgePosition = (int)(fingerUpPage+1)*mPageWidth;
		                	}
		                	else //carry on as normal
		                	{
		                		edgePosition = (int)(fingerUpPage)*mPageWidth;
		                	}
		                }
	                	
	                	
	                	
	                	
	                }
	                else //backwards
	                {
	                	if(distanceX < -DEFAULT_SWIPE_THRESHOLD)//go backwards
		                {
		                	edgePosition = (int)(fingerUpPage)*mPageWidth;
		                }
		                else //return to start position
		                {
		                	if(Math.round(fingerUpPage)==0)//if at beginning, correct
		                	{
		                		//need to correct for when user starts to scroll into 
		                		//nothing then pulls it back a bit, this becomes a 
		                		//kind of backwards scroll instead
		                		edgePosition = (int)(fingerUpPage)*mPageWidth;
		                	}
		                	else //carry on as normal
		                	{
		                		edgePosition = (int)(fingerUpPage+1)*mPageWidth;
		                	}
		                	
		                }
	                	
	                	
	                }
	                
	                smoothScrollTo((int)edgePosition, 0);
	                firstMotionEvent = true;
	                
	                //fire OnPageChangedListener, talk to page control
	                if(mCurrentPage!=(int)(edgePosition/mPageWidth) && (int)(edgePosition/mPageWidth)<getPageCount()) //if the page at the beginning of this action is not equal to page we are now on, i.e. if the page has changed
	                {
	                	//page control
	                	if(mPageControl!=null)
	                	{
	                		mPageControl.setCurrentPage((int)(edgePosition/mPageWidth));
	                	}
	               
	                	//page changed listener 
	                	if(mOnPageChangedListener!=null)
	                	{
	                		mOnPageChangedListener.onPageChanged(mCurrentPage, (int)(edgePosition/mPageWidth));
	                	}
	                }
	                
	                mCurrentPage = (int)(edgePosition/mPageWidth);
	                return true;
			}
			
			return false;
		}
		
	}

	
}	
