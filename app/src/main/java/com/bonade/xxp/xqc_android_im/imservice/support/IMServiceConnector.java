package com.bonade.xxp.xqc_android_im.imservice.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.util.Logger;

public abstract class IMServiceConnector {

    protected static Logger logger = Logger.getLogger(IMServiceConnector.class);

    public abstract void onIMServiceConnected();
    public abstract void onServiceDisconnected();

    private IMService imService;
    public IMService getIMService() {
        return imService;
    }

    // todo when to release?
    private ServiceConnection imServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.i("im#onService(imService)Connected");
            if (imService == null) {
                IMService.IMServiceBinder binder = (IMService.IMServiceBinder) service;
                imService = binder.getService();

                if (imService == null) {
                    logger.e("im#get imService failed");
                    return;
                }
                logger.d("im#get imService ok");
            }
            IMServiceConnector.this.onIMServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.i("onService(imService)Disconnected");
            IMServiceConnector.this.onServiceDisconnected();
        }
    };

    public boolean connect(Context context) {
        return bindService(context);
    }

    public void disconnect(Context context) {
        logger.d("im#disconnect");
        unbindService(context);
        IMServiceConnector.this.onServiceDisconnected();
    }

    public boolean bindService(Context context) {
        logger.d("im#bindService");

        Intent intent = new Intent(context, IMService.class);
        if (!context.bindService(intent, imServiceConnection, Context.BIND_AUTO_CREATE)) {
            logger.e("im#bindService(imService) failed");
            return false;
        } else {
            logger.i("im#bindService(imService) ok");
            return true;
        }
    }

    public void unbindService(Context context) {
        try {
            // todo check the return value .check the right place to call it
            context.unbindService(imServiceConnection);
        } catch (IllegalArgumentException exception) {
            logger.w("im#got exception becuase of unmatched bind/unbind, we sould place to onStop next version.e:%s", exception.getMessage());
        }
        logger.i("unbindservice ok");
    }

}
