package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;
import games.stendhal.server.actions.move.MoveToAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.ados.magician_house.WizardNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MazeTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;
	private Maze maze;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		new WizardNPC().configureZone(zone, null);	

		maze = new Maze();
		maze.addToWorld();

		questSlot = maze.getSlotName();

		player = PlayerTestHelper.createPlayer("bob");
		PlayerTestHelper.registerPlayer(player, zone);
	}

	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Haizen");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Greetings! How may I help you?", getReply(npc));
		en.step(player, "task");
		assertEquals("I can send you to a #maze you need to find your way out. I keep the a list of the fast and frequent maze solvers in that blue book on the table.", getReply(npc));
		en.step(player, "maze");
		assertEquals("There will be a portal out in the opposite corner of the maze. I'll also add scrolls to the two other corners you can try to get if you are fast enough. Do you want to try?", getReply(npc));
		en.step(player, "no");
		assertEquals("OK. You look like you'd only get lost anyway.", getReply(npc));
		en.step(player, "task");
		assertEquals("I can send you to a #maze you need to find your way out. I keep the a list of the fast and frequent maze solvers in that blue book on the table.", getReply(npc));
		en.step(player, "maze");
		assertEquals("There will be a portal out in the opposite corner of the maze. I'll also add scrolls to the two other corners you can try to get if you are fast enough. Do you want to try?", getReply(npc));
		en.step(player, "yes");
		String questStarted = player.getQuest(questSlot);
		assertTrue(questStarted.startsWith("start;"));

		// tried to double-click
		new MoveToAction().onAction(player, null);
		assertEquals("Mouse movement is not possible here. Use your keyboard.", PlayerTestHelper.getPrivateReply(player));

		// didn't solve the maze
		en.step(player, "hi");
		assertEquals("Greetings! How may I help you?", getReply(npc));
		en.step(player, "task");
		assertEquals("I can send you to a #maze you need to find your way out. I keep the a list of the fast and frequent maze solvers in that blue book on the table.", getReply(npc));
		en.step(player, "maze");
		assertEquals("I can send you to the maze only once in a day. You can go there again in 24 hours.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Bye.", getReply(npc));

		// jump back to the quest start state
		player.setQuest(questSlot, questStarted);
		// solve the maze
		Portal portal = maze.getPortal();
		player.setPosition(portal.getX(), portal.getY());
		portal.onUsed(player);
		assertTrue(PlayerTestHelper.getPrivateReply(player).matches("You used 0 seconds to solve the maze. That was worth [0-9]+ points."));
		assertEquals("done", player.getQuest(questSlot, 0));
	}
}
