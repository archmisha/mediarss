package com.turn.ttorrent.tracker;

import org.simpleframework.http.*;
import org.simpleframework.http.parse.AddressParser;
import org.simpleframework.http.parse.PathParser;
import org.simpleframework.http.session.Session;
import org.simpleframework.util.lease.LeaseException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 30/01/13 21:17
 */
public class HttpServletRequestWrapper implements Request {

	private HttpServletRequest request;

	public HttpServletRequestWrapper(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public boolean isKeepAlive() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Form getForm() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getParameter(String s) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Part getPart(String s) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Map getAttributes() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Object getAttribute(Object o) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public InetSocketAddress getClientAddress() {
		return new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
	}

	@Override
	public String getContent() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ReadableByteChannel getByteChannel() throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Session getSession() throws LeaseException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Session getSession(boolean b) throws LeaseException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String> getNames() {
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
	public Cookie getCookie(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<Cookie> getCookies() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getValue(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String> getValues(String s) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<Locale> getLocales() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean contains(String s) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ContentType getContentType() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getContentLength() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getMethod() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getTarget() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Address getAddress() {
		return new AddressParser(request.getRequestURL().toString() + "?" + request.getQueryString());
	}

	@Override
	public Path getPath() {
		return new PathParser(request.getServletPath());
	}

	@Override
	public Query getQuery() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getMajor() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getMinor() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
