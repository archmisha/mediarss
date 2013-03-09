package com.turn.ttorrent.tracker;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.WritableByteChannel;
import java.util.List;

/**
 * User: dikmanm
 * Date: 30/01/13 21:23
 */
public class HttpServletResponseWrapper implements Response {

	private HttpServletResponse response;

	public HttpServletResponseWrapper(HttpServletResponse response) {
		this.response = response;
	}

	@Override
	public void setContentLength(int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return response.getOutputStream();
	}

	@Override
	public OutputStream getOutputStream(int i) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public PrintStream getPrintStream() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public PrintStream getPrintStream(int i) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public WritableByteChannel getByteChannel() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public WritableByteChannel getByteChannel(int i) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean isCommitted() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void commit() throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void reset() throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void close() throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String> getNames() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void add(String s, String s2) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void add(String s, int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addDate(String s, long l) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void set(String s, String s2) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void set(String s, int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setDate(String s, long l) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void remove(String s) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean contains(String s) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getValue(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getInteger(String s) {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public long getDate(String s) {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String> getValues(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Cookie setCookie(Cookie cookie) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Cookie setCookie(String s, String s2) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Cookie getCookie(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<Cookie> getCookies() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ContentType getContentType() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getTransferEncoding() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getContentLength() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getCode() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setCode(int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getText() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setText(String s) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getMajor() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setMajor(int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getMinor() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setMinor(int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
