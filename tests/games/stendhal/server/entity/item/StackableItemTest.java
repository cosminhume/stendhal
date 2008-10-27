package games.stendhal.server.entity.item;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.maps.MockStendlRPWorld;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.RPClass.ItemTestHelper;

public class StackableItemTest {

	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetQuantity() {
		StackableItem stack = new StackableItem("item", "clazz", "subclass", null);
		assertThat(stack.getQuantity(), is(1));
		
	}

	@Test
	public void testRemoveOne() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		stack.put("id", 0);
		assertThat(stack.getQuantity(), is(1));
		stack.removeOne();
		assertThat(stack.getQuantity(), is(0));
	}

	@Test
	public void testSetQuantity() {
		StackableItem stack = new StackableItem("item", "clazz", "subclass", null);
		assertThat(stack.getQuantity(), is(1));
		stack.setQuantity(0);
		assertThat(stack.getQuantity(), is(1));
		stack.setQuantity(-1);
		assertThat(stack.getQuantity(), is(1));
		stack.setQuantity(100);
		assertThat(stack.getQuantity(), is(100));
	}

	@Test
	public void testSub() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		stack.put("id", 0);
		assertThat(stack.getQuantity(), is(1));
		stack.sub(0);
		assertThat(stack.getQuantity(), is(1));
		
		
		stack.setQuantity(1);
		assertThat(stack.getQuantity(), is(1));
		stack.sub(1);
		assertThat(stack.getQuantity(), is(0));
		
		stack.setQuantity(100); 
		assertThat(stack.getQuantity(), is(100));
		stack.sub(1);
		assertThat(stack.getQuantity(), is(99));
		
	}
	
	@Test
	public void testSubNegativeNumber() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		stack.put("id", 0);
		assertThat(stack.getQuantity(), is(1));
		stack.sub(-1);
		assertThat("similar to splitOff()", stack.getQuantity(), is(1));
	}
	
	
	@Test
	public void testSubNegativeNumberSimilarToSplitOff() {
		final StackableItem subStack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		subStack.put("id", 0);
		assertThat(subStack.getQuantity(), is(1));
		final StackableItem splitStack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		splitStack.put("id", 0);
		assertThat(splitStack.getQuantity(), is(1));
		subStack.sub(-1);
		splitStack.splitOff(-1);
		assertThat(subStack.getQuantity(), is(splitStack.getQuantity()));
	}
	
	@Test
	public void testAddStackable() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		assertThat(stack.add(stack), is(1));
		assertThat(stack.getQuantity(), is(1));
		final StackableItem stackToAdd = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		assertThat(stack.add(stackToAdd), is(2));
		assertThat(stack.getQuantity(), is(2));
	}

	@Test
	public void testSplitOff() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		stack.put("id", 0);
		assertThat(stack.getQuantity(), is(1));
		stack.splitOff(0);
		assertThat(stack.getQuantity(), is(1));
		
		
		stack.setQuantity(1);
		assertThat(stack.getQuantity(), is(1));
		stack.splitOff(1);
		assertThat(stack.getQuantity(), is(0));
		
		stack.setQuantity(100); 
		assertThat(stack.getQuantity(), is(100));
		stack.splitOff(1);
		assertThat(stack.getQuantity(), is(99));
	}

	@Test
	public void testSplitOffNegativeNumber() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		stack.put("id", 0);
		assertThat(stack.getQuantity(), is(1));
		stack.splitOff(-1);
		assertThat("similar to sub()", stack.getQuantity(), is(1));
	}
	
	@Test
	public void testIsStackableMoney() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		final StackableItem stackOnTop = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		assertTrue(stack.isStackable(stackOnTop));
		assertTrue(stackOnTop.isStackable(stack));
		assertFalse(stack.isStackable(stack));
	}
	
	@Test
	public void testIsStackableBaloon() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("balloon");
		final StackableItem stackOnTop = (StackableItem) SingletonRepository.getEntityManager().getItem("balloon");
		assertTrue(stack.isStackable(stackOnTop));
		assertTrue(stackOnTop.isStackable(stack));
		assertFalse(stack.isStackable(stack));
	}
	
	@Test
	public void testIsStackableBaloonOnMoney() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("balloon");
		final StackableItem stackOnTop = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		assertFalse(stack.isStackable(stackOnTop));
		assertFalse(stackOnTop.isStackable(stack));
		assertFalse(stack.isStackable(stack));
	}
	
	@Test
	public void testIsStackableSummonScrolls() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("summon scroll");
		final StackableItem stackOnTop = (StackableItem) SingletonRepository.getEntityManager().getItem("summon scroll");
		assertTrue(stack.isStackable(stackOnTop));
		assertTrue(stackOnTop.isStackable(stack));
		assertFalse(stack.isStackable(stack));
	}
	
	@Test
	public void testIsStackableInvitationScrolls() {
		final StackableItem stack = (StackableItem) SingletonRepository.getEntityManager().getItem("invitation scroll");
		final StackableItem stackOnTop = (StackableItem) SingletonRepository.getEntityManager().getItem("invitation scroll");
		assertTrue(stack.isStackable(stackOnTop));
		assertTrue(stackOnTop.isStackable(stack));
		assertFalse(stack.isStackable(stack));
	}
	
	@Test
	public void testIsStackableDifferentScrolls() {
	    final List<StackableItem> stackables = new LinkedList<StackableItem>();
	    final StackableItem summonStack = (StackableItem) SingletonRepository.getEntityManager().getItem("summon scroll");
	    stackables.add(summonStack);
	    final StackableItem invitationStack = (StackableItem) SingletonRepository.getEntityManager().getItem("invitation scroll");
	    stackables.add(invitationStack);
	    final StackableItem nalworStack = (StackableItem) SingletonRepository.getEntityManager().getItem("nalwor city scroll");
	    stackables.add(nalworStack);
	    final StackableItem adosStack = (StackableItem) SingletonRepository.getEntityManager().getItem("ados city scroll");
	    stackables.add(adosStack);
	    final StackableItem fadoStack = (StackableItem) SingletonRepository.getEntityManager().getItem("fado city scroll");
	    stackables.add(fadoStack);
	    final StackableItem kirdnehStack = (StackableItem) SingletonRepository.getEntityManager().getItem("kirdneh city scroll");
	    stackables.add(kirdnehStack);
        final StackableItem kalavanStack = (StackableItem) SingletonRepository.getEntityManager().getItem("kalavan city scroll");
        stackables.add(kalavanStack);
        final StackableItem markedStack = (StackableItem) SingletonRepository.getEntityManager().getItem("marked scroll");
        stackables.add(markedStack);
        final StackableItem homeStack = (StackableItem) SingletonRepository.getEntityManager().getItem("home scroll");
        stackables.add(homeStack);
        final StackableItem emptyStack = (StackableItem) SingletonRepository.getEntityManager().getItem("empty scroll");
        stackables.add(emptyStack);
        final StackableItem rainbowStack = (StackableItem) SingletonRepository.getEntityManager().getItem("rainbow beans");
        stackables.add(rainbowStack);
        final StackableItem balloonStack = (StackableItem) SingletonRepository.getEntityManager().getItem("balloon");
        stackables.add(balloonStack);
        final StackableItem twilightStack = (StackableItem) SingletonRepository.getEntityManager().getItem("twilight moss");
        stackables.add(twilightStack);
        for (StackableItem stackable : stackables) {
            for (StackableItem onTop : stackables) {
            	String message = stackable.getName() + " and " + onTop.getName();
                assertFalse(message, stackable.isStackable(onTop));
            }
        }
	}

}
