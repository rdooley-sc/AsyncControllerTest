package com.red.sandbox.async;


import lombok.Data;

import java.util.concurrent.Future;


@Data
public class ProcessResult {
    
    private Future<Integer> result = null;

    private String transactionId;

}
