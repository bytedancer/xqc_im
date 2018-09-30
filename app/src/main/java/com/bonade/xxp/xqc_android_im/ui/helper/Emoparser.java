package com.bonade.xxp.xqc_android_im.ui.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Emoparser {

    private static Logger logger = Logger.getLogger(Emoparser.class);

    private Context context;
    private String[] emoList;
    private Pattern mPattern;
    private static HashMap<String, Integer> phraseIdMap;
    private static HashMap<Integer, String> idPhraseMap;
    private static Emoparser instance = null;

    private final int DEFAULT_SMILEY_TEXTS = R.array.default_emo_phrase;
    private final int[] DEFAULT_EMO_RES_IDS = {
            R.mipmap.im_e0, R.mipmap.im_e1, R.mipmap.im_e2, R.mipmap.im_e3,
            R.mipmap.im_e4, R.mipmap.im_e5, R.mipmap.im_e6, R.mipmap.im_e7,
            R.mipmap.im_e8, R.mipmap.im_e9, R.mipmap.im_e10, R.mipmap.im_e11,
            R.mipmap.im_e12, R.mipmap.im_e13, R.mipmap.im_e14, R.mipmap.im_e15,
            R.mipmap.im_e16, R.mipmap.im_e17, R.mipmap.im_e18, R.mipmap.im_e19,
            R.mipmap.im_e20, R.mipmap.im_e21, R.mipmap.im_e22, R.mipmap.im_e23,
            R.mipmap.im_e24, R.mipmap.im_e25, R.mipmap.im_e26, R.mipmap.im_e27,
            R.mipmap.im_e28, R.mipmap.im_e29, R.mipmap.im_e30, R.mipmap.im_e31,
            R.mipmap.im_e32, R.mipmap.im_e33, R.mipmap.im_e34, R.mipmap.im_e35,
            R.mipmap.im_e36, R.mipmap.im_e37, R.mipmap.im_e38, R.mipmap.im_e39,
            R.mipmap.im_e40, R.mipmap.im_e41, R.mipmap.im_e42, R.mipmap.im_e43,
            R.mipmap.im_e44, R.mipmap.im_e45, R.mipmap.im_e46, R.mipmap.im_e47,
            R.mipmap.im_e48, R.mipmap.im_e49, R.mipmap.im_e50, R.mipmap.im_e51,
            R.mipmap.im_e52, R.mipmap.im_e53, R.mipmap.im_e54
    };

    private Emoparser(Context context) {
        this.context = context;
        emoList = context.getResources().getStringArray(DEFAULT_SMILEY_TEXTS);
        buildMap();
        mPattern = buildPattern();
    }

    public static synchronized Emoparser getInstance(Context context) {
        if (null == instance && null != context) {
            instance = new Emoparser(context);
        }
        return instance;
    }

    public int[] getResIdList() {
        return DEFAULT_EMO_RES_IDS;
    }

    private void buildMap() {
        if (DEFAULT_EMO_RES_IDS.length != emoList.length) {
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        phraseIdMap = new HashMap<>(emoList.length);
        idPhraseMap = new HashMap<>(emoList.length);
        for (int i = 0; i < emoList.length; i++) {
            phraseIdMap.put(emoList[i], DEFAULT_EMO_RES_IDS[i]);
            idPhraseMap.put(DEFAULT_EMO_RES_IDS[i], emoList[i]);
        }
    }

    public HashMap<String, Integer> getPhraseIdMap() {
        return phraseIdMap;
    }

    public HashMap<Integer, String> getIdPhraseMap() {
        return idPhraseMap;
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(emoList.length * 3);
        patternString.append('(');
        for (String s : emoList) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1,
                patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    public CharSequence emoCharsequence(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = phraseIdMap.get(matcher.group());
            Drawable drawable = context.getResources().getDrawable(resId);
            int size = (int) (CommonUtil.getElementSzie(context) * 0.8);
            drawable.setBounds(0, 0, size, size);
            ImageSpan imageSpan = new ImageSpan(drawable,
                    ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
}
