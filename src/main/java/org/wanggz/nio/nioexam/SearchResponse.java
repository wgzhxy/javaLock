package org.wanggz.nio.nioexam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class SearchResponse {

    // US-ASCII CR,carriage return
    public static final int CR = 13;
    // US-ASCII LF,linefeed
    public static final int LF = 10;
    // 正常http响应状态码
    public static final int SC_OK = 200;
    protected static final Log logger = LogFactory.getLog(SearchResponse.class);
    // 响应头
    private Map<String, String> headers = new HashMap<String, String>();
    // 状态头
    private int status;
    // 响应头是否已经结束
    private boolean isHeaderEnd;
    // 响应是否已经结束
    private boolean completed;
    // 用于解析http头是否结束的flag
    private int headerEndFlag;
    // http响应体
    private byte[] bodyBytes;
    // Transfer-Encoding是否为chunked
    private boolean chunked;
    // body的长度
    private int contentLength;
    // 对于当前chunk还有多少可写的字节
    private int remainWrite;
    // 用于记录chunk头的信息
    private StringBuilder chunkHeader = new StringBuilder(32);
    // 是否这个http持久连接最后一个响应
    private boolean lastResponse;
    // 是否支持http1.1
    private boolean http11;
    // 请求开始时间,单位毫秒
    private long startTime;
    // 请求响应花费时间,单位毫秒
    private int costTime;
    private FastByteArrayOutputStream headerStream = new FastByteArrayOutputStream(512);
    private FastByteArrayOutputStream bodyStream = new FastByteArrayOutputStream(16348);

    public SearchResponse() {
        this.startTime = System.currentTimeMillis();
    }

    private static byte[] unZip(byte[] bodyBytes) {
        GZIPInputStream gzipStream = null;
        try {
            gzipStream = new GZIPInputStream(new ByteArrayInputStream(bodyBytes));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = 0;
            byte[] buf = new byte[1024];
            while ((n = gzipStream.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            byte[] unCompressData = out.toByteArray();
            if (logger.isInfoEnabled()) {
                logger.info("compress Data length:" + bodyBytes.length + ",uncompress Data length:"
                        + unCompressData.length);

            }
            return unCompressData;
        } catch (IOException e) {
            logger.error("up compress data error:" + e.getMessage());
            return new byte[0];
        } finally {
            if (gzipStream != null) {
                try {
                    gzipStream.close();
                } catch (IOException e) {
                }
            }
        }

    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }

    public byte[] getBodyStream() {
        if (this.bodyBytes != null && this.completed) {
            return this.bodyBytes;
        }
        if (this.completed) {
            String contentEncoding = headers.get("Content-Encoding");
            if ("gzip".equalsIgnoreCase(contentEncoding)) {// 解压
                this.bodyBytes = unZip(bodyStream.toByteArray());
            } else {
                this.bodyBytes = bodyStream.toByteArray();
            }
            bodyStream = null;
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("the body isn't completed!");
            }
            this.bodyBytes = new byte[0];
        }
        return this.bodyBytes;
    }

    public char[] getBody() {
        String contentType = headers.get("Content-Type");
        if (contentType != null) {// 采用HTTP响应指定的charset
            if (contentType.indexOf("=") > 0) {
                String charset = contentType.substring(contentType.indexOf("=") + 1);
                return getBody(charset);
            }
        }
        return getBody(null);
    }

    public char[] getBody(String encoding) {
        byte[] bb = getBodyStream();
        if (bb == null || bb.length == 0) {
            //返回null是为了在外面判断时只判断null而不用判断其它情况。
            return null;
        }
//    	if(bb.length < 12){ //无论是V2还是V4，长度小于12肯定是错误的
//    		return null;
//    	}
//    	String strInt = new String(bb,0,12).trim();
//    	int x = -1;
//    	try{
//    		x = Integer.parseInt(strInt);
//    	}catch(Exception e){}
//    	if(x != -1){//说明前12个字节是一个整数，V2.0版
//    		if(x != bb.length - 12){
//    			logger.error("incomplete body data.");
//    			return null; //数据格式错误
//    		}
//    	}


        Charset charset = null;
        if (encoding == null) {
            charset = Charset.defaultCharset();
        } else {
            charset = Charset.forName(encoding);
        }
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bb));
        char[] result = new char[charBuffer.limit()];

        charBuffer.get(result);

        return result;

    }

    /**
     * @param array
     * @return http body是否已经结束
     * @throws IOException
     */
    public boolean appendBytes(byte[] array) throws IOException {

        if (array == null || array.length == 0) {
            return this.completed;
        }

        if (!isHeaderEnd) {
            for (int i = 0; i < array.length; i++) {
                headerStream.write(array[i]);
                if (array[i] == CR && (headerEndFlag == 0 || headerEndFlag == 2)) {
                    headerEndFlag = headerEndFlag + 1;
                    continue;
                }
                if (array[i] == LF && (headerEndFlag == 1 || headerEndFlag == 3)) {
                    headerEndFlag = headerEndFlag + 1;
                    if (headerEndFlag == 4) {// 已经找到header的结束点

                        decodeHeader(headerStream.toByteArray());

                        if ("close".equalsIgnoreCase(headers.get("Connection"))) {
                            this.lastResponse = true;
                        }
                        if ("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
                            this.chunked = true;
                        }
                        if (headers.get("Content-Length") != null) {
                            this.contentLength = Integer.valueOf(headers.get("Content-Length"));
                        }

                        if (!this.lastResponse && !this.chunked && headers.get("Content-Length") == null) {
                            throw new RuntimeException("can't know the response's length!");
                        }

                        // 把剩余数据写到bodystream
                        appendBody(array, i + 1);
                        headerStream = null;
                        break;
                    }
                    continue;
                }
                headerEndFlag = 0;
            }
        } else {
            appendBody(array, 0);
        }

        return this.completed;

    }

    private void appendBody(byte b[], int off) {
        if (this.isHeaderEnd && b != null && b.length > 0) {
            if (this.chunked) {// 使用了块传输
                if (remainWrite <= 0) {// 如果还不清楚下一个块的大小
                    findNextChunkSize(b, off);
                } else {
                    writeToBody(b, off);
                }
            } else {
                bodyStream.write(b, off, b.length - off);
                if (bodyStream.size() == contentLength) {

                    setCompleted(true);
                }
            }
        }
    }

    private void findNextChunkSize(byte[] b, int off) {
        if (this.remainWrite > 0) {
            return;
        }
        for (int i = off; i < b.length; i++) {

            chunkHeader.append((char) b[i]);
            if (chunkHeader.length() > 2 && chunkHeader.charAt(chunkHeader.length() - 2) == '\r'
                    && chunkHeader.charAt(chunkHeader.length() - 1) == '\n') {
                int chunkSize;
                if (chunkHeader.indexOf(";") != -1) {
                    // 这个chunk有chunk-extension，但不需要处理
                    chunkSize = Integer.valueOf(chunkHeader.substring(0, chunkHeader.indexOf(";")).trim(), 16);
                } else {
                    chunkSize = Integer.valueOf(chunkHeader.toString().trim(), 16);
                }
                if (chunkSize == 0) {
                    // 整个body已经结束
                    setCompleted(true);
                    break;
                }
                this.chunkHeader.delete(0, chunkHeader.length());
                this.remainWrite = chunkSize;

                writeToBody(b, i + 1);
                break;
            }
        }

    }

    private void writeToBody(byte b[], int off) {

        if (this.remainWrite > 0 && !this.completed) {
            int remain = (b.length - off);
            if (remain < 0) {
                return;
            }
            if (remain < this.remainWrite) {

                bodyStream.write(b, off, remain);
                // 重新计算剩余可写的数据
                this.remainWrite = (this.remainWrite - remain);
            } else {
                bodyStream.write(b, off, this.remainWrite);
                int writedLength = this.remainWrite;
                this.remainWrite = 0;
                findNextChunkSize(b, off + writedLength);

            }
        }

    }

    private void decodeHeader(byte[] headerBytes) {

        String statusAndHeaders = new String(headerBytes, 0, headerBytes.length - 4).trim();
        String lines[] = statusAndHeaders.split("\r\n");

        if (lines == null || lines.length == 0) {
            throw new RuntimeException("illegal http response:\r\n" + new String(headerBytes));
        }

        this.isHeaderEnd = true;
        String[] statusLine = lines[0].split(" ");
        if (logger.isDebugEnabled()) {
            logger.debug("http response status line:" + lines[0]);
        }
        if (statusLine[0].startsWith("HTTP/")) {
            if ("HTTP/1.1".equalsIgnoreCase(statusLine[0])) {
                http11 = true;
            }
        } else {
            throw new RuntimeException(" error http response status line:" + lines[0]);
        }

        this.status = Integer.parseInt(statusLine[1]);

        if (lines.length > 1) {
            for (int i = 1; i < lines.length; i++) {
                String header[] = lines[i].split(": ");
                this.headers.put(header[0], header[1]);
            }
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.costTime = (int) (System.currentTimeMillis() - startTime);
        this.completed = completed;
    }

    protected boolean isHeaderEnd() {
        return isHeaderEnd;
    }

    protected void setHeaderEnd(boolean isHeaderEnd) {
        this.isHeaderEnd = isHeaderEnd;
    }

    public boolean isLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(boolean lastResponse) {
        this.lastResponse = lastResponse;
    }

    public boolean isHttp11() {
        return http11;
    }

    public boolean isForceClose() {
        return !this.isCompleted() || this.isLastResponse() || !this.isHttp11() || this.headers.isEmpty();
    }

    public int getCostTime() {
        return costTime;
    }

    public String toString() {
        return "status:" + status + ",headers:" + headers + ",body.size():" + bodyStream.size() + ",costTime:"
                + costTime;
    }
}
