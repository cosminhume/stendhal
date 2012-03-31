/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;


import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.sprite.SpriteStore;


import org.apache.log4j.Logger;

/**
 * The 2D view of a spell.
 */
public class Spell2DView extends Entity2DView {

	private static final Logger logger = Logger.getLogger(Spell2DView.class);

	//
	// Entity2DView
	//

	/**
	 * Build the visual representation of this entity.
	 */
	@Override
	protected void buildRepresentation(IEntity entity) {
		String translate = translate(getClassResourcePath());
		logger.debug("Sprite path: " + translate);
		setSprite(SpriteStore.get()
				.getSprite(translate));
	}


	/**
	 * Determines on top of which other entities this entity should be drawn.
	 * Entities with a high Z index will be drawn on top of ones with a lower Z
	 * index.
	 *
	 * Also, players can only interact with the topmost entity.
	 *
	 * @return The drawing index.
	 */
	@Override
	public int getZIndex() {
		return 7000;
	}

	/**
	 * Translate a resource name into it's sprite image path.
	 *
	 * @param name
	 *            The resource name.
	 *
	 * @return The full resource name.
	 */
	@Override
	protected String translate(final String name) {
		String translatedname = translateName(name);
		return "data/sprites/spells/" + translatedname + ".png";
	}

	//
	// EntityChangeListener
	//

	private String translateName(String name) {
		return name.replaceAll(" ", "_");
	}

	/**
	 * An entity was changed.
	 *
	 * @param entity
	 *            The entity that was changed.
	 * @param property
	 *            The property identifier.
	 */
	@Override
	public void entityChanged(final IEntity entity, final Object property) {
		super.entityChanged(entity, property);

		if (property == IEntity.PROP_CLASS) {
			representationChanged = true;
		}
	}

	//
	// EntityView
	//

	/**
	 * Perform the default action.
	 */
	@Override
	public void onAction() {
		onAction(ActionType.USE);
	}

	/**
	 * Perform an action.
	 *
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		if (isReleased()) {
			return;
		}
		switch (at) {
		case USE:
			j2DClient.get().switchToSpellState(this.entity.getRPObject());
			break;


		default:
			super.onAction(at);
			break;
		}
	}

}
