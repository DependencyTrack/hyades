package org.acme.common;

public class ProxyInfo {
    private String host;
    private int port;
    private String domain;
    private String username;
    private String password;
    private String[] noProxy;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDomain() {
        return domain;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String[] getNoProxy() {
        return noProxy;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNoProxy(String[] noProxy) {
        this.noProxy = noProxy;
    }
}

