package com.google.gwt.sample.crimehub.client;


import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Parse")
public interface ParserService extends RemoteService {
	public List<Crime> getParsedCrimes();
}
