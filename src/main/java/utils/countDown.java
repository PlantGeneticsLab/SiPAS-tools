package utils;

import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class countDown {
    public void whenParallelProcessing_thenMainThreadWillBlockUntilCompletion()
            throws InterruptedException {

        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(5);
//        List<Thread> workers = Stream
//                .generate(() -> new Thread(new Worker(outputScraper, countDownLatch)))
//                .limit(5)
//                .collect(toList());
//
//        workers.forEach(Thread::start);
        countDownLatch.await();
        outputScraper.add("Latch released");

    }

}
