package com.thor.fireclassification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.Size;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Utils {
    private static final String TAG = "Utils";

    public static String generateRandomUid(){
        String listChars = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        Random rand =  new Random();
        StringBuilder stringBuilder = new StringBuilder();

        while (stringBuilder.length() < 20){
            int index = (int) (rand.nextFloat() * listChars.length());
            stringBuilder.append(listChars.charAt(index));
        }

        return  stringBuilder.toString();

    }

    public static String getCurrentDate(){
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'pukul' HH:mm:ss");
        String currentDate = simpleDateFormat.format(date);

        return  currentDate;
    }

    public static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final Size desiredSize = new Size(width, height);

        boolean exactSizeFound = false;
        float desiredAspectRatio = width * 1.0f / height;
        float bestAspectRatio = 0;
        final List<Size> bigEnough = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {

                exactSizeFound = true;
                break;
            }

            float aspectRatio = option.getWidth() * 1.0f / option.getHeight();
            if (aspectRatio > desiredAspectRatio) continue;

            if (aspectRatio > bestAspectRatio) {
                if (option.getHeight() >= height && option.getWidth() >= width) {
                    bigEnough.clear();
                    bigEnough.add(option);
                    bestAspectRatio = aspectRatio;
                }
            } else if (aspectRatio == bestAspectRatio) {
                if (option.getHeight() >= height && option.getWidth() >= width) {
                    bigEnough.add(option);
                }
            }
        }
        if (exactSizeFound) {
            return desiredSize;
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(
                            (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            });
        } else {
            return choices[0];
        }
    }


    public static void copyFileFromAsset(Context context, String assets_path, String new_path) {
        File father_path = new File(new File(new_path).getParent());
        if (!father_path.exists()) {
            father_path.mkdirs();
        }
        try {
            File new_file = new File(new_path);
            InputStream is_temp = context.getAssets().open(assets_path);
            if (new_file.exists() && new_file.isFile()) {
                if (contrastFileMD5(new_file, is_temp)) {
                    Log.d(TAG, new_path + " is exists!");
                    return;
                } else {
                    Log.d(TAG, "delete old model file!");
                    new_file.delete();
                }
            }
            InputStream is = context.getAssets().open(assets_path);
            FileOutputStream fos = new FileOutputStream(new_file);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            Log.d(TAG, "the model file is copied");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get bin file's md5 string
    private static boolean contrastFileMD5(File new_file, InputStream assets_file) {
        MessageDigest new_file_digest, assets_file_digest;
        int len;
        try {
            byte[] buffer = new byte[1024];
            new_file_digest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(new_file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                new_file_digest.update(buffer, 0, len);
            }

            assets_file_digest = MessageDigest.getInstance("MD5");
            while ((len = assets_file.read(buffer, 0, 1024)) != -1) {
                assets_file_digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String new_file_md5 = new BigInteger(1, new_file_digest.digest()).toString(16);
        String assets_file_md5 = new BigInteger(1, assets_file_digest.digest()).toString(16);
        Log.d("new_file_md5", new_file_md5);
        Log.d("assets_file_md5", assets_file_md5);
        return new_file_md5.equals(assets_file_md5);
    }

    public static ArrayList<String> ReadListFromFile(AssetManager assetManager, String filePath) {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader;
        InputStream istr = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(assetManager.open(filePath)));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
