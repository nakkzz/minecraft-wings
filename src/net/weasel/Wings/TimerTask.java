package net.weasel.Wings;

import net.weasel.Wings.Wings.flyingState;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TimerTask implements Runnable
{
	Wings plugin = null;
	Server server = null;
	
	public TimerTask( Wings instance )
	{
		plugin = instance;
		server = plugin.getServer();
	}
	
	@Override
	public void run() 
	{
//		double M = Wings.hoverMultiplier;
		
		World world = null;
		Player player = null;

		for( int W = 0; W < server.getWorlds().size(); W++ )
		{
			world = server.getWorlds().get(W);
			
			for( int P = 0; P < world.getPlayers().size(); P++ )
			{
				player = world.getPlayers().get(P);
				
				if( Wings.isFlying( player ) == flyingState.HOVERING )
				{
/*					if( player.isSneaking() == true ) M = Wings.hoverBoostMultiplier;
					
					double X = Wings.getHoverLocation(player).getX();
					double Y = Wings.getHoverLocation(player).getY();
					double Z = Wings.getHoverLocation(player).getZ();
					
					Vector pVector = new Vector( X, Y , Z );
					Vector hVector = new Vector( 0, M, 0 );
					
					Vector hover = pVector.multiply( hVector );
					hover.clone();
					
					player.setVelocity( new Vector( 0,0,0 ) );
					player.setVelocity( hover );*/

					player.setVelocity( new Vector( 0,0,0 ) );
					
					Location last = Wings.getHoverLocation(player);
					Location current = player.getLocation();
					last.setPitch(current.getPitch());
					last.setYaw(current.getPitch());
					
					player.teleport(last);
					
					
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
			}
		}
	}
}