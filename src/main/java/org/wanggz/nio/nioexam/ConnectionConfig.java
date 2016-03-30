package org.wanggz.nio.nioexam;

import java.util.HashMap;
import java.util.Map;

public class ConnectionConfig {

    // selector的最大可用数
    private static int maxSelectors = 100;
    // 取得可用的selector之前的超时时间,小于0就一直阻塞到有可用的资源,单位为毫秒
    private static int waitSelectorFromPoolTime = 5000;

    //这个时间表示一次请求中所有连接处理的最大时间，即多个连接循环处理的部时间，原来使用selectTimeout
    //时间，现在独立出来
    private static int allProcessMaxTime = 10000;


    // 每个http响应超时时间,单位为毫秒,应用在Selector.select(selectTimeout)。
    private static int selectTimeout = 200;

    // 单个host的最大SocketChannel连接数
    private static int hostMaxSockets = 100;

    // 从连接池中取得socket连接之前超时时间,单位为毫秒	
    private static int waitSocketFromPoolTime = 3000;

    // 创建socket连接的超时时间，单位为毫秒
    private static int socketTimeout = 5000;

    // so_timeout,读超时即一个read的call等待的最大时间
    private static int soTimeout = 1000;

    //每个请求默认最大处理时间，从发送到得到返回的结果 
    private static int maxRequestTime = 10000;

    private static ConnectionConfig CONFIG = new ConnectionConfig();
    private static Map<String, Integer> connParamesProperties = new HashMap<String, Integer>();
    private static Map<String, Integer> socketTimeoutByHost = new HashMap<String, Integer>();
    private static Map<String, Integer> waitSocketFromPoolTimeByHost = new HashMap<String, Integer>();
    private static Map<String, Integer> maxRequestTimeByHost = new HashMap<String, Integer>();

    static {
        // test......
        // hostConnectionTimeout.put("172.22.44.201:2088", 100);

    }

    private ConnectionConfig() {
    }

    public static ConnectionConfig getInstance() {
        return CONFIG;
    }

    public Map<String, Integer> getConfig() {
        return connParamesProperties;
    }

    /**
     * 存储每个IP＋PORT对应的超时时间
     *
     * @return
     */
    public Map<String, Integer> getSocketTimeoutByHost() {
        return socketTimeoutByHost;
    }

    public Map<String, Integer> getWaitSocketFromPoolTimeByHost() {
        return waitSocketFromPoolTimeByHost;
    }

    public Map<String, Integer> getMaxRequestTimeByHost() {
        return maxRequestTimeByHost;
    }

    public int getMaxSelectors() {
        Integer v = connParamesProperties.get("maxSelectors");
        if (v == null) {
            v = maxSelectors;
        }
        return v;
    }

    public int getWaitSelectorFromPoolTime() {
        Integer v = connParamesProperties.get("waitSelectorFromPoolTime");
        if (v == null) {
            v = waitSelectorFromPoolTime;
        }
        return v;
    }

    public int getSelectTimeout() {
        Integer v = connParamesProperties.get("selectTimeout");
        if (v == null) {
            v = selectTimeout;
        }
        return v;
    }

    public int getHostMaxSockets() {
        Integer v = connParamesProperties.get("hostMaxSockets");
        if (v == null) {
            v = hostMaxSockets;
        }
        return v;
    }

    public int getWaitSocketFromPoolTime() {
        Integer v = connParamesProperties.get("waitSocketFromPoolTime");
        if (v == null) {
            v = waitSocketFromPoolTime;
        }
        return v;
    }

    public int getSocketTimeout() {
        Integer v = connParamesProperties.get("socketTimeout");
        if (v == null) {
            v = socketTimeout;
        }
        return v;
    }

    public int getSoTimeout() {
        Integer v = connParamesProperties.get("soTimeout");
        if (v == null) {
            v = soTimeout;
        }
        return v;
    }

    public int getMaxRequestTime() {
        Integer v = connParamesProperties.get("maxRequestTime");
        if (v == null) {
            v = maxRequestTime;
        }
        return v;
    }


    public int getAllProcessMaxTime() {
        Integer v = connParamesProperties.get("allProcessMaxTime");
        if (v == null) {
            v = allProcessMaxTime;
        }
        return v;
    }
}

