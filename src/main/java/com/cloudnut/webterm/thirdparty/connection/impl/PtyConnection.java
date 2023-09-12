package com.cloudnut.webterm.thirdparty.connection.impl;

import com.cloudnut.webterm.thirdparty.connection.Connection;
import com.cloudnut.webterm.thirdparty.pojo.TerminalSessionInfo;
import com.jcraft.jsch.JSchException;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.cloudnut.webterm.thirdparty.Utils.TimeMatchAndSubmit.matchPromptToSubmit;
import static com.cloudnut.webterm.utils.Constants.*;

@Slf4j
public class PtyConnection extends Connection {
    private PtyProcess ptyProcess;

    private static boolean telnetOk = false;
    private static boolean sshOk = false;
    private static boolean c3270Ok = false;

    public PtyConnection(WebSocketSession webSocketSession, TerminalSessionInfo terminalSessionInfo) {
        super(webSocketSession, terminalSessionInfo);
    }

    /**
     * start connection on separate thread
     *
     * @throws JSchException
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void connect() throws JSchException, IOException, InterruptedException {
        if (!SystemUtils.IS_OS_WINDOWS) {
            if (!sshOk) {
                Path path = Paths.get(SSH_CMD);
                if (Files.notExists(path)) {
                    throw new IOException("ConfigError: SSH/pty NOT SET UP");
                } else {
                    sshOk = true;
                }
            }
        } else {
            throw new IOException("OS NOT SUPPORT");
        }

        String[] cmd = {SSH_CMD, "-q",
                "-oUserKnownHostsFile=/dev/null",
                "-oStrictHostKeyChecking=no",
                "-oKexAlgorithms=+diffie-hellman-group1-sha1",
                "-oHostKeyAlgorithms=+ssh-rsa,ssh-dss",
                "-p", terminalSessionInfo.getPort().toString(),
                "-l", terminalSessionInfo.getUsername(),
                terminalSessionInfo.getHost()};

        // check
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm");

        try {
            ptyProcess = new PtyProcessBuilder(cmd).setEnvironment(env)
                    .setInitialColumns(COL_SIZE)
                    .setInitialRows(ROW_SIZE).start();
        } catch (IOException ex) {
            log.error("PTY START Error: {}", ex.getMessage());
            throw new IOException(ex.getCause());
        }

        InputStream inputStream  = ptyProcess.getInputStream();
        if (StringUtils.isNotEmpty(terminalSessionInfo.getPassword())) {
            matchPromptToSubmit(6, inputStream, PASSWORD_PROMPTS, terminalSessionInfo.getPassword(), this);
        }
        blockingRead(inputStream);
    }

    /**
     * send data to downstream
     *
     * @param data
     * @throws IOException
     */
    @Override
    public void send(String data) throws IOException {
        if (isAlive()) {
            OutputStream os = ptyProcess.getOutputStream();
            os.write(data.getBytes());
            os.flush();

            terminalSessionInfo.setTrafficTimeNow();
        } else {
            throw new IOException("no valid backend connection");
        }
    }

    /**
     * check
     *
     * @return
     */
    @Override
    public boolean isAlive() {
        return ptyProcess != null && ptyProcess.isAlive();
    }

    /**
     * close connection
     */
    @Override
    public void close() {
        if (ptyProcess != null && ptyProcess.isAlive()) {
            try {
                if (ptyProcess.getInputStream() != null) {
                    ptyProcess.getInputStream().close();
                }

                if (ptyProcess.getOutputStream() != null) {
                    ptyProcess.getOutputStream().close();
                }

                ptyProcess.destroyForcibly();
                ptyProcess.waitFor();
            } catch (Exception ex) {
                log.error("PTY close exception: {}", ex.getMessage());
            }
        }
    }

    /**
     * screen resize
     */
    @Override
    public void resize() {
        WinSize ws = new WinSize(COL_SIZE, ROW_SIZE);
        ptyProcess.setWinSize(ws);
    }
}
