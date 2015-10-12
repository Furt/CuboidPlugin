package se.jeremy.minecraft.cuboid;
import java.io.Serializable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("serial")
public class CuboidItem implements Serializable {	
	int amount = 1, durability = 0;
	Material material;
	
	public CuboidItem(ItemStack item){
		this.material = item.getType();
		this.amount = item.getAmount();
		this.durability = item.getDurability();
	}
}
