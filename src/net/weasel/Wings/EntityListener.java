package net.weasel.Wings;

//import javax.persistence.EntityListeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class EntityListener implements Listener//extends org.bukkit.event.entity.EntityListener 
{
	public static Wings plugin;
	
	public EntityListener( Wings instance ) 
	{
		plugin = instance;
	}

	// Prevent damage when allowed to fly
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
	    if ((event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) && ((event.getEntity() instanceof Player)))
	    {
		    try 
		    {
		    	Player player = ((Player)event.getEntity());
		    	
		    	if( Wings.isAllowed(player) )
		    	{
			        if( Wings.isFlying(player) >= 0 ) // does this make sense?
			        {
			    		if( Wings.falldamageWithoutFeather && player.getItemInHand().getType() != Material.FEATHER )
			    			return;

			        	event.setCancelled( true );	
			        }
		    	}
		    }
		    catch (Exception e) 
		    {
		    	// Do nothing..
		    }
	    }
	}
}
