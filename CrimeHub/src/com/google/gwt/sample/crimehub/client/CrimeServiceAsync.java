package com.google.gwt.sample.crimehub.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CrimeServiceAsync {
	public void storeCrimes(AsyncCallback<Void> async);
	public void getCrimes(AsyncCallback<List<Crime>> async);
}
