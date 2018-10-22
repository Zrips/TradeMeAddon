package me.Zrips.TradeMeAddon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.AmountClickAction;
import me.Zrips.TradeMe.Containers.TradeAction;

public class TradeMeAddon extends JavaPlugin {

    @Override
    public void onEnable() {
	// ExpAlternative - Should be unique for your trade mode and this will define permission node required to trade in this mode
	// AmountClickAction.Amounts - defines if this trade mode uses amount buttons or it will be double click to confirm trade
	// Last variable defines if trade will have more than one possible trade variable, like McMMO have different skills
	TradeAction tradeAction = new TradeAction("ExpAlternative", AmountClickAction.Amounts, false);	
	TradeMe.getInstance().addNewTradeMode(tradeAction, new Exp(TradeMe.getInstance(), "ExpAlternative"));
	
	TradeAction tradeAction2 = new TradeAction("MoneyAlternative", AmountClickAction.Amounts, false);	
	TradeMe.getInstance().addNewTradeMode(tradeAction2, new Money(TradeMe.getInstance(), "MoneyAlternative"));
	
	// Reloads TradeMe config files to implement new trade mode
	TradeMe.getInstance().getConfigManager().reload();
	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[TradeMeAddon] " + ChatColor.GOLD + "Injected alternative Exp trade mode");
    }

}
