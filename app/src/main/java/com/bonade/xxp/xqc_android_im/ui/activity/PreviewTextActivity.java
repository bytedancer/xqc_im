package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.helper.Emoparser;

import butterknife.ButterKnife;

public class PreviewTextActivity extends BaseActivity {

    public static void launch(Context from, String content) {
        Intent intent = new Intent(from, PreviewTextActivity.class);
        intent.putExtra(FRAGMENT_ARGS_PREVIEW_TEXT_CONTENT, content);
        from.startActivity(intent);
    }

    private static final String FRAGMENT_ARGS_PREVIEW_TEXT_CONTENT = "FRAGMENT_ARGS_PREVIEW_TEXT_CONTENT";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_preview_text;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        String displayText = getIntent().getStringExtra(FRAGMENT_ARGS_PREVIEW_TEXT_CONTENT);
        TextView contentView = ButterKnife.findById(this, R.id.tv_content);
        if (!TextUtils.isEmpty(displayText)) {
            contentView.setText(Emoparser.getInstance(this).emoCharsequence(displayText));
        }

        ((View) contentView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewTextActivity.this.finish();
            }
        });
    }
}
