package com.red.sandbox.async;

public class TestApp {

    public static void main(String[] args) throws InterruptedException {

        ProcessService.init();

        ExampleProcess p = new ExampleProcess();

        ProcessService.submitProcess(p);

        Thread.sleep(60000);

        System.exit(0);

    }



}
