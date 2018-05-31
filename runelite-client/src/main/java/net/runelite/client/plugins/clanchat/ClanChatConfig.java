
package net.runelite.client.plugins.clanchat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup(
		name = "Clan Chat",
		keyName = "clanchat",
		description = "Configuration for the clan chat plugin"
)
public interface ClanChatConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "showEnterAndLeaveMessages",
			name = "Show member enter and leave messages",
			description = "Configures whether or not to display messages informing who has entered and left the clan chat"
	)
	default boolean showEnterAndLeaveMessages()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "LAEC",
			name = "Message Color",
			description = "Configures the color of the Leave and Enter message"
	)
	default Color getLAEC()
	{
		return new Color(0, 100, 255);
	}
}