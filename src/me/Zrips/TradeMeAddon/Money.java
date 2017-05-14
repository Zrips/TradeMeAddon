package me.Zrips.TradeMeAddon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.Zrips.TradeMe.Debug;
import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.Amounts;
import me.Zrips.TradeMe.Containers.OfferButtons;
import me.Zrips.TradeMe.Containers.TradeInfo;
import me.Zrips.TradeMe.Containers.TradeMap;
import me.Zrips.TradeMe.Containers.TradeModeInterface;
import me.Zrips.TradeMe.Containers.TradeResults;
import me.Zrips.TradeMe.Containers.TradeSize;

public class Money implements TradeModeInterface {

    private String at = "Money";

    List<ItemStack> AmountButtons = new ArrayList<ItemStack>();
    ItemStack OfferedTradeButton = new ItemStack(Material.STONE);
    OfferButtons offerButton = new OfferButtons();
    Amounts amounts = new Amounts(1, 100, 10000, 1000000);
    private TradeMe plugin;

    public Money(TradeMe plugin, String name) {
	this.plugin = plugin;
	at = name;
    }

    @Override
    public HashMap<String, Object> getLocale() {
	HashMap<String, Object> map = new HashMap<String, Object>();
	map.put("Button.Name", "&2Money increment by &6[amount]");
	map.put("Button.Lore", Arrays.asList(
	    "&eLeft click to add",
	    "&eRight click to take",
	    "&eHold shift to increase 10 times",
	    "&eMaximum available: &6[balance]",
	    "&eCurrent money offer: &6[offer] [taxes]"));
	map.put("ToggleButton.Name", "&2Toggle to money offer");
	map.put("ToggleButton.Lore", Arrays.asList("&eCurent money offer: &6[amount] [taxes]"));
	map.put("OfferedButton.Name", "&2[player]'s money offer");
	map.put("OfferedButton.Lore", Arrays.asList("&eCurent money offer: &6[amount] [taxes]"));
	map.put("Error", "&e[playername] doesn't have enough money!");
	map.put("Limit", "&eYou dont have enough money! Amount was set to maximum you can trade: &6[amount]");
	map.put("hardLimit", "&6[playername] &ecant have more than 10,000,000,000,000 money!");
	map.put("InLoanTarget", "&eYour offered money amount is to low to get &6[playername] &eout of loan! offer atleast &6[amount]");
	map.put("InLoanYou", "&6[playername] &eoffered money amount is to low to get you out of loan!");
	map.put("Got", "&eYou have received &6[amount] &emoney");
	map.put("CantWidraw", "&cCan't widraw money from player! ([playername])");
	map.put("ChangedOffer", "&6[playername] &ehas changed their money offer to: &6[amount]");

	map.put("log", "&e[amount] &7Money");
	return map;
    }

    @Override
    public void setAmounts(Amounts amounts) {
	this.amounts = amounts;
    }

    @Override
    public List<ItemStack> getAmountButtons() {
	AmountButtons.add(new ItemStack(Material.GOLD_NUGGET, 1, (byte) 0));
	AmountButtons.add(new ItemStack(Material.GOLD_INGOT, 1, (byte) 0));
	AmountButtons.add(new ItemStack(Material.GOLD_BLOCK, 1, (byte) 0));
	AmountButtons.add(new ItemStack(Material.DIAMOND, 1, (byte) 0));
	return AmountButtons;
    }

    @Override
    public ItemStack getOfferedTradeButton() {
	OfferedTradeButton = new ItemStack(Material.GOLD_INGOT, 1, (byte) 0);
	return OfferedTradeButton;
    }

    @Override
    public OfferButtons getOfferButtons() {
	offerButton.addOfferOff(new ItemStack(Material.ENDER_PEARL, 1, (byte) 0));
	offerButton.addOfferOn(new ItemStack(Material.EYE_OF_ENDER, 1, (byte) 0));
	return offerButton;
    }

    @Override
    public void setTrade(TradeInfo trade, int i) {
	if (plugin.getEconomy().useVaultEcon()) {
	    trade.setP1Money(plugin.getEconomy().getBalance(trade.getP1()));
	    trade.setP2Money(plugin.getEconomy().getBalance(trade.getP2()));
	    trade.getButtonList().add(trade.getPosibleButtons().get(i));
	}
    }

