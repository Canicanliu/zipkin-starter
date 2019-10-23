package com.mideadc.smart.life.zipkin.filter.servlet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class BufferedHttpRequestWrapper extends HttpServletRequestWrapper {
    private static final Logger log = LoggerFactory.getLogger(BufferedHttpRequestWrapper.class);
    private byte[] reqBody = null;

    public BufferedHttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.reqBody = IOUtils.toString(request.getInputStream(), "utf-8").getBytes();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ZeroServletInputStream(new ByteArrayInputStream(this.reqBody));

    }

    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getRequestBody() {
        String ex="";
        try {
            return new String(this.reqBody, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("获取参数异常",e);
            ex=e.getMessage();
        }
        return ex;
    }

    class ZeroServletInputStream extends ServletInputStream {
        private InputStream inputStream;

        public ZeroServletInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }

}
