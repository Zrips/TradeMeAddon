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

import com.Zrips.CMIGUI.CMIGui;
import com.Zrips.CMIGUI.CMIGuiButton;
import com.Zrips.CMIGUI.GUIManager.GUIClickType;
import com.Zrips.CMILib.ActionBarTitleMessages;
import com.Zrips.CMILib.ItemManager.CMIMaterial;

import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.Amounts;
import me.Zrips.TradeMe.Containers.OfferButtons;
import me.Zrips.TradeMe.Containers.TradeMap;
import me.Zrips.TradeMe.Containers.TradeModeInterface;
import me.Zrips.TradeMe.Containers.TradeOffer;
import me.Zrips.TradeMe.Containers.TradeResults;
import me.Zrips.TradeMe.Containers.TradeSize;
import me.Zrips.TradeMe.Locale.LC;

public class Money implements TradeModeInterface {

    private String at = "Money";

    static List<ItemStack> AmountButtons = new ArrayList<ItemStack>();
    ItemStack OfferedTradeButton = CMIMaterial.GOLD_INGOT.newItemStack();
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
	map.put("ChangedOfferTitle", "&8Offered &0[amount] &8money");

	map.put("log", "&e[amount] &7Money");
	return map;
    }

    @Override
    public void setAmounts(Amounts amounts) {
	this.amounts = amounts;
    }

    @Override
    public List<ItemStack> getAmountButtons() {
	AmountButtons.add(CMIMaterial.GOLD_NUGGET.newItemStack());
	AmountButtons.add(CMIMaterial.GOLD_INGOT.newItemStack());
	AmountButtons.add(CMIMaterial.GOLD_BLOCK.newItemStack());
	AmountButtons.add(CMIMaterial.DIAMOND.newItemStack());
	return AmountButtons;
    }

    @Override
    public ItemStack getOfferedTradeButton() {
	return OfferedTradeButton;
    }

    @Override
    public OfferButtons getOfferButtons() {
	offerButton.addOfferOff(CMIMaterial.ENDER_PEARL.newItemStack());
	offerButton.addOfferOn(CMIMaterial.ENDER_EYE.newItemStack());
	return offerButton;
    }

    @Override
    public void setTrade(TradeOffer trade, int i) {
	if (plugin.getEconomy().useVaultEcon()) {
	    trade.setP1Money(plugin.getEconomy().getBalance(trade.getP1()));
	    trade.setP2Money(plugin.getEconomy().getBalance(trade.getP2()));
	    trade.getButtonList().add(trade.getPosibleButtons().get(i));
	}
    }

    @Override
    public CMIGui Buttons(final TradeOffer trade, CMIGui GuiInv, final int slot) {

	String firstBalance = plugin.getUtil().TrA((long) trade.getP1Money());
	String firstOffer = plugin.getUtil().TrA(trade.getOffer(at));

	ItemStack ob = trade.getOffer(at) == 0 ? offerButton.getOfferOff() : offerButton.getOfferOn();

	String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));

	String mid = "";
	if (trade.getButtonList().size() > 4)
	    mid = "\n" + plugin.getMessage("MiddleMouse");
	if (trade.Size == TradeSize.REGULAR)
	    GuiInv.updateButton(new CMIGuiButton(slot, plugin.getUtil().makeSlotItem(ob, plugin.getMessage(at, "ToggleButton.Name"),
		plugin.getMessageListAsString(at, "ToggleButton.Lore",
		    "[amount]", plugin.getUtil().TrA(trade.getOffer(at)),
		    "[taxes]", taxes) + mid)) {
		@Override
		public void click(GUIClickType click) {
		    trade.toogleMode(at, click, slot);
		}
	    });

	if (trade.getAction() == at) {

	    String lore = plugin.getMessageListAsString(at, "Button.Lore",
		"[balance]", firstBalance,
		"[offer]", firstOffer,
		"[taxes]", taxes);
	    for (int i = 45; i < 49; i++) {
		TradeMe.getInstance().d(AmountButtons.get(i - 45).getType());
		GuiInv.updateButton(new CMIGuiButton(i, plugin.getUtil().makeSlotItem(AmountButtons.get(i - 45),
		    plugin.getMessage(at, "Button.Name", "[amount]", plugin.getUtil().TrA(amounts.getAmount(i - 45))), lore)) {

		    @Override
		    public void click(GUIClickType click) {
			trade.amountClick(at, click, this.getSlot() - 45, slot);
		    }
		});
	    }
	}

	return GuiInv;
    }

    @Override
    public void Change(TradeOffer trade, int slot, GUIClickType button) {
	Double amount = amounts.getAmount(slot);
	double PlayerMoney = trade.getP1Money();
	double targetMoney = trade.getP2Money();
	double OfferedMoney = trade.getOffer(at);

	if (button.isShiftClick())
	    amount *= 10;

	if (button.isLeftClick()) {
	    if (plugin.EssPresent && OfferedMoney + amount + targetMoney >= 10000000000000D) {
		amount = 10000000000000D - OfferedMoney - targetMoney;
		trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "hardLimit", "[playername]", trade.getP2Name()));
	    }

	    if (OfferedMoney + amount > PlayerMoney) {
		if (PlayerMoney < 0)
		    trade.setOffer(at, 0);
		else
		    trade.setOffer(at, Math.floor(PlayerMoney));
		trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Limit", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
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

	ActionBarTitleMessages.send(trade.getP2(), msg);

	TradeMe.getInstance().getUtil().updateInventoryTitle(trade.getP2(), plugin.getMessage(at, "ChangedOfferTitle", "[playername]", trade.getP1().getName(), "[amount]", trade.getOffer(at)), 1000L);

    }

    @Override
    public ItemStack getOfferedItem(TradeOffer trade) {
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

	if (check(p1, p2, trade.getP1Trade().getOffer(at), trade.getP2Trade().getOffer(at)))
	    return true;
	return false;
    }

    private boolean check(Player p1, Player p2, Double offer1, Double offer2) {
	if (plugin.getEconomy().enabled()) {
	    Double balance = plugin.getEconomy().getBalance(p1);
//	    if (balance < 0 && balance + offer2 < 0 && !plugin.EssPresent) {
//		p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "InLoanYou", "[playername]", p2.getName()));
//		p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "InLoanTarget", "[playername]", p1.getName(), "[amount]",
//		    plugin.getUtil().TrA(-balance)));
//		return false;
//	    }
	    if (balance < offer1) {
		p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
		p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
		return false;
	    }

	    balance = plugin.getEconomy().getBalance(p2);
//	    if (balance < 0 && balance + offer1 < 0 && !plugin.EssPresent) {
//		p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "InLoanYou", "[playername]", p1.getName()));
//		p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "InLoanTarget", "[playername]", p2.getName(), "[amount]",
//		    plugin.getUtil().TrA(-balance)));
//		return false;
//	    }
	    if (balance < offer2) {
		p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
		p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
		return false;
	    }
	    return true;
	}
	return false;
    }

    @Override
    public boolean finish(TradeOffer trade) {
	Player target = trade.getP2();
	Player source = trade.getP1();

	if (!check(source, target, trade.getOffer(this.at), 0D))
	    return false;
	if (trade.getOffer(this.at) <= 0.0D)
	    return true;

	double amount = trade.getOffer(this.at);
	if (amount < 0)
	    return false;
	if (source != null) {
	    if (!plugin.getEconomy().has(source, amount))
		return false;
	    boolean done = plugin.getEconomy().withdrawMoney(source, amount);
	    if (!done)
		return false;
	}
	double tamount = plugin.getUtil().CheckTaxes(this.at, amount);
	if (tamount < 0) {
	    return false;
	}
	trade.setOffer(this.at, tamount);
	if (target != null) {
	    plugin.getEconomy().depositeMoney(target, tamount);
	    target.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Got", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
	}
	return true;
    }

    @Override
    public void getResults(TradeOffer trade, TradeResults TR) {
	if (trade.getOffer(at) > 0) {
	    double amount = trade.getOffer(at);
	    amount = amount - plugin.getUtil().CheckFixedTaxes(at, amount);
	    amount = amount - plugin.getUtil().CheckPercentageTaxes(at, amount);
	    TR.add(at, amount);
	}
    }

    @Override
    public String Switch(TradeOffer trade, GUIClickType button) {
	return null;
    }
}
