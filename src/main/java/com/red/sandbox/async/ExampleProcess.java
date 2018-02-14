package com.red.sandbox.async;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleProcess extends CallableProcess {


    public ExampleProcess() {
        super();
    }


    @Override
    public Integer call() throws Exception {
        Integer result = new Integer(1);

        log.debug("Running Example Process");

        for (int i=0; i < 10; i++) {
            Thread.sleep(2000);
        }

        // random number between 1 and 1000
        result = (int) (Math.random() * 1000);

        return result;
    }

}