    @Override
    public Inventory Buttons(TradeInfo trade, Inventory GuiInv, int slot) {

	String firstBalance = plugin.getUtil().TrA((long) trade.getP1Money());
	String firstOffer = plugin.getUtil().TrA(trade.getOffer(at));

	ItemStack ob = trade.getOffer(at) == 0 ? offerButton.getOfferOff() : offerButton.getOfferOn();

	String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));

	String mid = "";
	if (trade.getButtonList().size() > 4)
	    mid = "\n" + plugin.getMessage("MiddleMouse");
	if (trade.Size == TradeSize.REGULAR)
	    GuiInv.setItem(slot, plugin.getUtil().makeSlotItem(ob, plugin.getMessage(at, "ToggleButton.Name"),
		plugin.getMessageListAsString(at, "ToggleButton.Lore",
		    "[amount]", plugin.getUtil().TrA(trade.getOffer(at)),
		    "[taxes]", taxes) + mid));

	if (trade.getAction() == at) {

	    String lore = plugin.getMessageListAsString(at, "Button.Lore",
		"[balance]", firstBalance,
		"[offer]", firstOffer,
		"[taxes]", taxes);
	    for (int i = 45; i < 49; i++) {
		GuiInv.setItem(i, plugin.getUtil().makeSlotItem(AmountButtons.get(i - 45),
		    plugin.getMessage(at, "Button.Name", "[amount]", amounts.getAmount(i - 45)), lore));
	    }
	}
	return GuiInv;
    }

    @Override
    public void Change(TradeInfo trade, int slot, ClickType button) {
	Double amount = amounts.getAmount(slot);
	double PlayerMoney = trade.getP1Money();
	double targetMoney = trade.getP2Money();
	double OfferedMoney = trade.getOffer(at);

	if (button.isShiftClick())
	    amount *= 10;

	if (button.isLeftClick()) {
	    if (plugin.EssPresent && OfferedMoney + amount + targetMoney >= 10000000000000D) {
		amount = 10000000000000D - OfferedMoney - targetMoney;
		trade.getP1().sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "hardLimit", "[playername]", trade.getP2Name()));
	    }

	    if (OfferedMoney + amount > PlayerMoney) {
		if (PlayerMoney < 0)
		    trade.setOffer(at, 0);
		else
		    trade.setOffer(at, PlayerMoney);
		trade.getP1().sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Limit", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
	    } else {
		trade.addOffer(at, amount);
	    }
	}
	if (button.isRightClick())
	    if (OfferedMoney - amount < 0) {
		trade.setOffer(at, 0);
	    } else {
		trade.takeFromOffer(at, amount);
	    }

	String msg = plugin.getMessage(at, "ChangedOffer", "[playername]", trade.getP1Name(), "[amount]", plugin.getUtil().TrA(trade.getOffer(at)));

	plugin.getAb().send(trade.getP2(), msg);
    }

    @Override
    public ItemStack getOfferedItem(TradeInfo trade) {
	if (trade.getOffer(at) > 0) {
	    String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));
	    ItemStack item = plugin.getUtil().makeSlotItem(OfferedTradeButton,
		plugin.getMessage(at, "OfferedButton.Name",
		    "[player]", trade.getP1().getName()),
		plugin.getMessageListAsString(at, "OfferedButton.Lore",
		    "[amount]", plugin.getUtil().TrA(trade.getOffer(at)),
		    "[taxes]", taxes));
	    return item;
	}
	return null;
    }

    @Override
    public boolean isLegit(TradeMap trade) {
	Player p1 = trade.getP1Trade().getP1();
	Player p2 = trade.getP2Trade().getP1();

	if (plugin.getEconomy().enabled()) {
	    Double balance = plugin.getEconomy().getBalance(p1);
	    if (balance < 0 && balance + trade.getP2Trade().getOffer(at) < 0 && !plugin.EssPresent) {
		p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "InLoanYou", "[playername]", p2.getName()));
		p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "InLoanTarget", "[playername]", p1.getName(), "[amount]",
		    plugin.getUtil().TrA(-balance)));
		return false;
	    }
	    if (balance > 0 && balance < trade.getP1Trade().getOffer(at)) {
		p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
		p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
		return false;
	    }

	    balance = plugin.getEconomy().getBalance(p2);
	    if (balance < 0 && balance + trade.getP1Trade().getOffer(at) < 0 && !plugin.EssPresent) {
		p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "InLoanYou", "[playername]", p1.getName()));
		p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "InLoanTarget", "[playername]", p2.getName(), "[amount]",
		    plugin.getUtil().TrA(-balance)));
		return false;
	    }
	    if (balance > 0 && balance < trade.getP2Trade().getOffer(at)) {
		p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
		p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
		return false;
	    }
	}

	return true;
    }

    @Override
    public boolean finish(TradeInfo trade) {
	Player target = trade.getP2();
	Player source = trade.getP1();
	if (source == null || target == null)
	    return false;

	if (trade.getOffer(at) > 0) {

	    double amount = trade.getOffer(at);

	    if (!plugin.getEconomy().has(source, amount))
		return false;

	    boolean done = false;
	    done = plugin.getEconomy().withdrawMoney(source, amount);
	    if (done) {
		amount = plugin.getUtil().CheckTaxes(at, amount);
		trade.setOffer(at, amount);
		plugin.getEconomy().depositeMoney(target, amount);
		target.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Got", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
		return true;
	    }

	    target.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "CantWidraw", "[playername]", source.getName()));

	}
	return false;
    }

    @Override
    public void getResults(TradeInfo trade, TradeResults TR) {
	if (trade.getOffer(at) > 0) {
	    double amount = trade.getOffer(at);
	    amount = amount - plugin.getUtil().CheckFixedTaxes(at, amount);
	    amount = amount - plugin.getUtil().CheckPercentageTaxes(at, amount);
	    TR.add(at, amount);
	}
    }

    @Override
    public String Switch(TradeInfo trade, ClickType button) {
	return null;
    }
}
