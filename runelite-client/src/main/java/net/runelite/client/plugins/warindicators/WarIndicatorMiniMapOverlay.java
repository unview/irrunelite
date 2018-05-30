/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
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
package net.runelite.client.plugins.warindicators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.apache.commons.lang3.ArrayUtils;

@Singleton
public class WarIndicatorMiniMapOverlay extends Overlay
{
	private final WarIndicatorService warIndicatorService;
	private final WarIndicatorConfig config;

	@Inject
	private WarIndicatorMiniMapOverlay(WarIndicatorConfig config, WarIndicatorService warIndicatorService)
	{
		this.config = config;
		this.warIndicatorService = warIndicatorService;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		warIndicatorService.forEachPlayer((player, color) -> renderPlayerOverlay(graphics, player, color));
		return null;
	}

	private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color)
	{
		final String name = actor.getName().replace('\u00A0', ' ');
		final net.runelite.api.Point minimapLocation = actor.getMinimapLocation();

		String[] callers = config.getActiveCallers().split(", ");
		String[] targets = config.getTargetedSnipes().split(", ");

		if (config.callerMinimap() && ArrayUtils.contains(callers, actor.getName()))
		{
			if (minimapLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, minimapLocation, name, color);
			}
			}

		if (config.snipeMinimap() && ArrayUtils.contains(targets, actor.getName()))
		{
			if (minimapLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, minimapLocation, name, color);
			}
		}
	}
}