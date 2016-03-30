package org.wanggz.nio.nioexam;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SearchRequest {

    private static final char BLANK = ' ';
    private static final String LINE_BREAK = "\r\n";
    private static final char COLON = ':';
    private static final String HOST_PARAM_NAME = "Host";

    private Map<String, String> headers = new HashMap<String, String>();
    private String host;
    private int port = 80;
    private String method = "GET";
    private String pathInfo;
    private String protocol = "HTTP/1.1";                   //
    private int maxRequestTime = ConnectionConfig.getInstance().getMaxRequestTime();
    private long lastAccess = -1L;

    public SearchRequest(String host, int port, String pathInfo) {
        this(host, port, pathInfo, false);
    }

    public SearchRequest(String host, int port, String pathInfo, boolean keepAlive) {
        this.host = host;
        if (port > 0) {
            this.port = port;
        }
        this.pathInfo = pathInfo;
        addHeader("User-Agent", "httpclient based nio");

        if (keepAlive) {
            addHeader("Connection", "Keep-Alive");// 启用Keep-Alive功能，
        } else {
            addHeader("Connection", "close");// 不启用Keep-Alive功能
        }
        Integer v = ConnectionConfig.getInstance().getMaxRequestTimeByHost().get(host + ":" + this.port);
        if (v != null && v > 0) {
            this.maxRequestTime = v;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getMaxRequestTime() {
        return maxRequestTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxRequestTime = maxTime;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public boolean isValid() {
        if (host == null || host.trim().equals("") || port <= 0 || pathInfo == null
                || pathInfo.trim().equals("")) {
            return false;
        }
        return true;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public byte[] toBytes() {
        StringBuilder builder = new StringBuilder(1024);
        // 请求行
        builder.append(this.method).append(BLANK);
        builder.append(this.pathInfo).append(BLANK);
        builder.append(this.protocol).append(LINE_BREAK);
        // 请求头参数列表
        Set<Map.Entry<String, String>> pairs = headers.entrySet();
        for (Map.Entry<String, String> entry : pairs) {
            builder.append(entry.getKey()).append(COLON).append(BLANK).append(entry.getValue())
                    .append(LINE_BREAK);
        }
        if (!headers.containsKey(HOST_PARAM_NAME)) {
            builder.append(HOST_PARAM_NAME).append(COLON).append(BLANK).append(this.host);
            if (this.port != 80) {
                builder.append(COLON).append(this.port);
            }
            builder.append(LINE_BREAK);
        }
        builder.append(LINE_BREAK);

        return builder.toString().getBytes();
    }

    public String toString() {
        return new String(toBytes());
    }

}
