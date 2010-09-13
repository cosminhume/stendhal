package games.stendhal.server.core.events.achievements;

import games.stendhal.common.Grammar;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.db.AchievementDAO;
import games.stendhal.server.core.engine.dbcommand.WriteReachedAchievementCommand;
import games.stendhal.server.entity.player.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.DAORegister;

import org.apache.log4j.Logger;
/**
 * Checks for reached achievements and marks them as reached for a player if he has fullfilled them
 *  
 * @author madmetzger
 */
public class AchievementNotifier {
	
	private static final Logger logger = Logger.getLogger(AchievementNotifier.class);
	
	private static AchievementNotifier instance;
	
	private Map<Category, List<Achievement>> achievements;
	
	private Map<String, Integer> identifiersToIds;
	
	private AchievementNotifier() {
		achievements = new HashMap<Category, List<Achievement>>();
		identifiersToIds = new HashMap<String, Integer>();
	}
	
	/**
	 * singleton accessor method
	 * 
	 * @return the AchievementNotifier
	 */
	public static AchievementNotifier get() {
		if(instance == null) {
			instance = new AchievementNotifier();
		}
		return instance;
	}
	
	/**
	 * initializes the achievements that are available and registers the login listener
	 * new added achievements are added to the achievements table
	 */
	public void initialize() {
		//read all configured achievements and put them into the categorized map
		Map<String, Achievement> allAchievements = createAchievements();
		for(Achievement a : allAchievements.values()) {
			if(!achievements.containsKey(a.getCategory())) {
				achievements.put(a.getCategory(), new LinkedList<Achievement>());
			}
			achievements.get(a.getCategory()).add(a);
		}
		//collect all identifiers from database
		Map<String, Integer> allIdentifiersInDatabase = collectAllIdentifiersFromDatabase();
		//update stored data with configured achievements
		identifiersToIds.putAll(allIdentifiersInDatabase);
		for(String identifier : allIdentifiersInDatabase.keySet()) {
			Achievement achievement = allAchievements.get(identifier);
			try {
				// this happens if an achievement is not configured anymore but already in the database
				// in that case we should keep it as players could have reached it
				// useful to stop checking for a certain achievement but keep results
				if(achievement != null) {
					DAORegister.get().get(AchievementDAO.class).updateAchievement(allIdentifiersInDatabase.get(identifier), achievement);
				} 
			} catch (SQLException e) {
				logger.error("Error while updating exisiting achievement "+achievement.getTitle(), e);
			}
		}
		// remove already stored achievements before saving them
		for(String identifier : allIdentifiersInDatabase.keySet()) {
			allAchievements.remove(identifier);
		}
		//save new achievements and add their identifier and id to the identifierToId map
		for (Achievement a : allAchievements.values()) {
			Integer id;
			try {
				id = DAORegister.get().get(AchievementDAO.class).saveAchievement(a);
				identifiersToIds.put(a.getIdentifier(), id);
			} catch (SQLException e) {
				logger.error("Error while saving new achievement "+a.getTitle(), e);
			}
		}
	}

	/**
	 * collects all identifiers from the database
	 * 
	 * @return a set of all identifier strings
	 */
	private Map<String, Integer> collectAllIdentifiersFromDatabase() {
		Map<String, Integer> mapFromDB = new HashMap<String, Integer>();
		try {
			mapFromDB = DAORegister.get().get(AchievementDAO.class).loadIdentifierIdPairs();
		} catch (SQLException e) {
			logger.error("Error while loading Identifier to id map for achievements.", e);
		}
		return mapFromDB;
	}

	/**
	 * checks all for level change relevant achievements for a player
	 * 
	 * @param player
	 */
	public void onLevelChange(Player player) {
		getAndCheckAchievementsInCategory(player, Category.EXPERIENCE);
	}
	

