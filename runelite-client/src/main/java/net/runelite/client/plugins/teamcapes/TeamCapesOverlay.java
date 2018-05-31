/*
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
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
package net.runelite.client.plugins.teamcapes;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class TeamCapesOverlay extends Overlay
{
	private final int MAX_CLAN_NAME_LENGTH = 35;
	private final int MIN_PX_BETWEEN_NAME_COUNT = 4;

	private final TeamCapesPlugin plugin;
	private final TeamCapesConfig config;
	private final PanelComponent panelComponent = new PanelComponent();
	private Map<Integer, String> teamNames = new HashMap<>();

	@Inject
	TeamCapesOverlay(TeamCapesPlugin plugin, TeamCapesConfig config)
	{
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<Integer, Integer> teams = plugin.getTeams();
		if (teams.isEmpty())
		{
			return null;
		}

		final FontMetrics metrics = graphics.getFontMetrics();

		// Format w/ example (comma separated): teamcape#=TEAMNAME
		// 26=Intense Redemption,30=FOE
		teamNames.clear();
		if (this.config.getCustomCapeNames().length() >= 3)
		{
			for (String teamName : this.config.getCustomCapeNames().split(","))
			{
				String[] values = teamName.split("=");
				int capeNum;
				String capeName;
				try
				{
					capeNum = Integer.parseInt(values[0]);
					capeName = values[1];
				}

				catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
				{
					continue;
				}

				teamNames.put(capeNum, capeName);
			}
		}

		panelComponent.getChildren().clear();
		for (Map.Entry<Integer, Integer> team : teams.entrySet())
		{
			String capeName;

			if (team.getValue() >= config.getMinimumCapeCount())
			{
				if (teamNames.containsKey(team.getKey()))
				{
					capeName = teamNames.get(team.getKey());

					if (capeName.length() > MAX_CLAN_NAME_LENGTH)
					{
						capeName = capeName.substring(0, MAX_CLAN_NAME_LENGTH);
						teamNames.replace(team.getKey(), capeName);
					}

					while (((FontMetrics) metrics).stringWidth(capeName) >=
							(panelComponent.getWidth() -
									metrics.stringWidth(Integer.toString(team.getValue())) -
									PanelComponent.LEFT_BORDER - PanelComponent.RIGHT_BORDER -
									MIN_PX_BETWEEN_NAME_COUNT))
					{
						capeName = capeName.substring(0, capeName.length() - 1);
					}
				}
				else
				{
					capeName = "Team - ";
				}
				panelComponent.getChildren().add(LineComponent.builder()
						.left(capeName + " - " + Integer.toString(team.getKey()))
						.right(Integer.toString(team.getValue()))
						.build());
			}
		}
		return panelComponent.render(graphics);
	}
}