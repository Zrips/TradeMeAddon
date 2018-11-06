package me.Zrips.TradeMeAddon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Zrips.CMIGUI.CMIGui;
import com.Zrips.CMIGUI.CMIGuiButton;
import com.Zrips.CMIGUI.GUIManager.GUIClickType;
import com.Zrips.CMILib.ActionBarTitleMessages;
import com.Zrips.CMILib.ItemManager.CMIMaterial;
import com.Zrips.CMILib.VersionChecker.Version;

import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.Amounts;
import me.Zrips.TradeMe.Containers.OfferButtons;
import me.Zrips.TradeMe.Containers.TradeMap;
import me.Zrips.TradeMe.Containers.TradeModeInterface;
import me.Zrips.TradeMe.Containers.TradeOffer;
import me.Zrips.TradeMe.Containers.TradeResults;
import me.Zrips.TradeMe.Containers.TradeSize;
import me.Zrips.TradeMe.Locale.LC;

public class Exp implements TradeModeInterface {

    private String at = "Exp";
    private TradeMe plugin;

    List<ItemStack> AmountButtons = new ArrayList<ItemStack>();
    ItemStack OfferedTradeButton = new ItemStack(Material.STONE);
    OfferButtons offerButton = new OfferButtons();
    Amounts amounts = new Amounts(1, 100, 10000, 1000000);

    public Exp(TradeMe plugin, String name) {
	this.plugin = plugin;
	at = name;
    }

    /* 
     * All locale lines used for this trade mode
     */
    @Override
    public HashMap<String, Object> getLocale() {
	HashMap<String, Object> map = new HashMap<String, Object>();
	map.put("Button.Name", "&2Exp increment by &6[amount]");
	map.put("Button.Lore",
	    Arrays.asList("&eLeft click to add",
		"&eRight click to take",
		"&eHold shift to increase 10 times",
		"&eMaximum available: &6[balance]",
		"&eCurrent exp offer: &6[offer] [taxes]",
		"&eYou will be left with &6[sourcelevel] &elevels and &6[sourceexp] &eexp",
		"&eTarget player will be at &6[targetlevel] &eand have &6[targetexp] &eexp"));
	map.put("ToggleButton.Name", "&2Toggle to exp offer");
	map.put("ToggleButton.Lore", Arrays.asList(
	    "&eCurent exp offer: &6[amount] [taxes]",
	    "&eYou will be left with &6[sourcelevel] &elevels and &6[sourceexp] &eexp",
	    "&eTarget player will be at &6[targetlevel] &eand have &6[targetexp] &eexp"));
	map.put("OfferedButton.Name", "&2[player]'s exp offer");
	map.put("OfferedButton.Lore", Arrays.asList(
	    "&eCurent exp offer: &6[amount] [taxes]",
	    "&e[player] will be left with &6[sourcelevel] &elevels and &6[sourceexp] &eexp",
	    "&eYou will be at &6[targetlevel] &eand have &6[targetexp] &eexp"));
	map.put("Error", "&e[playername] doesn't have enough exp!");
	map.put("Limit", "&eYou dont have enough exp! Amount was set to maximum you can trade: &6[amount]");
	map.put("ChangedOffer", "&6[playername] &ehas changed their exp offer to: &6[amount]");
	map.put("ChangedOfferTitle", "&8Offered &0[amount] &8exp");
	map.put("Got", "&eYou have received &6[amount] &eexp");

	map.put("log", "&e[amount] &7Exp");
	return map;
    }

    /* 
     * Used to set amounts for this trade mode
     * Changed amounts are grabbed from TradeMe config file
     */
    @Override
    public void setAmounts(Amounts amounts) {
	this.amounts = amounts;
    }

    /* 
     * Used to set 4 default amount buttons when changing offer amount
     * Changed values are grabbed from TradeMe config file
     */
    @Override
    public List<ItemStack> getAmountButtons() {
	AmountButtons.add(CMIMaterial.BUCKET.newItemStack());
	AmountButtons.add(CMIMaterial.MILK_BUCKET.newItemStack());
	AmountButtons.add(CMIMaterial.WATER_BUCKET.newItemStack());
	AmountButtons.add(CMIMaterial.LAVA_BUCKET.newItemStack());
	return AmountButtons;
    }

