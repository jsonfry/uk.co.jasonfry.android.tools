package uk.co.jasonfry.android.tools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import uk.co.jasonfry.android.tools.ui.SwipeView;

/**
 * Problems will arise if you change the carousel mode after setting an adapter
 */
public class PageView extends BounceSwipeView
{
	private Adapter mAdapter;
	private OnPageChangedListener mOnPageChangedListener; //as we are implementing our own OnPageChangedListener we need to keep the one set by a class implementing a CarouseView and then call it after we've done our bits. 
	private int mCurrentPage;
	private int mOffset;
	private boolean mCarouselMode = false;
	
	public PageView(Context context) 
	{
		super(context);
		initView();
	}
	
	public PageView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		initView();
	}
	
	public PageView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		initView();
	}
	
	private void initView()
	{
		setBounceEnabled(false);
	}
	
	/**
	 * The page being shown on the screen, rather than the underlying page in the SwipeView structure ( getCurrentPage() )
	 * @return
	 */
	public int getRealCurrentPage()
	{
		return mCurrentPage;
	}
	
	@Override
	public int setPageWidth(int pageWidth)
	{
		mOffset = super.setPageWidth(pageWidth);
		return mOffset;
	}
	
	public void setCarouselEnabled(boolean enabled)
	{
		mCarouselMode = enabled;
		setBounceEnabled(!enabled);
	}
	
	public boolean getCarouselEnabled()
	{
		return mCarouselMode;
	}
	
	public void setAdapter(BaseAdapter adapter)
	{
		setAdapter(adapter, 0);
	}
	
	public void setAdapter(BaseAdapter adapter, final int startPosition)
	{
		mAdapter = adapter;
		if(mAdapter!=null)
		{
			mCurrentPage=startPosition;
			fillCarousel(startPosition);
			post(new Runnable()
			{
				public void run()
				{
					if(!mCarouselMode && startPosition==0)
					{
						PageView.super.scrollToPage(0);
					}
					else if(!mCarouselMode && startPosition == mAdapter.getCount()-1)
					{
						PageView.super.scrollToPage(2);
					}
					else
					{
						PageView.super.scrollToPage(1);
					}
				}
			});

			if(mAdapter.getCount()<=1)
			{
				setBounceEnabled(true);
			}
		}
	}
	
	public Adapter getAdapter()
	{
		return mAdapter;
	}
	
	//we call this locally because we do a sneaky sneaky for 2 pages. we double it so it behave like it has four pages instead e.g. [1][2] becomes [1][2][1][2]. We then correct for this in the loadPage method
	private int getAdapterPageCount()
	{
		if(mAdapter!=null)
		{
			if(mAdapter.getCount()==2 && mCarouselMode)
			{
				return 4;
			}
			else
			{
				return mAdapter.getCount();
			}	
		}
		else
		{
			return -1;
		}
	}
	
	private void emptyCarousel()
	{
		getChildContainer().removeAllViews();
	}
	
	private void fillCarousel(int page)
	{
		emptyCarousel();
		if(mAdapter.getCount() == 1)
		{
			loadPage(0,0,null);//TODO make this actually support one page only.
		}
		else if(mAdapter.getCount() == 2)
		{
			if(!mCarouselMode)
			{
				loadPage(0,0,null);
				loadPage(1,1,null);
			}
			else if(page==0)
			{
				loadPage(1,0,null);
				loadPage(0,1,null);
				loadPage(1,2,null);
			}
			else //page==1 (hopefully)
			{
				loadPage(0,0,null);
				loadPage(1,1,null);
				loadPage(0,2,null);
			}
		}
		else if(mAdapter.getCount() > 2)
		{
			if(page==0 && mCarouselMode)//if first page, need to handle loading last page
			{
				loadPage(mAdapter.getCount()-1,0,null);
				loadPage(0,1,null);
				loadPage(1,2,null);
			}
			else if(page==0 && !mCarouselMode)
			{
				loadPage(0,0,null);
				loadPage(1,1,null);
				loadPage(2,2,null);
			}
			else if(page==mAdapter.getCount()-1 && mCarouselMode)//if last page, need to handle loading first page
			{
				loadPage(page-1,0,null);
				loadPage(mAdapter.getCount()-1,1,null);
				loadPage(0,2,null);
			}
			else if(page==mAdapter.getCount()-1 && !mCarouselMode)
			{
				loadPage(mAdapter.getCount()-3,0,null);
				loadPage(mAdapter.getCount()-2,1,null);
				loadPage(mAdapter.getCount()-1,2,null);
			}
			else //get on with it
			{
				loadPage(page-1,0,null);
				loadPage(page,1,null);
				loadPage(page+1,2,null);
			}
			resetMargins();
		}
	}
	
	@Override
	public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener)
	{
		mOnPageChangedListener = onPageChangedListener;
	}
	
	@Override 
	public OnPageChangedListener getOnPageChangedListener()
	{
		return mOnPageChangedListener;
	}
	
	private void loadPage(int page, int position, View convertView)
	{
		if(mAdapter.getCount()==2 && page>1)
		{
			page=page-2;
		}
		super.addView(mAdapter.getView(page, convertView, getChildContainer()), position);
	}
	
	@Override
	public void smoothScrollToPage(int page)
	{
		scrollToPage(page,true);
	}
	
	@Override 
	public void scrollToPage(int page)
	{
		scrollToPage(page,false);
	}
	
	private void scrollToPage(int page, boolean smooth) 
	{
		if(!mCarouselMode && getCurrentPage()==getPageCount()-1 && page>=getCurrentPage() ||
			!mCarouselMode && getCurrentPage()==0 && page<=0)
		{
			doAtEdgeAnimation();
		}
		else if(getCurrentPage()!=page)
		{
			rearrangePages(getCurrentPage(), page, smooth);
			notifiyAssignedOnPageChangedListener(page);
		}
	}
	
	private void notifiyAssignedOnPageChangedListener(int newPage)
	{
		//call the OnPageChangedListener that might have been set by a class implementing a PageView
		if(mOnPageChangedListener !=null)
		{
			if(mCarouselMode && mCurrentPage==0 && newPage==2) //looping round to end from begining 
			{
				mOnPageChangedListener.onPageChanged(mAdapter.getCount()-1, mCurrentPage);
			}
			else if(mCarouselMode && mCurrentPage==mAdapter.getCount()-1 && newPage==0) //looping round from end back to begining
			{
				mOnPageChangedListener.onPageChanged(0, mCurrentPage);
			}
			else if(!mCarouselMode && mCurrentPage==1 && newPage==1) //going from first page into second, not using carousel
			{
				mOnPageChangedListener.onPageChanged(0,1);
			}
			else if(!mCarouselMode && mCurrentPage==mAdapter.getCount()-1 && newPage==mAdapter.getCount()-1)
			{
				mOnPageChangedListener.onPageChanged(mCurrentPage,mAdapter.getCount()-2);
			}
			else if(newPage==2) //going forwards
			{
				mOnPageChangedListener.onPageChanged(mCurrentPage-1, mCurrentPage);
			}
			else //if(newPage==0) //going backwards
			{
				mOnPageChangedListener.onPageChanged(mCurrentPage+1, mCurrentPage);
			}
		}
	}
	
	private void rearrangePages(int oldPage, int newPage, final boolean smooth)
	{
		//do the clever loading / moving pages thingy...
		if(getAdapterPageCount()>1)
		{
			final int pageToScrollTo;
			if(newPage>=oldPage+1)//going forwards
			{
				if(mCarouselMode || mCurrentPage<getAdapterPageCount()-2 && mCurrentPage>0)
				{
					mCallScrollToPageInOnLayout = false;
					scrollTo(getScrollX()-getPageWidth(),0);
					forwardsMove();
					pageToScrollTo = 1;
				}
				else if(mCurrentPage<=0)
				{
					mCurrentPage=1;
					pageToScrollTo=1;
				}
				else
				{
					mCurrentPage=getAdapterPageCount()-1;
					pageToScrollTo=2;
				}
			}
			else if(newPage<=oldPage-1)//going backwards
			{
				if(mCarouselMode || mCurrentPage>1 && mCurrentPage<getAdapterPageCount()-1)
				{
					mCallScrollToPageInOnLayout = false;
					scrollTo(getScrollX()+getPageWidth(),0);
					backwardsMove();
					pageToScrollTo = 1;
				}
				else if(mCurrentPage>=getAdapterPageCount()-1)
				{
					mCurrentPage=getAdapterPageCount()-2;
					pageToScrollTo=1;
				}
				else
				{
					mCurrentPage=0;
					pageToScrollTo=0;
				}
			}
			else //doesn't do anything, needed to make it compile... 
			{
				pageToScrollTo = 1;
			}
			
			post(new Runnable()
			{
				public void run()
				{
					if(smooth)
					{
						PageView.super.smoothScrollToPage(pageToScrollTo);
					}
					else
					{
						PageView.super.scrollToPage(pageToScrollTo);
					}
				}
			});
		}
	}
	
	private void forwardsMove()
	{
		if(mCurrentPage<getAdapterPageCount()-1)
		{
			mCurrentPage++;
		}
		else
		{
			mCurrentPage=0;
		}
		
		if(mCurrentPage<getAdapterPageCount()-1)
		{
			forwardsRearrange(mCurrentPage+1);
		}
		else
		{
			forwardsRearrange(0);
		}
	}
	
	private void backwardsMove()
	{
		if(mCurrentPage>0)
		{
			mCurrentPage--;
		}
		else
		{
			mCurrentPage=getAdapterPageCount()-1;
		}
		
		if(mCurrentPage>0)
		{
			backwardsRearrange(mCurrentPage-1);
		}
		else
		{
			backwardsRearrange(getAdapterPageCount()-1);
		}
	}
	
	private void forwardsRearrange(int frontPageToLoad)
	{
		//Lose '0', move '1' into '0', move '2' into '1', and load in new '2'
		View convertView = getChildContainer().getChildAt(0);
		getChildContainer().removeViewAt(0);
		loadPage(frontPageToLoad,2,convertView);
		resetMargins();
	}
	
	private void backwardsRearrange(int backPageToLoad)
	{
		View convertView = getChildContainer().getChildAt(2);
		getChildContainer().removeViewAt(2);
		loadPage(backPageToLoad,0,convertView);
		resetMargins();
	}
	
	private void resetMargins()
	{
		if(mOffset >0)
		{
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(0).getLayoutParams()).leftMargin = mOffset;
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(0).getLayoutParams()).rightMargin = 0;
			
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(1).getLayoutParams()).leftMargin = 0;
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(1).getLayoutParams()).rightMargin = 0;
			
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(2).getLayoutParams()).leftMargin = 0;
			((LinearLayout.LayoutParams)getChildContainer().getChildAt(2).getLayoutParams()).rightMargin = mOffset;
		}
	}
	
	public void itemAddedToAdapter(int position)
	{
		if(position<=mCurrentPage)
		{
			mCurrentPage++;
		}
		if(mAdapter.getCount()>1)
		{
			setBounceEnabled(false);
		}
		
		refill(position);
	}
	
	public void itemRemovedFromAdapter(int position)
	{
		if(position<=mCurrentPage && mCurrentPage!=0)
		{
			mCurrentPage--;
		}
		
		refill(position);
	}
	
	private void refill(int position)
	{
		if(mCurrentPage == 0)//if it could be an edge case at start
		{
			if(position == getAdapterPageCount()-1 || position<=mCurrentPage+1)//if we need to refill carousel
			{
				fillCarousel(mCurrentPage);
			}
		}
		else if(mCurrentPage == getAdapterPageCount()-1)//if it could be an edge case at end
		{
			if(position >= mCurrentPage || position ==0)//if we need to refill carousel
			{
				fillCarousel(mCurrentPage);
			}
		}
		else //not an edge case
		{
			if(position >= mCurrentPage-1 && position <= mCurrentPage+1)//if we need to refill carousel
			{
				fillCarousel(mCurrentPage);
			}
		}
	}
}
