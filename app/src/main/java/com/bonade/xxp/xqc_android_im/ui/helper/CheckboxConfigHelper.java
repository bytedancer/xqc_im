package com.bonade.xxp.xqc_android_im.ui.helper;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bonade.xxp.xqc_android_im.DB.sp.ConfigurationSp;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.util.Logger;

import java.util.HashSet;

public class CheckboxConfigHelper {

    private Logger logger = Logger.getLogger(CheckboxConfigHelper.class);
    private ConfigurationSp configMamager;

    public CheckboxConfigHelper() {

    }

    public void init(ConfigurationSp configMamager) {
        this.configMamager = configMamager;
    }

    public void initTopCheckBox(final CheckBox checkBox, final String sessionKey) {
        if (configMamager == null || checkBox == null) {
            logger.e("config#configMamager is null");
            return;
        }
        boolean shouldCheck = false;
        HashSet<String> topList = configMamager.getSessionTopList();
        if (topList != null && !topList.isEmpty()) {
            shouldCheck = topList.contains(sessionKey);
        }
        checkBox.setChecked(shouldCheck);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configMamager.setSessionTop(sessionKey, checkBox.isChecked());
            }
        });
    }

    public void initCheckBox(CheckBox checkBox, String key, ConfigurationSp.ConfigDimension dimension) {
        handleCheckBoxChanged(checkBox, key, dimension);
        configCheckBox(checkBox, key, dimension);
    }

    private void configCheckBox(CheckBox checkBox, String key, ConfigurationSp.ConfigDimension dimension) {
        if (configMamager == null) {
            logger.e("config#configMamager is null");
            return;
        }

        boolean shouldCheck = configMamager.getConfig(key, dimension);
        logger.d("config#%s is set %s", dimension, shouldCheck);
        checkBox.setChecked(shouldCheck);
    }

    private void handleCheckBoxChanged(final CheckBox checkBox, final String key, final ConfigurationSp.ConfigDimension dimension) {
        if (checkBox == null || configMamager == null) {
            return;
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configMamager.setConfig(key, dimension, checkBox.isChecked());
            }
        });
    }
}
