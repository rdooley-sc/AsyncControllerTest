package com.red.sandbox.async;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Relatively simple controller for launching Processes (threads)
 * Spawns a resultProcessor (clean-up) thread that checks the thread pool for completion
 * and removes them from the result queue (<Future> objects)
 * @author rdooley
 *
 */


@Slf4j
public final class ProcessService {

    public static int PROCESS_LAUNCH_SUCCESS = 200;
	public static int PROCESS_LAUNCH_FAIL = -999;

	private static long RESULT_PROCESSOR_POLL_TIMEOUT = 1000L;
	private static long RESULT_PROCESSOR_SLEEP_TIME = 5000L;
	
    private static Thread resultProcessor;
    
    private static List<ProcessResult> resultQueue;
    
    private static ThreadPoolExecutor processThreadPool;

    private static CompletionService<Integer> executorService;


	private static int maxThreads = 10;
	
	static {
        processThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        processThreadPool.setKeepAliveTime(1000, TimeUnit.MILLISECONDS);

        executorService = new ExecutorCompletionService<>(processThreadPool);

        resultQueue = new ArrayList<>(maxThreads);
	}
	
	
	public static void init() {
        log.debug("ProcessService: Init....");
        resultProcessor = createResultProcessingThread();
        resultProcessor.start();
	}

    public static synchronized int getAvailableThreads() {
        return (processThreadPool.getMaximumPoolSize() - resultQueue.size());
    }

    public static int submitProcess(CallableProcess process ) {
        log.info("submitProcess: ***** Submitting Process....");

        if (getAvailableThreads() <= 0) {
            return PROCESS_LAUNCH_FAIL;   // failed - busy
        }

        String transactionId = process.getTransactionId();

        Future<Integer> f = processThreadPool.submit(process);

        if (f == null) {
            log.error("submitProcess: ***** Error: null Future from submit call()");
            return PROCESS_LAUNCH_FAIL;
        }

        addFutureResult(f,transactionId);
        return PROCESS_LAUNCH_SUCCESS;
    }


    private static synchronized void addFutureResult(Future<Integer> f, String transactionId) {
        ProcessResult result = new ProcessResult();
        result.setResult(f);
        result.setTransactionId(transactionId);
        resultQueue.add(result);
    }


    public static Thread createResultProcessingThread() {
        Thread resultProcessor = new Thread() {
            private boolean shutdown=false;

            @Override
            public synchronized void start() {
                super.start();
                log.debug("resultProcessor: Result processing thread started.");
            }
            @Override
            public void run() {
                while (!shutdown) {
                    processResults();
                    try {
                        Thread.sleep(RESULT_PROCESSOR_SLEEP_TIME);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }

            @Override
            public void interrupt() {
                super.interrupt();
                shutdown=true;
                log.debug("resultProcessor: Interrupt processed ****");
            }

        };
        return resultProcessor;
    }


    public void shutdown() {
        //processThreadPool.shutdownNow();
        resultProcessor.interrupt();
    }


    public static synchronized void processResults() {
        //log.debug("processResults: ***** checking for results....");

        if (resultQueue==null || resultQueue.size() == 0) {
            log.debug("processResults: ***** NO running jobs.");
            return;
        }


        for (ListIterator<ProcessResult> iter = resultQueue.listIterator(); iter.hasNext();) {
            ProcessResult cr = iter.next();

            String transactionId = cr.getTransactionId();
            Future<Integer> f = cr.getResult();

            Integer result;
            try {
                result = f.get(RESULT_PROCESSOR_POLL_TIMEOUT,TimeUnit.MILLISECONDS);
                log.debug("processResults: ***** Completed Process ID: " + transactionId + " result: " + result);
            } 
            catch (TimeoutException te) {
                log.debug("processResults: ***** Process ID: " + transactionId + " still running");
                continue;
            }
            catch (InterruptedException e) { 
                log.error("processResults: ***** InterruptedException: Process ID: " + transactionId + " was Interrupted");
                log.error("processResults: ***** Execption caught: ", e);
            }
            catch (ExecutionException e) {
                log.error("processResults: ***** ExecutionException: Process ID: " + transactionId + " was aborted due to Exception!");
                log.error("processResults: ***** Execption caught: ", e);
            }

            // The task either completed or threw an Exception
            // in either case, remove it from queue
            iter.remove();
        }
        
    }


     public static synchronized List<String> getRunningIds() {
        int count = processThreadPool.getMaximumPoolSize() - resultQueue.size();
        List<String> ids = null;
        return ids;
    }





}
