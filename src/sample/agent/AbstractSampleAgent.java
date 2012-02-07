package sample.agent;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.Constants;
import rescuecore2.log.Logger;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.standard.kernel.comms.StandardCommunicationModel;
import sample.search.SampleSearch;

/**
 * agentsの基底クラスです．
 * 
 * @param <E> 派生クラスのコントロールするStandardEntityを指定します．
 */
public abstract class AbstractSampleAgent<E extends StandardEntity> extends StandardAgent<E> {
	private static final int RANDOM_WALK_LENGTH = 50;

	private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
	private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();

	/**
	 * The search algorithm.
	 */
	protected SampleSearch search;

	/**
	 * Whether to use AKSpeak messages or not.
	 */
	protected boolean useSpeak;

	/**
	 * Cache of building IDs.
	 */
	protected List<EntityID> buildingIDs;

	/**
	 * Cache of road IDs.
	 */
	protected List<EntityID> roadIDs;

	/**
	 * Cache of refuge IDs.
	 */
	protected List<EntityID> refugeIDs;

	private Map<EntityID, Set<EntityID>> neighbours;

	/**
	 * Construct an AbstractSampleAgent.
	 */
	protected AbstractSampleAgent() {
	}
	
	/*
	 * TODO 独自のワールドモデルを作成する場合は
	 * StandardWorldModelを継承しこのメソッドでインスタンスを作成します．
	 */
//	@Override
//	public StandardWorldModel createWorldModel() {
//	}

	@Override
	protected void postConnect() {
		super.postConnect();
		buildingIDs = new ArrayList<EntityID>();
		roadIDs = new ArrayList<EntityID>();
		refugeIDs = new ArrayList<EntityID>();
		for (StandardEntity next : model) {
			if (next instanceof Building) {
				buildingIDs.add(next.getID());
			}
			if (next instanceof Road) {
				roadIDs.add(next.getID());
			}
			if (next instanceof Refuge) {
				refugeIDs.add(next.getID());
			}
		}
		search = new SampleSearch(model);
		neighbours = search.getGraph();
		useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
		Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
		Logger.debug(useSpeak ? "Using speak model" : "Using say model");
		
		System.out.println(toString() + " was connected. [id=" + getID() + "]");
	}

	/**
	 * Construct a random walk starting from this agent's current location to a
	 * random building.
	 * 
	 * @return A random walk.
	 */
	protected List<EntityID> randomWalk() {
		List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
		Set<EntityID> seen = new HashSet<EntityID>();
		EntityID current = ((Human) me()).getPosition();
		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
			result.add(current);
			seen.add(current);
			List<EntityID> possible = new ArrayList<EntityID>(neighbours.get(current));
			Collections.shuffle(possible, random);
			boolean found = false;
			for (EntityID next : possible) {
				if (seen.contains(next)) {
					continue;
				}
				current = next;
				found = true;
				break;
			}
			if (!found) {
				// We reached a dead-end.
				break;
			}
		}
		return result;
	}
}
