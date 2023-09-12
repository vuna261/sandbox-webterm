package com.cloudnut.webterm.thirdparty.Utils;

import com.cloudnut.webterm.thirdparty.connection.Connection;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

import static com.cloudnut.webterm.utils.Constants.LF;

@Slf4j
public class TimeMatchAndSubmit {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void matchPromptToSubmit(
            int seconds, InputStream in, String[] prompts, String data, Connection connection) throws IOException {
        log.info("Start prompt submit");
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                char lastChar = ' ';
                StringBuilder sb = new StringBuilder();
                boolean done = false;
                while (!done) {
                    int ch;
                    try {
                        ch = in.read();
                    } catch (IOException ex) {
                        return null;
                    }

                    if (ch == -1) {
                        return null;
                    }

                    sb.append((char) ch);
                    if (ch == lastChar) {
                        for (String pattern: prompts) {
                            if (sb.toString().toLowerCase().endsWith(pattern)) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
                return sb.toString();
            }
        });

        try {
            String result = future.get(seconds, TimeUnit.SECONDS);
            if (result == null) {
                throw new IOException("EOF or IOException Encountered");
            } else {
                connection.send(data + LF);
            }
        } catch (TimeoutException ex) {
            boolean cancelled = future.cancel(true);
            throw new IOException("Timeout");
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        } catch (ExecutionException ex) {
            throw new IOException(ex);
        }
    }
}