	/**
	 * checks all achievements for a player that should be checked when a player kills sth
	 * 
	 * @param player
	 */
	public void onKill(Player player) {
		getAndCheckAchievementsInCategory(player, Category.FIGHTING);
	}
	
	/**
	 * check all achievements for a player that are relevant on finishing a quest
	 * 
	 * @param player
	 */
	public void onFinishQuest(Player player) {
		getAndCheckAchievementsInCategory(player, Category.QUEST);
	}

	
	/**
	 * retrieve all achievements for a category and check if player has reached each of the found achievements
	 * 
	 * @param player
	 * @param category
	 */
	private void getAndCheckAchievementsInCategory(Player player, Category category) {
		if(achievements.containsKey(category)) {
			List<Achievement> toCheck = achievements.get(category);
			List<Achievement> reached = checkAchievements(player, toCheck);
			notifyPlayerAboutReachedAchievements(player, reached);
		}
	}

	/**
	 * checks for each achievement if the player has reached it. in case of reaching
	 * an achievement it starts logging and notifying about reaching
	 * 
	 * @param player
	 * @param toCheck
	 */
	private List<Achievement> checkAchievements(Player player,
			List<Achievement> toCheck) {
		List<Achievement> reached = new ArrayList<Achievement>();
		for (Achievement achievement : toCheck) {
			if(achievement.isFulfilled(player) && !player.hasReachedAchievement(achievement.getIdentifier())) {
				logReachingOfAnAchievement(player, achievement);
				reached.add(achievement);
			}
		}
		//check for meta achievements and add them to the reached list
		if(!reached.isEmpty()) {
			if(achievements.containsKey(Category.META)) {
				reached.addAll(checkAchievements(player, achievements.get(Category.META)));
			}
		}
		return reached;
	}
	
	/**
	 * Notifies a player about reached achievements via private message
	 * 
	 * @param player
	 * @param achievements
	 */
	private void notifyPlayerAboutReachedAchievements(Player player, List<Achievement> achievements) {
		for (Achievement achievement : achievements) {
			notifyPlayerAboutReachedAchievement(player, achievement);
		}
	}
	
	/**
	 * logs reached achievement to gameEvents table and reached_achievment table
	 * 
	 * @param player
	 * @param achievement
	 */
	private void logReachingOfAnAchievement(Player player, Achievement achievement) {
		String identifier = achievement.getIdentifier();
		String title = achievement.getTitle();
		Category category = achievement.getCategory();
		String playerName = player.getName();
		DBCommandQueue.get().enqueue(new WriteReachedAchievementCommand(identifiersToIds.get(identifier), title, category, playerName));
		player.addReachedAchievement(achievement.getIdentifier());
		new GameEvent(playerName, "reach-achievement", category.toString(), title, identifier).raise();
	}

	/**
	 * notifies the player about reaching an achievement
	 * 
	 * @param player
	 * @param achievement
	 */
	private void notifyPlayerAboutReachedAchievement(Player player,
			Achievement achievement) {
//		player.sendPrivateText("Congratulations! You have reached the "+achievement.getTitle()+" achievement!");
	}

	/**
	 * creates all available achievements
	 * 
	 * @return map with key identifier and value the identified achievement
	 */
	private Map<String, Achievement> createAchievements() {
		Map<String, Achievement> achievementMap = new HashMap<String, Achievement>();
		for(Achievement a : new ExperienceAchievementFactory().createAchievements()) {
			achievementMap.put(a.getIdentifier(), a);
		}
		for(Achievement a : new FightingAchievementFactory().createAchievements()) {
			achievementMap.put(a.getIdentifier(), a);
		}
		for(Achievement a : new QuestAchievementFactory().createAchievements()) {
			achievementMap.put(a.getIdentifier(), a);
		}
		for(Achievement a : new MetaAchievementFactory().createAchievements()) {
			achievementMap.put(a.getIdentifier(), a);
		}
		return achievementMap;
	}

}
