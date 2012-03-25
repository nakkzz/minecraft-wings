package net.weasel.Wings;

import net.weasel.Wings.Wings.flyingState;

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

	// Allow player flying around depending on state
	@EventHandler
	public void onPlayerMove( PlayerMoveEvent event )
	{
		Player player = event.getPlayer();
		
		if( Wings.isFlying( player ) == flyingState.FLYING )
		{
			if( player.getLocation().getY() < 300 )
			{
				Vector dir = player.getLocation().getDirection();
				player.setVelocity( dir.multiply(Wings.flightSpeed) );
			}
		}

		if( Wings.isFlying( player ) == flyingState.HOVERING )
		{
			double M = Wings.hoverMultiplier;
			
			if( player.isSneaking() == true ) M = Wings.hoverBoostMultiplier;
			
			double X = Wings.getHoverLocation(player).getX();
			double Y = Wings.getHoverLocation(player).getY();
			double Z = Wings.getHoverLocation(player).getZ();
			
			Vector pVector = new Vector( X, Y , Z );
			Vector hVector = new Vector( 0, M, 0 );
			
			Vector hover = pVector.multiply( hVector );
			hover.clone();
			
			player.setVelocity( new Vector( 0,0,0 ) );
//			player.setVelocity( hover );
			
			player.teleport(Wings.getHoverLocation(player));
		}
		
		if( Wings.flyingEatsFeathers == true && Wings.isFlying(player) != flyingState.NOT_FLYING )
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
						Wings.setFlying( player, flyingState.NOT_FLYING );
						player.sendMessage( "You are no longer flying." );
					}
					else
					{
						ItemStack F = new ItemStack( Material.FEATHER, 1 );
						player.getInventory().removeItem(F);
		
						if( player.getInventory().all(Material.FEATHER).get(0).getAmount() < 1 )
						{
							Wings.setFlying( player, flyingState.NOT_FLYING );
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
					Wings.setFlying( player, flyingState.NOT_FLYING );
					player.sendMessage( "You are no longer flying." );
				}
			}
		}
	}
	
	// player using feather
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
					if( Wings.isFlying(player) == flyingState.NOT_FLYING )
					{
						player.sendMessage( "You are now flying." );
						
						if( Wings.featherPoints.containsKey(player) == false )
							Wings.setFeatherPoints( player, Wings.defaultFeatherAmount );
						
						Wings.setFlying( player, flyingState.FLYING );
					}
					else if( Wings.isFlying(player) == flyingState.FLYING )
					{
						Wings.setHoverLocation( player );
						player.setVelocity( new Vector( 0,0,0 ) );
						player.sendMessage( "You are now hovering." );
						Wings.setFlying( player, flyingState.HOVERING );
					}
					else
					{
						player.sendMessage( "You are no longer flying." );
						Wings.setFlying( player, flyingState.NOT_FLYING );
					}
	
					event.setCancelled( true );
				}
			}
		}
	}

	// clean up when player disconnects
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
