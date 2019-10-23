package com.mideadc.smart.life.zipkin.filter.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 20:42 2019-10-9
 */
public class ByteArrayResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream buffer = null;
    private ServletOutputStream out = null;
    private PrintWriter writer = null;

    public ByteArrayResponseWrapper(HttpServletResponse response) throws IOException{
        super(response);
        buffer = new ByteArrayOutputStream();
        out = new WapperedOutputStream(buffer);
        writer = new PrintWriter(new OutputStreamWriter(buffer, "UTF-8"));
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (out != null) {
            out.flush();
        }
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public void reset() {
        buffer.reset();
    }

    public String getResponseData(String charset) throws IOException {
        flushBuffer();
        byte[] bytes = buffer.toByteArray();
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    class WapperedOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream bos = null;
        public WapperedOutputStream(ByteArrayOutputStream stream) throws IOException {
            bos = stream;
        }
        @Override
        public void write(int b) throws IOException {
            bos.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

    }
}