    /* 
     * Used to set default item for other player when trade offered
     * Changed value are grabbed from TradeMe config file
     */
    @Override
    public ItemStack getOfferedTradeButton() {
	OfferedTradeButton = CMIMaterial.EXPERIENCE_BOTTLE.newItemStack();
	return OfferedTradeButton;
    }

    /* 
     * Used to set default items when particular offer exist or not
     * Changed value are grabbed from TradeMe config file
     */
    @Override
    public OfferButtons getOfferButtons() {
	offerButton.addOfferOff(CMIMaterial.GLASS_BOTTLE.newItemStack());
	offerButton.addOfferOn(CMIMaterial.EXPERIENCE_BOTTLE.newItemStack());
	return offerButton;
    }

    /* 
     * Used to particular button in selection row on particular place
     * If not set then player will not have option to trade with that trade mode
     */
    @Override
    public void setTrade(TradeOffer trade, int i) {
	trade.getButtonList().add(trade.getPosibleButtons().get(i));
    }

    /* 
     * Main buttons update event when player changes trade mode or changes trade value
     */
    @Override
    public CMIGui Buttons(final TradeOffer trade, CMIGui GuiInv, final int slot) {

	double offerAmount = trade.getOffer(at);
	ItemStack ob = offerAmount == 0 ? offerButton.getOfferOff() : offerButton.getOfferOn();

	double Sourceleftexp = (int) (getPlayerExperience(trade.getP1()) - offerAmount);
	double newSourceLevel = EXPLevelFromExp(Sourceleftexp, trade.getP1().getLevel());
	double leftSourceExp = Sourceleftexp - EXPlevelToExp(newSourceLevel);
	String taxes = plugin.getUtil().GetTaxesString(at, offerAmount);
	double Targetleftexp = getPlayerExperience(trade.getP2()) + plugin.getUtil().CheckTaxes(at, offerAmount);

	double newTargetLevel = EXPLevelFromExp(Targetleftexp, trade.getP1().getLevel() + trade.getP2().getLevel());
	double leftTargetExp = Targetleftexp - EXPlevelToExp(newTargetLevel);

	String mid = "";
	if (trade.getButtonList().size() > 4)
	    mid = "\n" + plugin.getMessage("MiddleMouse");

	if (trade.Size == TradeSize.REGULAR) {
	    GuiInv.updateButton(new CMIGuiButton(slot, plugin.getUtil().makeSlotItem(ob, plugin.getMessage(at, "ToggleButton.Name"),
		plugin.getMessageListAsString(at, "ToggleButton.Lore",
		    "[amount]", offerAmount,
		    "[taxes]", taxes,
		    "[sourcelevel]", newSourceLevel,
		    "[sourceexp]", leftSourceExp,
		    "[targetlevel]", newTargetLevel,
		    "[targetexp]", leftTargetExp) + mid)) {
		
		// Mandatory override. This will be performed when switching to this game mode. Additional actions can be done. Like playing sounds, effects and so on.
		@Override
		public void click(GUIClickType click) {
		    trade.toogleMode(at, click, slot);
		}
	    });
	}
	if (!trade.getAction().equalsIgnoreCase(at))
	    return GuiInv;

	for (int i = 45; i < 49; i++) {
	    GuiInv.updateButton(new CMIGuiButton(i, plugin.getUtil().makeSlotItem(AmountButtons.get(i - 45),
		plugin.getMessage(at, "Button.Name",
		    "[amount]", plugin.getUtil().TrA(amounts.getAmount(i - 45))),
		plugin.getMessageListAsString(at, "Button.Lore",
		    "[balance]", plugin.getUtil().TrA(getPlayerExperience(trade.getP1())),
		    "[offer]", plugin.getUtil().TrA(offerAmount),
		    "[taxes]", taxes,
		    "[sourcelevel]", newSourceLevel,
		    "[sourceexp]", leftSourceExp,
		    "[targetlevel]", newTargetLevel,
		    "[targetexp]", leftTargetExp))) {

		// Mandatory override. This will be performed when clicking on appropriate amount change button. Additional actions can be done. Like playing sounds, effects and so on.
		@Override
		public void click(GUIClickType click) {
		    trade.amountClick(at, click, this.getSlot() - 45, slot);
		}
	    });
	}

	return GuiInv;
    }

