package com.njdp.njdp_farmer.MyClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/7/25.
 * 压缩图片的大小
 */
public class PictureCompress {

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1; if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //设置为true，只读尺寸信息，不加载像素信息到内存
        BitmapFactory.decodeFile(filePath, options); //此时bitmap为空
        options.inJustDecodeBounds = false;
        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 1080, 1920);
        //Decode bitmap with inSampleSize set options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //获得图片压缩后的存储路径
    public static File getSmallImageFile(String filePath){
        Bitmap bm = getSmallBitmap(filePath);
        File outputFile, inputFile = new File(filePath);
        int size = getBitmapSize(bm), quality = 100;
        if(size > 12000000){
            quality = 12000000 * 100 / size;
        }else {
            return inputFile; //图片大小符合要求（500K以内），直接返回
        }

        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            outputFile = new File(Environment.getExternalStorageDirectory() + "/NJDP/Image/" + inputFile.getName());
            Log.d("dir", filePath);
        } else
            outputFile = new File(Environment.getDownloadCacheDirectory() + "/NJDP/Image/" + inputFile.getName());
        try {
            if(!outputFile.exists())
                if(!outputFile.getParentFile().exists()){
                    if(!outputFile.getParentFile().mkdirs()){
                        return null;
                    }
                }
                if(outputFile.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    bm.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                    fos.close();
                }
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!bm.isRecycled()) {
            bm.recycle();
        }
        return outputFile;
    }

    //把bitmap转换成String
    public static String bitmapToString(String filePath) {
        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /*
    压缩图片，处理某些手机拍照角度旋转的问题
    */
    public static String compressImage(Context context,String filePath,String fileName,int q) throws FileNotFoundException {
        Bitmap bm = getSmallBitmap(filePath);
        int degree = readPictureDegree(filePath);
        if(degree!=0) {
            //旋转照片角度
            bm = rotateBitmap(bm, degree);
        }
        File imageDir = Environment.getDownloadCacheDirectory() ;
        File outputFile=new File(imageDir,fileName);
        FileOutputStream out = new FileOutputStream(outputFile);
        bm.compress(Bitmap.CompressFormat.JPEG, q, out);
        return outputFile.getPath();
    }

    //判断照片角度
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    //旋转照片
    public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return null;
    }

    //获取图片大小
    public static int getBitmapSize(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){   //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){  //API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }
}
