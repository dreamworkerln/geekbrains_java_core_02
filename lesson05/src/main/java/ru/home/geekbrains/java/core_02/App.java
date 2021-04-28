package ru.home.geekbrains.java.core_02;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Hello world!
 *
 */
public class App
{


    static class CalcThread extends Thread {

        private float[] data;

        CalcThread(float[] data) {
            this.data = data;
        }



        @Override
        public void run() {

            for (int i = 0; i < data.length; i++) {
                data[i] = (float) (data[i] * Math.sin(0.2f + i / 5) *
                                   Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
            }
        }
    }

    // ============================================================

    public static void main(String[] args){

        new App().start();
    }


    // ------------------------------------------------------------


    private long method01(float[] data) {

        long t = System.currentTimeMillis();

        CalcThread th1 = new CalcThread(data);
        th1.run();

        return System.currentTimeMillis() - t;

    }


    private long method02(float[] data) {

        long t = System.currentTimeMillis();

        final int h = data.length / 2;
        float[] dat1 =  Arrays.copyOfRange(data, 0, h);
        float[] dat2 =  Arrays.copyOfRange(data, h, data.length);

        CalcThread t1 = new CalcThread(dat1);
        CalcThread t2 = new CalcThread(dat2);
        t1.start();
        t2.start();

        try {

            t1.join();
            t2.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.arraycopy(dat1, 0, data, 0, h);
        System.arraycopy(dat2, 0, data, h, h);


        return System.currentTimeMillis() - t;
    }



    private void start() {

        int size = 10000000;
        float[] data;
        long t;

        // method01
        System.out.print("Starting method01 ... ");
        data = new float[size];
        Arrays.fill(data, 1);
        t = method01(data);
        System.out.println(t);

        // method02
        System.out.print("Starting method02 ... ");
        data = new float[size];
        Arrays.fill(data, 1);
        t = method02(data);
        System.out.println(t);
    }

}
