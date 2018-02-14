package com.red.sandbox.async;

import java.util.UUID;
import java.util.concurrent.Callable;

public abstract class CallableProcess implements Callable<Integer> {

    private String transactionId;

    public CallableProcess() {
        transactionId = UUID.randomUUID().toString();
    }




    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

}
