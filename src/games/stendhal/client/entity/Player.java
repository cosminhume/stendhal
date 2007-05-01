/* $Id$ */
/***************************************************************************
 *		      (C) Copyright 2003 - Marauroa		      *
 ***************************************************************************
 ***************************************************************************
 *									 *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.				   *
 *									 *
 ***************************************************************************/
package games.stendhal.client.entity;

import games.stendhal.client.StendhalUI;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/** A Player entity */
public class Player extends RPEntity {
	/**
	 * The away message of this player.
	 */
	private String	away;


	/**
	 * Create a player entity.
	 */
	public Player() {
		away = null;
	}


	//
	// Player
	//

	/**
	 * Determine if the player is away.
	 *
	 * @return	<code>true</code> if the player is away.
	 */
	public boolean isAway() {
		return (getAway() != null);
	}


	/**
	 * Get the away message.
	 *
	 * @return	The away text, or <code>null</code> if not away.
	 */
	public String getAway() {
		return away;
	}


	/**
	 * An away message was set/cleared.
	 *
	 * @param	message		The away message, or <code>null</code>
	 *				if no-longer away.
	 */
	protected void onAway(final String message) {
		addFloater(((message != null) ? "Away" : "Back"), Color.blue);
	}

	@Override
	public void onAction(final ActionType at, final String... params) {

		// ActionType at =handleAction(action);
		RPAction rpaction;
		switch (at) {
			case ADD_BUDDY:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				rpaction.put("target", getName());
				at.send(rpaction);
				break;

			default:
				super.onAction(at, params);
				break;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see games.stendhal.client.entity.RPEntity#buildOfferedActions(java.util.List)
	 */
	@Override
	protected void buildOfferedActions(List<String> list) {
		super.buildOfferedActions(list);

		list.add(ActionType.ADD_BUDDY.getRepresentation());
		list.add(ActionType.JOIN_GUILD.getRepresentation());
	}


	//
	// Entity
	//

	/**
	 * Transition method. Create the screen view for this entity.
	 *
	 * @return	The on-screen view of this entity.
	 */
	@Override
	protected Entity2DView createView() {
		return new Player2DView(this);
	}


	/**
	 * Get the area the entity occupies.
	 *
	 * @return	A rectange (in world coordinate units).
	 */
	@Override
	public Rectangle2D getArea() {
		return new Rectangle.Double(getX(), getY() + 1, getWidth(), getHeight());
	}


	//
	// RPObjectChangeListener
	//

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("away")) {
			/*
			 * Filter out a player "changing" to the same message
			 */
			if (!object.has("away") || !object.get("away").equals(changes.get("away"))) {
				away = changes.get("away");
				changed();
				onAway(away);
			}
		}

		// The first time we ignore it.
		if (object != null) {
			if (changes.has("online")) {
				String[] players = changes.get("online").split(",");
				for (String playerName : players) {
					StendhalUI.get().addEventLine(playerName + " has joined Stendhal.", Color.orange);
				}
			}

			if (changes.has("offline")) {
				String[] players = changes.get("offline").split(",");
				for (String playername : players) {
					StendhalUI.get().addEventLine(playername + " has left Stendhal.", Color.orange);
				}
			}
		}
	}

	/**
	 * The object removed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		super.onChangedRemoved(object, changes);

		if (changes.has("away")) {
			away = null;
			changed();
			onAway(null);
		}
	}
}
