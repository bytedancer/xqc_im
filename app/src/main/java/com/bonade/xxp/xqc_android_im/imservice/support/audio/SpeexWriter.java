package com.bonade.xxp.xqc_android_im.imservice.support.audio;

import com.bonade.xxp.xqc_android_im.util.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SpeexWriter implements Runnable {

    private Logger logger = Logger.getLogger(SpeexWriter.class);
    private final Object mutex = new Object();

    public static int writePackageSize = 1024;

    private SpeexWriteClient client = new SpeexWriteClient();
    private volatile boolean isRecording;
    private ProcessedData pData;
    private List<ProcessedData> list;

    public SpeexWriter(String fileName) {
        super();
        list = Collections.synchronizedList(new LinkedList<ProcessedData>());
        client.setSampleRate(8000);

        client.start(fileName);
    }

    @Override
    public void run() {
        logger.d("write thread runing");
        while (this.isRecording() || list.size() > 0) {

            if (list.size() > 0) {
                pData = list.remove(0);
                // gauss_packageSize/2
                logger.i("pData size=" + pData.size);

                client.writeTag(pData.processed, pData.size);

                logger.d("list size = {}" + list.size());
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        logger.d("write thread exit");
        stop();
    }

    public void putData(final byte[] buf, int size) {

        logger.d("after convert. size=====================[640]:" + size);

        ProcessedData data = new ProcessedData();
        // data.ts = ts;
        data.size = size;
        System.arraycopy(buf, 0, data.processed, 0, size);
        list.add(data);
    }

    public void stop() {
        client.stop();
    }

    public void setRecording(boolean isRecording) {
        synchronized (mutex) {
            this.isRecording = isRecording;
            if (this.isRecording) {
                mutex.notify();
            }
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }

    class ProcessedData {
        // private long ts;
        private int size;
        private byte[] processed = new byte[writePackageSize];
    }

}
