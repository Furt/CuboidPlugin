package me.furt.cuboidplugin;
import java.io.Serializable;

import org.bukkit.entity.Item;

@SuppressWarnings("serial")
public class CuboidItem implements Serializable{	
	int itemId = 1, amount = 1, slot = -1;
	public CuboidItem(Item item){
		this.itemId = item.getItemId();
		this.amount = item.getAmount();
		this.slot = item.getSlot();
	}
}
