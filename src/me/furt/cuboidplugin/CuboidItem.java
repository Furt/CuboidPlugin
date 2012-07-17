package me.furt.cuboidplugin;
import java.io.Serializable;

import org.bukkit.inventory.ItemStack;

@SuppressWarnings("serial")
public class CuboidItem implements Serializable{	
	int itemId = 1, amount = 1, durability = 0;
	public CuboidItem(ItemStack item){
		this.itemId = item.getTypeId();
		this.amount = item.getAmount();
		this.durability = item.getDurability();
	}
}
