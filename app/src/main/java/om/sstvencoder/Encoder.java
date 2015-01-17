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

import java.util.LinkedList;
import java.util.List;

import om.sstvencoder.Modes.MartinModes.*;
import om.sstvencoder.Modes.*;
import om.sstvencoder.Modes.ScottieModes.*;

public class Encoder {
    private final Thread mThread;
    private final List<Mode> mQueue;
    private boolean mQuit;
    private Class mModeClass;

    public Encoder() {
        mQueue = new LinkedList<>();
        mQuit = false;
        mModeClass = Robot36.class;

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

    public ModeSize setMartin1() {
        return setMode(Martin1.class);
    }

    public ModeSize setMartin2() {
        return setMode(Martin2.class);
    }

    public ModeSize setScottie1() {
        return setMode(Scottie1.class);
    }

    public ModeSize setScottie2() {
        return setMode(Scottie2.class);
    }

    public ModeSize setScottieDX() {
        return setMode(ScottieDX.class);
    }

    public ModeSize setRobot36() {
        return setMode(Robot36.class);
    }

    public ModeSize setRobot72() {
        return setMode(Robot72.class);
    }

    public ModeSize setWrasse() {
        return setMode(Wrasse.class);
    }

    private ModeSize setMode(Class modeClass) {
        mModeClass = modeClass;
        if (mModeClass.isAnnotationPresent(ModeSize.class))
            return (ModeSize) mModeClass.getAnnotation(ModeSize.class);
        return null;
    }

    public void send(Bitmap bitmap) {
        Mode mode = Mode.Create(mModeClass, bitmap);
        if (mode != null)
            enqueue(mode);
    }

    private void enqueue(Mode mode) {
        synchronized (mThread) {
            mQueue.add(mode);
            mThread.notify();
        }
    }

    public void destroy() {
        synchronized (mThread) {
            mQuit = true;
            mThread.notify();
        }
    }
}
