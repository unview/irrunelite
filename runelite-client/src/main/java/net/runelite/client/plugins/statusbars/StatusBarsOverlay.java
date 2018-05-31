/*
 * Copyright (c) 2018, Jos <Malevolentdev@gmail.com>
 * Creation date : 26-5-2018
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.statusbars;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Image;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import javax.inject.Inject;
import net.runelite.client.game.SkillIconManager;

@Slf4j
public class StatusBarsOverlay extends Overlay
{
	// Static colors
	final static Color DEPLETED = new Color(25, 25, 25, 150);
	final static Color PRAYER_COLOR = new Color(50, 200, 200, 150);
	final static Color BACKGROUND = new Color(0, 0 , 0, 150);
	final static Color HEALTH_COLOR = new Color(200, 35, 0, 150);
	final static Color POISONED_COLOR = new Color(0, 145, 0, 150);
	final static Color VENOMED_COLOR = new Color(0, 65, 0, 150);
	final static Color COUNTER_COLOR = new Color(255, 255,  255, 255);
	final static Color NO_COLOR = new Color(0, 0, 0, 0);
	final private static int HEIGHT = 252;
	final private static int WIDTH = 20;
	final private static int PADDING = 1;
	private Client client;
	private StatusBarsConfig config;
	private SkillIconManager skillIconManager;
	private FontManager fontManager;
	private Color healthBar;
	private Color counter;

	@Inject
	public StatusBarsOverlay(Client client, StatusBarsConfig config, SkillIconManager skillIconManager)
	{
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.config = config;
		this.skillIconManager = skillIconManager;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		//Variables
		int maxHealth = client.getRealSkillLevel(Skill.HITPOINTS);
		int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
		int currentHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		int poisonState = client.getVar(VarPlayer.IS_POISONED);
		int counterHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int counterPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		int imageSize = 16;
		String counter_health_text;
		String counter_prayer_text;

		//Store images
		BufferedImage healthImage = skillIconManager.getSkillImage(Skill.HITPOINTS, true);
		BufferedImage prayerImage = skillIconManager.getSkillImage(Skill.PRAYER, true);
		BufferedImage noImage = new BufferedImage(17, 17, BufferedImage.TYPE_INT_ARGB);

		//Code to toggle skill icons
		if (!config.enableSkillIcon())
		{
			healthImage = noImage;
			prayerImage = noImage;
		}

		//Code to toggle counters
		if (config.enableCounter())
		{
			counter = COUNTER_COLOR;
		}
		else
		{
			counter = NO_COLOR;
		}

		// If the player gets inflicted with poison or venom the colors will be replaced to indicate this status.
		if (poisonState > 0 && poisonState < 37)
		{
			healthBar = POISONED_COLOR;
		}
		else if (poisonState > 1000000)
		{
			healthBar = VENOMED_COLOR;
		}
		else
		{
			healthBar = HEALTH_COLOR;
		}

		for (Viewport viewport : Viewport.values())
		{
			Widget viewportWidget = client.getWidget(viewport.getViewport());

			if (viewportWidget != null && !viewportWidget.isHidden())
			{
				Widget widget = client.getWidget(viewport.getViewport());
				Point location = viewportWidget.getCanvasLocation();
				Point offsetLeft = viewport.getOffsetLeft();
				Point offsetRight = viewport.getOffsetRight();
				int offsetPrayerX = (location.getX() - offsetRight.getX());
				int offsetHealthX = (location.getX() - offsetLeft.getX());
				int offsetHealthY = (location.getY() - offsetLeft.getY());
				int offsetPrayerY = (location.getY() - offsetRight.getY());
				int height = HEIGHT;
				counter_health_text = Integer.toString(counterHealth);
				counter_prayer_text = Integer.toString(counterPrayer);

				if (viewport == Viewport.RESIZED_BOTTOM && !widget.isHidden())
				{
					offsetPrayerY = (location.getY() - 18 - offsetRight.getY());
					offsetHealthY = (location.getY() - 18 - offsetRight.getY());
					height += 20;
				}
				else if (viewport == Viewport.RESIZED_BOX && widget.isHidden())
				{
					return null;
				}
				else
				{
					offsetPrayerX = (location.getX() + widget.getWidth() - offsetRight.getX());
				}

				// Render the HP and Prayer bar
				renderBar(g, offsetHealthX , offsetHealthY,
						maxHealth, currentHealth, WIDTH + 1, height,
						PADDING, healthBar, healthImage.getScaledInstance(imageSize , imageSize , Image.SCALE_AREA_AVERAGING), counter , counter_health_text);
				renderBar(g, offsetPrayerX, offsetPrayerY ,
						maxPrayer, currentPrayer, WIDTH, height,
						PADDING, PRAYER_COLOR, prayerImage.getScaledInstance(imageSize , imageSize , Image.SCALE_AREA_AVERAGING), counter , counter_prayer_text);
			}
		}
		return null;
	}

	private void renderBar(Graphics2D graphics, int x, int y, int max, int current, int width, int height, int padding, Color filled, Image image, Color counter_color, String counter)
	{
		//draw background
		graphics.setColor(BACKGROUND);
		graphics.fillRect(x, y, width, height);

		//draw bar background
		graphics.setColor(DEPLETED);
		graphics.fillRect(x + padding, y + padding, width - padding * 2, height - padding * 2);

		//draw bar with current health or prayer points
		int filledHeight = getBarHeight(max, current, height);
		graphics.setColor(filled);
		graphics.fillRect(x + padding, y + padding + (height - filledHeight), width - padding * 2, filledHeight - padding * 2);

		//draw counter
		graphics.setColor(counter_color);
		graphics.setFont(FontManager.getRunescapeSmallFont());

		if (config.enableCounter() && !config.enableSkillIcon())
		{
			graphics.drawString(counter, x + 2  + padding, y + 20);
		}
		else
		{
			graphics.drawString(counter, x + 2  + padding, y + 39);
		}

		//draw icons
		graphics.drawImage(image, x + 1 + padding, y + 21 - image.getWidth(null), null);
	}

	//calculate bar height with set size in mind
	private int getBarHeight(int base, int current, int size)
	{
		double ratio = (double) current / (double) base;

		if (ratio >= 1)
		{
			return size;
		}
		else
		{
			return (int) Math.round((ratio * size));
		}
	}
}