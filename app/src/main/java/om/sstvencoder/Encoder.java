/*
Copyright 2014 Olga Miller <olga.rgb@googlemail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package om.sstvencoder;

import android.graphics.Bitmap;

import om.sstvencoder.Modes.MartinModes.*;
import om.sstvencoder.Modes.*;

import java.util.LinkedList;
import java.util.List;

public class Encoder {
    private final Thread mThread;
    private final List<Mode> mQueue;
    private boolean mQuit;

    public Encoder() {
        mQueue = new LinkedList<>();
        mQuit = false;

        mThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    Mode mode;
                    synchronized (this) {
                        while (mQueue.isEmpty() && !mQuit) {
                            try {
                                wait();
                            } catch (Exception ignore) {
                            }
                        }
                        if (mQuit)
                            return;
                        mode = mQueue.remove(0);
                    }
                    mode.init();

                    while (mode.process()) {
                        synchronized (this) {
                            if (mQuit)
                                break;
                        }
                    }
                    mode.finish();
                }
            }
        };
        mThread.start();
    }

    public Bitmap sendMartin1(Bitmap bitmap) {
        return enqueue(new Martin1(bitmap));
    }

    public Bitmap sendMartin2(Bitmap bitmap) {
        return enqueue(new Martin2(bitmap));
    }

    public Bitmap sendRobot36(Bitmap bitmap) {
        return enqueue(new Robot36(bitmap));
    }

    public Bitmap sendRobot72(Bitmap bitmap) {
        return enqueue(new Robot72(bitmap));
    }

    public Bitmap sendWrasse(Bitmap bitmap) {
        return enqueue(new Wrasse(bitmap));
    }

    private Bitmap enqueue(Mode mode) {
        Bitmap nativeBitmap = mode.getBitmap();
        synchronized (mThread) {
            mQueue.add(mode);
            mThread.notify();
        }
        return nativeBitmap;
    }

    public void destroy() {
        synchronized (mThread) {
            mQuit = true;
            mThread.notify();
        }
    }
}
