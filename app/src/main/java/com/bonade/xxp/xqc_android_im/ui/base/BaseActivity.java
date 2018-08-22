package com.bonade.xxp.xqc_android_im.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity {

    protected final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        } else {
            throw new IllegalArgumentException("You must return a right contentView layout resource Id");
        }
        ButterKnife.bind(this);
        BaseAppManager.getInstance().addActivity(this);
        setupViews(savedInstanceState);
    }

    @Override
    public void finish() {
        super.finish();
        BaseAppManager.getInstance().removeActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getLayoutId();

    protected abstract void setupViews(Bundle savedInstanceState);
}
