package com.linguaculturalists.phoenicia.components;

import android.content.Context;

import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.orm.androrm.QuerySet;

import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 4/17/17.
 */
public class GiftCode {

    public static final int CODE_LENGTH = 6;
    public static int[] toArray(int code) {
        int[] numbers = new int[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            int power = (int) (Math.pow(10, CODE_LENGTH - i - 1));
            Debug.d("Showing request code position: " + power);
            int digit = code / power;
            numbers[i] = digit;
            code -= digit * power;
        }
        return numbers;
    }

    public static int encode(final int checkKey, final int rawData) {
        int encData = rawData;
        // Encode item data with the check key
        int encKey = checkKey * checkKey;
        encData = rawData ^ encKey;
        return encData;
    }

    public static int decode(final int checkKey, final int encData) {
        int rawData = encData;
        // Decode item data with the check key
        int encKey = checkKey * checkKey;
        rawData = encData ^ encKey;
        return rawData;
    }

}
