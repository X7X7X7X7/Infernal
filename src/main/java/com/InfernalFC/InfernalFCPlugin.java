package com.InfernalFC;

import com.google.common.collect.ObjectArrays;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Slf4j
@PluginDescriptor(
		name = "Infernal FC",
		description = "A plugin used to keep track of clan events/announcements",
		tags = {"Infernal"}
)

public class InfernalFCPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private InfernalFCConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfernalFCOverlay overlay;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private ClientToolbar clientToolbar;
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private InfernalFCPanel panel;
	private NavigationButton uiNavigationButton;

	static final String CONFIG_GROUP = "InfernalFC";
	static final String CHECK = "Infernal lookup";

	@Override
	protected void startUp() throws Exception
	{
		setOverLay();
		startClanPanel();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(uiNavigationButton);
	}


	@Subscribe
	private void onConfigChanged(ConfigChanged event) throws IOException {
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			if (event.getKey().equals("overlay")) {
				setOverLay();
			} else if (event.getKey().equals("col1color") || event.getKey().equals("col2color")){
				panel.removeAll();
				clientToolbar.removeNavigation(uiNavigationButton);
				startClanPanel();
			}
		}
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged event) {
		if (panel.getRanksPanel().isShowing() && event.getContainerId() == InventoryID.INVENTORY.getId()) {
			try {
				SwingUtilities.invokeAndWait(() ->
				{
					panel.getRanksPanel().updateItems();
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		int groupId = WidgetInfo.TO_GROUP(event.getActionParam1());
		String option = event.getOption();

		if (groupId == WidgetInfo.FRIENDS_LIST.getGroupId() || groupId == WidgetInfo.FRIENDS_CHAT.getGroupId() ||
				groupId == WidgetInfo.CHATBOX.getGroupId() &&
				groupId == WidgetInfo.RAIDING_PARTY.getGroupId() || groupId == WidgetInfo.PRIVATE_CHAT_MESSAGE.getGroupId() ||
				groupId == WidgetInfo.IGNORE_LIST.getGroupId())
		{
			if (option.equals("Delete") && groupId != WidgetInfo.IGNORE_LIST.getGroupId())
			{
				return;
			}

			final MenuEntry lookup = new MenuEntry();
			lookup.setOption(CHECK);
			lookup.setTarget(event.getTarget());
			lookup.setType(MenuAction.RUNELITE.getId());
			lookup.setParam0(event.getActionParam0());
			lookup.setParam1(event.getActionParam1());
			lookup.setIdentifier(event.getIdentifier());

			insertMenuEntry(lookup, client.getMenuEntries());
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if ((event.getMenuAction() == MenuAction.RUNELITE || event.getMenuAction() == MenuAction.RUNELITE_PLAYER)
				&& event.getMenuOption().equals(CHECK))
		{
			final String target;
			if (event.getMenuAction() == MenuAction.RUNELITE)
			{
				target = Text.removeTags(event.getMenuTarget());

				panel.SwitchPanel("lookup");
				panel.getLookupPanel().SearchExact(target);
			}
		}
	}

	private void insertMenuEntry(MenuEntry newEntry, MenuEntry[] entries)
	{
		MenuEntry[] newMenu = ObjectArrays.concat(entries, newEntry);
		int menuEntryCount = newMenu.length;
		ArrayUtils.swap(newMenu, menuEntryCount - 1, menuEntryCount - 2);
		client.setMenuEntries(newMenu);
	}

	private void startClanPanel()
	{
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		panel = injector.getInstance(InfernalFCPanel.class);
		uiNavigationButton = NavigationButton.builder()
				.tooltip("Infernal FC")
				.icon(icon)
				.priority(7)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(uiNavigationButton);
	}

	private void setOverLay() {
		if (config.overlay()) {
			overlayManager.add(overlay);
		} else {
			overlayManager.remove(overlay);
		}
	}

	@Provides
	InfernalFCConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InfernalFCConfig.class);
	}
}
