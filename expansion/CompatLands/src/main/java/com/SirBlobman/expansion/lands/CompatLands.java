package com.SirBlobman.expansion.lands;

import java.io.File;

import com.SirBlobman.combatlogx.config.ConfigLang;
import com.SirBlobman.combatlogx.expansion.NoEntryExpansion;
import com.SirBlobman.combatlogx.utility.PluginUtil;
import com.SirBlobman.expansion.lands.config.ConfigLands;
import com.SirBlobman.expansion.lands.listener.ListenLands;
import com.SirBlobman.expansion.lands.utility.LandsUtil;

public class CompatLands extends NoEntryExpansion {
	public static File FOLDER;

	public String getUnlocalizedName() {
		return "CompatLands";
	}

	public String getName() {
		return "Lands Compatibility";
	}

	public String getVersion() {
		return "14.7";
	}

	@Override
	public boolean canEnable() {
		if(!PluginUtil.isEnabled("Lands", "Angeschossen")) {
			print("Could not find Lands plugin.");
			return false;
		}

		return true;
	}

	@Override
	public void onEnable() {
		FOLDER = getDataFolder();
		ConfigLands.load();

		ListenLands listener = new ListenLands(this);
		PluginUtil.regEvents(listener);
	}
	
	@Override
	public void disable() {
		super.disable();
		
		LandsUtil.onDisable();
	}
	
	@Override
	public void onConfigReload() {
		ConfigLands.load();
	}

	@Override
	public double getKnockbackStrength() {
		return ConfigLands.NO_ENTRY_KNOCKBACK_STRENGTH;
	}

	@Override
	public NoEntryMode getNoEntryMode() {
		return ConfigLands.getNoEntryMode();
	}

	@Override
	public String getNoEntryMessage(boolean mobEnemy) {
		String messageKey = "messages.expansions.lands compatibility.no entry";
		return ConfigLang.getWithPrefix(messageKey);
	}

	@Override
	public int getNoEntryMessageCooldown() {
		return ConfigLands.MESSAGE_COOLDOWN;
	}
}