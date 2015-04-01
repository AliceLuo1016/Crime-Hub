package com.google.gwt.sample.crimehub.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ParserServiceAsync {
	public void getParsedCrimes(AsyncCallback<List<Crime>> async);
}