    /* 
     * Event when player click on one of 4 buttons to change trade amount
     * Slot is value from 0 to 3
     */
    @Override
    public void Change(TradeOffer trade, int slot, GUIClickType button) {
	Double amount = amounts.getAmount(slot);
	int PlayerExp = (int) getPlayerExperience(trade.getP1());
	double OfferedExp = trade.getOffer(at);

	if (button.isShiftClick())
	    amount *= 10;

	if (button.isLeftClick())
	    if (OfferedExp + amount > PlayerExp) {
		trade.setOffer(at, PlayerExp);
		trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Limit", "[amount]", OfferedExp));
	    } else if (PlayerExp + OfferedExp + amount > Integer.MAX_VALUE) {
		trade.setOffer(at, Integer.MAX_VALUE);
		trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Limit", "[amount]", OfferedExp));
	    } else {
		trade.addOffer(at, amount);
	    }
	if (button.isRightClick())
	    if (OfferedExp - amount < 0) {
		trade.setOffer(at, 0);
	    } else {
		trade.takeFromOffer(at, amount);
	    }

	String msg = plugin.getMessage(at, "ChangedOffer", "[playername]", trade.getP1().getName(), "[amount]", trade.getOffer(at));
	ActionBarTitleMessages.send(trade.getP2(), msg);

	TradeMe.getInstance().getUtil().updateInventoryTitle(trade.getP2(), plugin.getMessage(at, "ChangedOfferTitle", "[playername]", trade.getP1().getName(), "[amount]", trade.getOffer(at)), 1000L);
    }

