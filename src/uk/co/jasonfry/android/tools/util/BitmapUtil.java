package uk.co.jasonfry.android.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;

public final class BitmapUtil 
{
	public static Bitmap decodeFile(String filepath, int size, boolean square)
	{
		return decodeFile(new File(filepath),size,square);
	}
	
	public static Bitmap decodeFile(File file, int size, boolean square)
	{
		try 
        {
            //decode image size
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            Bitmap bitmapSize = BitmapFactory.decodeStream(new FileInputStream(file),null,bitmapOptions);
            
            //Find the correct scale value. It should be the power of 2.
            if(size>0)
            {
	            int width_tmp=bitmapOptions.outWidth, height_tmp=bitmapOptions.outHeight;
	            int scale = 1;
	            while(true)
	            {
	                if(width_tmp/2<size || height_tmp/2<size)
	                    break;
	                width_tmp/=2;
	                height_tmp/=2;
	                scale++;
	            }
				//decode with inSampleSize
	            bitmapOptions = new BitmapFactory.Options();
	            bitmapOptions.inSampleSize=scale;
				bitmapOptions.inScaled = true;
				bitmapSize = null;
				if(square)
	            {
	            	return cropToSquare(BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions));
	            }
	            return BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);
            }
            return null;
        } 
        catch (FileNotFoundException e) 
        {
        	e.printStackTrace();
        }
        return null;
	}
	
	public static Bitmap cropToSquare(Bitmap bitmap)
	{
		if(bitmap!=null)//make a square!
	    {
		    if(bitmap.getWidth()>bitmap.getHeight())
			{
				return Bitmap.createBitmap(bitmap, ((bitmap.getWidth()-bitmap.getHeight())/2), 0, bitmap.getHeight(), bitmap.getHeight());
			}
			else if(bitmap.getWidth()<bitmap.getHeight())
			{
				return Bitmap.createBitmap(bitmap, 0,((bitmap.getHeight()-bitmap.getWidth())/2), bitmap.getWidth(), bitmap.getWidth());
			}
		    //else if they are equal, do nothing!
	    }
		return bitmap;
	}
	
	public static Bitmap getThumbnail(ContentResolver contentResolver, long id)
	{
		Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	             	new String[]{MediaStore.Images.Media.DATA}, // Which columns to return 
	             	MediaStore.Images.Media._ID+ "=?",       // Which rows to return 
	             	new String[]{String.valueOf(id)},       // Selection arguments
	             	null);// order
		
		if(cursor!=null && cursor.getCount()>0)
		{
			cursor.moveToFirst();
			String filepath = cursor.getString(0);
			cursor.close();
			int rotation = 0;
			
			try
			{
				ExifInterface exifInterface = new ExifInterface(filepath);
				int exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);

				if(exifRotation!=ExifInterface.ORIENTATION_UNDEFINED)
				{
					switch(exifRotation)
					{
						case ExifInterface.ORIENTATION_ROTATE_180 :
							rotation = 180;
							break;
						case ExifInterface.ORIENTATION_ROTATE_270 :
							rotation = 270;
							break;
						case ExifInterface.ORIENTATION_ROTATE_90 :
							rotation = 90;
							break;
					}
				}
			}
			catch(java.io.IOException e){}
			
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
			
			if(rotation!=0)
			{
				Matrix matrix = new Matrix();
				matrix.setRotate(rotation);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			}
			
			return bitmap;
		}
		else
		{
			return null;
		}
	}
}
