package com.bonade.xxp.xqc_android_im.util;

import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.SearchElement;
import com.bonade.xxp.xqc_android_im.util.pinyin.PinYin;

public class IMUIHelper {


    public static boolean handleGroupSearch(String key, GroupEntity group) {
        if (TextUtils.isEmpty(key) || group == null) {
            return false;
        }
        group.getSearchElement().reset();

        return handleTokenFirstCharsSearch(key, group.getPinyinElement(), group.getSearchElement())
                || handleTokenPinyinFullSearch(key, group.getPinyinElement(), group.getSearchElement())
                || handleNameSearch(group.getMainName(), key, group.getSearchElement());
    }

    public static boolean handleNameSearch(String name, String key,
                                           SearchElement searchElement) {
        int index = name.indexOf(key);
        if (index == -1) {
            return false;
        }

        searchElement.startIndex = index;
        searchElement.endIndex = index + key.length();

        return true;
    }

    public static boolean handleTokenFirstCharsSearch(String key, PinYin.PinYinElement pinYinElement, SearchElement searchElement) {
        return handleNameSearch(pinYinElement.tokenFirstChars, key.toUpperCase(), searchElement);
    }

    public static boolean handleTokenPinyinFullSearch(String key, PinYin.PinYinElement pinYinElement, SearchElement searchElement) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        String searchKey = key.toUpperCase();

        //onLoginOut the old search result
        searchElement.reset();

        int tokenCnt = pinYinElement.tokenPinyinList.size();
        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < tokenCnt; ++i) {
            String tokenPinyin = pinYinElement.tokenPinyinList.get(i);

            int tokenPinyinSize = tokenPinyin.length();
            int searchKeySize = searchKey.length();

            int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
            String keyPart = searchKey.substring(0, keyCnt);

            if (tokenPinyin.startsWith(keyPart)) {

                if (startIndex == -1) {
                    startIndex = i;
                }

                endIndex = i + 1;
            } else {
                continue;
            }

            if (searchKeySize <= tokenPinyinSize) {
                searchKey = "";
                break;
            }

            searchKey = searchKey.substring(keyCnt, searchKeySize);
        }

        if (!searchKey.isEmpty()) {
            return false;
        }

        if (startIndex >= 0 && endIndex > 0) {
            searchElement.startIndex = startIndex;
            searchElement.endIndex = endIndex;

            return true;
        }

        return false;
    }

    // 这个还是蛮有用的,方便以后的替换
    public static int getDefaultAvatarResId(int sessionType) {
//        if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
//            return R.drawable.tt_default_user_portrait_corner;
//        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
//            return R.drawable.group_default;
//        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
//            return R.drawable.discussion_group_default;
//        }
//
//        return R.drawable.tt_default_user_portrait_corner;
        return 1;
    }
}
