package com.bonade.xxp.xqc_android_im.imservice.support.audio;

import android.os.Process;

import com.bonade.xxp.xqc_android_im.util.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SpeexEncoder implements Runnable {

    private Logger logger = Logger.getLogger(SpeexEncoder.class);
    private final Object mutex = new Object();
    private Speex speex = new Speex();

    public static int encoderPackageSize = 1024;
    private byte[] processedData = new byte[encoderPackageSize];

    List<ReadData> list = null;
    private volatile boolean isRecording;
    private String fileName;

    public SpeexEncoder(String fileName) {
        super();
        speex.init();
        list = Collections.synchronizedList(new LinkedList<ReadData>());
        this.fileName = fileName;
    }

    @Override
    public void run() {
        SpeexWriter fileWriter = new SpeexWriter(fileName);
        Thread consumerThread = new Thread(fileWriter);
        fileWriter.setRecording(true);
        consumerThread.start();

        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        int getSize = 0;
        while (this.isRecording()) {
            if (list.size() == 0) {
                logger.d("no data need to do encode");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (list.size() > 0) {
                synchronized (mutex) {
                    ReadData rawdata = list.remove(0);
                    short[] playData = new short[rawdata.size];
                    getSize = speex.encode(rawdata.ready, 0, processedData,
                            rawdata.size);
                    logger.i("after encode......................before="
                            + rawdata.size + " after=" + processedData.length
                            + " getsize=" + getSize);
                }
                if (getSize > 0) {
                    fileWriter.putData(processedData, getSize);
                    logger.i("............onLoginOut....................");
                    processedData = new byte[encoderPackageSize];
                }
            }
        }
        logger.d("encode thread exit");
        fileWriter.setRecording(false);
        speex.close();
    }

    public void putData(short[] data, int size) {
        ReadData rd = new ReadData();
        synchronized (mutex) {
            rd.size = size;
            System.arraycopy(data, 0, rd.ready, 0, size);
            list.add(rd);
        }
    }

    public void setRecording(boolean isRecording) {
        synchronized (mutex) {
            this.isRecording = isRecording;
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }

    class ReadData {
        private int size;
        private short[] ready = new short[encoderPackageSize];
    }
}
