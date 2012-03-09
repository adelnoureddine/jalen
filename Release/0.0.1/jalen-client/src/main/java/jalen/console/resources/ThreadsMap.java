/*******************************************************************************
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 ******************************************************************************/
package jalen.console.resources;

import java.util.ArrayList;
import java.util.List;

public final class ThreadsMap {

	// Singleton instance
	private static volatile ThreadsMap instance = null;
	
	// List of threads
	private List<Threads> map;
	
	/**
	 * Constructor
	 */
	private ThreadsMap() {
		this.setMap(new ArrayList<Threads>());
	}
	
	/**
	 * 
	 * @return the instance of the singleton ThreadHistoryMap
	 */
	public final static ThreadsMap getInstance() {
		if (ThreadsMap.instance == null) {
			synchronized(ThreadsMap.class) {
				if (ThreadsMap.instance == null) {
					ThreadsMap.instance = new ThreadsMap();
				}
			}
		}
		return ThreadsMap.instance;
	}

	/**
	 * @return the map
	 */
	public List<Threads> getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(List<Threads> map) {
		this.map = map;
	}
	
	/**
	 * Add a thread to the map
	 * @param t the thread to add
	 */
	public void addToMap(Threads t) {
		this.map.add(t);
	}
	
	/**
	 * Checks if thread with id already have a thread map
	 * @param id the id to search for
	 * @return true if thread id is found, false if not
	 */
	public boolean containsThread(Long id) {
		for (Threads t : this.map) {
			if (t.getThreadId().equals(id))
				return true;
		}
		return false;
	}
	

	/**
	 * Get the thread data of the thread
	 * @param id the id of the thread
	 * @return the thread data of the thread, null if thread not found in map
	 */
	public Threads getThreads(Long id) {
		for (Threads t : this.map) {
			if (t.getThreadId().equals(id))
				return t;
		}
		return null;
	}
	
	public void addBeforeCpuTime(Long id, Long bcpu) {
		for (Threads t : this.map) {
			if (t.getThreadId().equals(id))
				t.setBeforeCpuTime(bcpu);
		}
	}
	
	public void addAfterCpuTime(Long id, Long acpu) {
		for (Threads t : this.map) {
			if (t.getThreadId().equals(id))
				t.setAfterCpuTime(acpu);
		}
	}

}
