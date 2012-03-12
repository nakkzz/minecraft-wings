package net.weasel.Wings;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class PlayerListener implements Listener // extends org.bukkit.event.player.PlayerListener 
{
	public Wings plugin;
	public Server server;
	
	public PlayerListener(Wings instance)
	{
		plugin = instance;
		server = plugin.getServer();
	}

	@EventHandler
	public void onPlayerMove( PlayerMoveEvent event )
	{
		Player player = event.getPlayer();
		
		if( Wings.isFlying( player ) == 1 )
		{
			if( player.getLocation().getY() < 300 )
			{
				Vector dir = player.getLocation().getDirection();
				player.setVelocity( dir.multiply(Wings.flightSpeed) );
			}
		}

		if( Wings.isFlying( player ) == 2 )
		{
			double M = Wings.hoverMultiplier;
			
			if( player.isSneaking() == true ) M = Wings.hoverBoostMultiplier;
			
			double X = Wings.getHoverLocation(player).getX();
			double Y = Wings.getHoverLocation(player).getY();
			double Z = Wings.getHoverLocation(player).getZ();
			
			Vector pVector = new Vector( X, Y , Z );
			Vector hVector = new Vector( 0, M, 0 );
			
			Vector hover = pVector.multiply( hVector );
			
			player.setVelocity( new Vector( 0,0,0 ) );
			player.setVelocity( hover );
		}
		
		if( Wings.flyingEatsFeathers == true && Wings.isFlying(player) > 0 )
		{
			Integer fAmount = Wings.getFeatherPoints(player);
			
			fAmount--;
			
			Wings.setFeatherPoints( player, fAmount );
			
			if( fAmount < 1 )
			{
				try
				{
					if( player.getInventory().all(Material.FEATHER).get(0).getAmount() < 1 )
					{
						System.out.println( "out of feathers (OPM)");
						Wings.setFlying( player, 0 );
						player.sendMessage( "You are no longer flying." );
					}
					else
					{
						ItemStack F = new ItemStack( Material.FEATHER, 1 );
						player.getInventory().removeItem(F);
		
						if( player.getInventory().all(Material.FEATHER).get(0).getAmount() < 1 )
						{
							Wings.setFlying( player, 0 );
							player.sendMessage( "You are no longer flying." );
						}
						else
						{
							Wings.setFeatherPoints( player, Wings.defaultFeatherAmount );
						}
					}
		
					Wings.setFeatherPoints( player, Wings.defaultFeatherAmount );
				}
				catch( Exception e )
				{
					Wings.setFlying( player, 0 );
					player.sendMessage( "You are no longer flying." );
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event )
	{
		Player player = event.getPlayer();
		
		if( Wings.isAllowed(player) == true )
		{
			if( player.getItemInHand().getType() == Material.FEATHER )
			{
				if( event.getAction() == Action.RIGHT_CLICK_AIR || 
					event.getAction() == Action.RIGHT_CLICK_BLOCK )
				{
					if( Wings.isFlying(player) == 0 )
					{
						player.sendMessage( "You are now flying." );
						
						if( Wings.featherPoints.containsKey(player) == false )
							Wings.setFeatherPoints( player, Wings.defaultFeatherAmount );
						
						Wings.setFlying( player, 1 );
					}
					else if( Wings.isFlying(player) == 1 )
					{
						Wings.setHoverLocation( player );
						player.setVelocity( new Vector( 0,0,0 ) );
						player.sendMessage( "You are now hovering." );
						Wings.setFlying( player, 2 );
					}
					else
					{
						player.sendMessage( "You are no longer flying." );
						Wings.setFlying( player, 0 );
					}
	
					event.setCancelled( true );
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit( PlayerQuitEvent event )
	{
		if( Wings.flyingPlayers.containsKey(event.getPlayer() ) )
		{
			Wings.flyingPlayers.remove( event.getPlayer() );
			System.out.println( "[Wings] Player quit; removed from list." );
		}
	}
}
