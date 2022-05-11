package org.devgraft.sc.reactive;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class SshConnection implements ISshConnection {
    private static SshConnection instance;
    private final JSch jSch;
    private final Properties config;
    private final Map<String, Session> sessions;

    public static SshConnection getInstance() {
        if (instance == null) {
            SshConnection.instance = new SshConnection();
        }
        return SshConnection.instance;
    }

    public SshConnection() {
        this.jSch = new JSch();
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        this.sessions = new HashMap<>();
    }

    @Override
    public void connect(SshSessionProperty property) throws JSchException {
        Session session = jSch.getSession(property.getUserName(),
                property.getConnectionHost(),
                property.getConnectionPort());
        if (property.getPassword() != null)
            session.setPassword(property.getPassword());
        session.setConfig(config);
        session.setServerAliveCountMax(5);
        session.setServerAliveInterval(0);
        session.connect();
        session.setPortForwardingL(property.getLPort(), property.getHost(), property.getRPort());
        sessions.put(property.getSessionName(), session);
        log.info("session: [{}] connection Complete!", property.getSessionName());
    }

    @Override
    public void sendAlive(String sessionName) throws Exception {
        if (sessions.containsKey(sessionName)) {
            Session session = sessions.get(sessionName);
            session.sendKeepAliveMsg();
        }
    }

    @Override
    public boolean isConnect(String sessionName) {
        if (sessions.containsKey(sessionName)) {
            Session session = sessions.get(sessionName);
            return session.isConnected();
        }
        return false;
    }

    @Override
    public void close(String sessionName) {
        if (sessions.containsKey(sessionName)) {
            Session session = sessions.get(sessionName);
            session.disconnect();
            sessions.remove(sessionName);
        }
    }
}