    /* 
     * Defines item for another player if trade offer changes
     */
    @Override
    public ItemStack getOfferedItem(TradeOffer trade) {
	if (trade.getOffer(at) <= 0)
	    return null;

	double Sourceleftexp = getPlayerExperience(trade.getP1()) - trade.getOffer(at);
	int newSourceLevel = EXPLevelFromExp(Sourceleftexp, trade.getP1().getLevel());
	double expToNewLevel = EXPlevelToExp(newSourceLevel);
	double leftSourceExp = Sourceleftexp - expToNewLevel;

	String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));

	double totalP2Exp = 0;
	int levelP2 = 0;

	totalP2Exp = getPlayerExperience(trade.getP2());
	levelP2 = trade.getP2().getLevel();

	double Targetleftexp = totalP2Exp + (trade.getOffer(at) - plugin.getUtil().CheckFixedTaxes(at, trade.getOffer(at))
	    - plugin.getUtil().CheckPercentageTaxes(at, trade.getOffer(at)));

	int newTargetLevel = EXPLevelFromExp(Targetleftexp, trade.getP1().getLevel() + levelP2);
	double expToNewTargetLevel = EXPlevelToExp(newTargetLevel);
	double leftTargetExp = Targetleftexp - expToNewTargetLevel;

	ItemStack item = plugin.getUtil().makeSlotItem(OfferedTradeButton, plugin.getMessage(at, "OfferedButton.Name",
	    "[player]", trade.getP1Name()),
	    plugin.getMessageListAsString(at, "OfferedButton.Lore",
		"[player]", trade.getP1().getName(),
		"[amount]", trade.getOffer(at),
		"[taxes]", taxes,
		"[sourcelevel]", newSourceLevel,
		"[sourceexp]", leftSourceExp < 0 ? 0 : leftSourceExp,
		"[targetlevel]", newTargetLevel < 0 ? 0 : newTargetLevel,
		"[targetexp]", leftTargetExp));
	return item;
    }

    /* 
     * Check performed when both players accepts trade
     * Extra checks can be performed to double check if trade can be finalized
     */
    @Override
    public boolean isLegit(TradeMap trade) {
	Player p1 = trade.getP1Trade().getP1();
	Player p2 = trade.getP2Trade().getP1();
	if (trade.getP1Trade().getOffer(at) > 0 && getPlayerExperience(p1) < trade.getP1Trade().getOffer(at)) {
	    p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
	    p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
	    return false;
	}
	if (trade.getP2Trade().getOffer(at) > 0 && getPlayerExperience(p2) < trade.getP2Trade().getOffer(at)) {
	    p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
	    p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
	    return false;
	}
	return true;
    }

    /* 
     * Final action for this trade mode after trade is finally gets green light
     */
    @Override
    public boolean finish(TradeOffer trade) {
	Player target = trade.getP2();
	Player source = trade.getP1();
	if (trade.getOffer(at) <= 0)
	    return false;

	double amount = trade.getOffer(at);

	withdraw(source, amount);
	amount = plugin.getUtil().CheckTaxes(at, amount);

	trade.setOffer(at, amount);
	deposite(target, amount);
	if (target != null)
	    target.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Got", "[amount]", trade.getOffer(at)));
	return true;

    }

    /* 
     * Returns results of this trade mode 
     */
    @Override
    public void getResults(TradeOffer trade, TradeResults TR) {
	if (trade.getOffer(at) <= 0)
	    return;
	double amount = plugin.getUtil().CheckTaxes(at, trade.getOffer(at));
	TR.add(at, amount);
    }

    /* 
     * This event will be fired when player click second time on same trade mode
     * Can be used to switch in example between skills for McMMO exp trade
     * Should return new name of changed skill value
     */
    @Override
    public String Switch(TradeOffer trade, GUIClickType button) {
	return null;
    }

    private void withdraw(Player player, double exp) {
	if (player == null)
	    return;
	double giverExp = getPlayerExperience(player) - exp;
	if (giverExp < 0)
	    giverExp = 0;
	player.setLevel(0);
	player.setExp(0);
	player.setTotalExperience(0);
	player.giveExp((int) giverExp);
    }

    private void deposite(Player player, double exp) {
	if (player == null)
	    return;
	double giverExp = getPlayerExperience(player) + exp;

	if (giverExp > Integer.MAX_VALUE)
	    giverExp = Integer.MAX_VALUE;

	player.setLevel(0);
	player.setExp(0);
	player.setTotalExperience(0);
	player.giveExp((int) giverExp);
    }

    private double getPlayerExperience(Player player) {
	if (player == null)
	    return 0;
	double bukkitExp = (EXPlevelToExp(player.getLevel()) + Math.round(deltaLevelToExp(player.getLevel()) * player.getExp()));
	return bukkitExp > Integer.MAX_VALUE ? Integer.MAX_VALUE : bukkitExp;
    }

    private double EXPlevelToExp(double newSourceLevel) {
	double exp = 0d;
	if (plugin.getVersionChecker().getVersion().isLower(Version.v1_8_R1)) {
	    if (newSourceLevel <= 15) {
		exp = 17 * newSourceLevel;
	    } else if (newSourceLevel <= 30) {
		exp = (3 * newSourceLevel * newSourceLevel / 2) - (59 * newSourceLevel / 2) + 360;
	    } else {
		exp = (7 * newSourceLevel * newSourceLevel / 2) - (303 * newSourceLevel / 2) + 2220;
	    }
	} else {
	    if (newSourceLevel <= 15) {
		exp = newSourceLevel * newSourceLevel + 6 * newSourceLevel;
	    } else if (newSourceLevel <= 30) {
		exp = 2.5 * newSourceLevel * newSourceLevel - 40.5 * newSourceLevel + 360;
	    } else {
		exp = 4.5 * newSourceLevel * newSourceLevel - 162.5 * newSourceLevel + 2220;
	    }
	}

	return exp > Integer.MAX_VALUE ? Integer.MAX_VALUE : exp;
    }

    private int deltaLevelToExp(int level) {
	double exp = 0d;
	if (plugin.getVersionChecker().getVersion().isLower(Version.v1_8_R1)) {
	    if (level <= 15) {
		exp = 17D;
	    } else if (level <= 30) {
		exp = 3D * level - 31;
	    } else {
		exp = 7D * level - 155;
	    }
	} else {
	    if (level <= 15) {
		exp = 2D * level + 7;
	    } else if (level <= 30) {
		exp = 5D * level - 38;
	    } else {
		exp = 9D * level - 158;
	    }
	}

	return exp > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) exp;
    }

    private int EXPLevelFromExp(double sourceleftexp, double d) {
	if (d > 21863)
	    d = 21863;
	for (int i = 1; i <= d + 1; i++) {
	    double levelexp = EXPlevelToExp(i);
	    if (levelexp > sourceleftexp)
		return i - 1;
	}
	return 0;
    }

}
