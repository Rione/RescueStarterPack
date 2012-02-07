package sample.util;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;

/**
 * EntityIDでソートするためのコンパレータ
 * @author com
 *
 */
public class IdSorter implements Comparator<StandardEntity> {

	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		return (a.getID().getValue() - b.getID().getValue());
	}
}
