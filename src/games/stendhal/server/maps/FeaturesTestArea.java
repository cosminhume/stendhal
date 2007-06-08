package games.stendhal.server.maps;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.AttackableCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.spawner.CreatureRespawnPoint;
import games.stendhal.server.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.rule.defaultruleset.DefaultItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeaturesTestArea implements ZoneConfigurator {

	private DefaultEntityManager manager;

	static class QuestRat extends Creature {

		public QuestRat(Creature copy) {
			super(copy);
		}

		@Override
		public void onDead(Entity killer) {
			if (killer instanceof RPEntity) {
				RPEntity killerRPEntity = (RPEntity) killer;
				if (!killerRPEntity.isEquipped("key_golden")) {
					Item item = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem("key_golden");
					killerRPEntity.equip(item, true);
				}
			}
		}

		@Override
		public void update() {
			noises = new LinkedList<String>(noises);
			noises.add("Thou shall not obtain the key!");
		}
	}

	public FeaturesTestArea() {
		manager = (DefaultEntityManager) StendhalRPWorld.get().getRuleManager().getEntityManager();
	}

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		createDoorAndKey(zone, attributes);
		attackableAnimal(zone, attributes);
	}

	private void createDoorAndKey(StendhalRPZone zone, Map<String, String> attributes) {
		List<String> slots = new LinkedList<String>();
		slots.add("bag");

		DefaultItem item = new DefaultItem("key", "gold", "key_golden", -1);
		item.setImplementation(Item.class);
		item.setWeight(1);
		item.setEquipableSlots(slots);
		manager.addItem(item);

		item = new DefaultItem("key", "golden", "key_silver", -1);
		item.setImplementation(Item.class);
		item.setWeight(1);
		item.setEquipableSlots(slots);
		manager.addItem(item);

		Creature creature = new QuestRat(manager.getCreature("rat"));
		CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 40, 5, creature, 1);
		zone.add(point);
	}

	private void attackableAnimal(StendhalRPZone zone, Map<String, String> attributes) {
		Creature creature = new AttackableCreature(manager.getCreature("orc"));
		CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 4, 56, creature, 1);
		point.setRespawnTime(60 * 60 * 3);
		zone.add(point);

		creature = manager.getCreature("deer");
		point = new CreatureRespawnPoint(zone, 14, 56, creature, 1);
		point.setRespawnTime(60 * 60 * 3);
		zone.add(point);
	}
}
