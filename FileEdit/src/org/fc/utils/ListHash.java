package org.fc.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Hash di liste
 * 
 * @author Fabrizio Calabretta
 */
public class ListHash {
	HashMap hash = new HashMap();

	public ListHash() {
	}

	public void add(Object k, Object v) {
		LinkedList l = (LinkedList) hash.get(k);
		if (l == null) {
			l = new LinkedList();
			hash.put(k, l);
		}
		l.add(v);
	}

	public LinkedList getList(Object k) {
		return ((LinkedList) hash.get(k));
	}

	public Set getKeys() {
		return hash.keySet();
	}

	public boolean containsKey(Object k) {
		return hash.containsKey(k);
	}
}
