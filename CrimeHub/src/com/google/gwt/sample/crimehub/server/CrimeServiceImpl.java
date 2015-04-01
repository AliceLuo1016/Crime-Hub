package com.google.gwt.sample.crimehub.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import com.google.gwt.sample.crimehub.client.Crime;
import com.google.gwt.sample.crimehub.client.CrimeService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CrimeServiceImpl extends RemoteServiceServlet implements CrimeService {

	private static final PersistenceManagerFactory PMF = 
			JDOHelper.getPersistenceManagerFactory("transactions-optional");
	private boolean alreadyStored;
	private ParserServiceImpl psi = new ParserServiceImpl();

	public CrimeServiceImpl() {

	}

	public void storeCrimes() {
		List<Crime> crimes = psi.getParsedCrimes();
		storeCrimesInDatastore(crimes);
	}

	private void storeCrimesInDatastore(List<Crime> crimes) {
		PersistenceManager pm = getPersistenceManager();
		if (alreadyStored) {
			System.out.println("Already stored to datastore.");
			return;
		}
		else {
			final long startTime = System.currentTimeMillis();
			pm.makePersistentAll(crimes);
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime) );
			pm.close();
			alreadyStored = true;
			System.out.println("Successfully stored in datastore.");
		}
	}

	public List<Crime> getCrimes() {
		PersistenceManager pm = getPersistenceManager();
		List<Crime> instance = new ArrayList<Crime>();

		javax.jdo.Query q = pm.newQuery(Crime.class);
		q.getFetchPlan().setFetchSize(900);
		List<Crime> queryCrimes = (List<Crime>) q.execute();
		int i = 0;

		pm.close();
		if (!queryCrimes.isEmpty()) {
			for (Crime crime : queryCrimes) {
				instance.add(crime);
				i++;
			}
			System.out.println("Query for all data successful.");
		}
		else {
			System.out.println("Query for all data unsuccessful.");
		}
		
		System.out.println("added " + i + " crimes");
		return instance;

	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}
