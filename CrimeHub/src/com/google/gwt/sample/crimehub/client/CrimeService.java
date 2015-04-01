package com.google.gwt.sample.crimehub.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Crime")
public interface CrimeService extends RemoteService {
	public void storeCrimes();
	public List<Crime> getCrimes();
}
