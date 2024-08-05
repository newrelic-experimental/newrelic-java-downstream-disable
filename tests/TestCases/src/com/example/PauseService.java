package com.example;

import java.util.Random;

public class PauseService {

    private static final long UNIT = 100L;
    private static final long SECOND = 1000L;

    private static final Random random = new Random();

    public static void pause(long ms) {
     try {
        Thread.sleep(ms);         
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void pauseRandomUnits() {
        int n = random.nextInt(25);
        pause(n * UNIT);
    }

    public static void pauseRandomSeconds() {
        int n = random.nextInt(15);
        pause(n*SECOND);
    }

 }
