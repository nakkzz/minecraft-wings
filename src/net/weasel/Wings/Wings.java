package net.weasel.Wings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Server;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.*;
/*import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;*/

public class Wings extends JavaPlugin 
{
	public final static HashMap<Player,Integer> flyingPlayers = new HashMap<Player,Integer>();
	public final static HashMap<Player,Location> hoverLocs = new HashMap<Player,Location>();
	public final static HashMap<Player,Integer> featherPoints = new HashMap<Player,Integer>();
	
	public static int tTask = 0;
	public static BukkitScheduler timer = null;
	public static Wings plugin;
	public static Server server;
	public static PermissionManager Permissions;
	public static double flightSpeed;
	public static double hoverMultiplier;
	public static double hoverBoostMultiplier;
	public static boolean allowOps;
	public static boolean flyingEatsFeathers;
	public static Integer defaultFeatherAmount;
	public static boolean falldamageWithoutFeather;
	public static String iniFile = "plugins/Wings/Settings.ini";
	
	@Override
	public void onDisable() 
	{
		try
		{
			timer.cancelTask( tTask );
		}
		catch( Exception e )
		{
			e.getMessage();
		}
		
		System.out.println( "[Wings] Wings v" + this.getDescription().getVersion() + " disabled." );
	}

	// initialize
	@Override
	public void onEnable() 
	{
		checkConfig();
		setupPermissions();
		
		plugin = this;
//		server = getServer();
//		Priority priority = Event.Priority.Monitor;
		hoverMultiplier = getDblSetting( "hoverMultiplier", 0.0012410253 );
		hoverBoostMultiplier = getDblSetting( "hoverBoostMultiplier", 0.00555 );
		allowOps = getBooleanSetting( "allowOps", true );
		flyingEatsFeathers = getBooleanSetting( "flyingEatsFeathers", false );
		defaultFeatherAmount = getIntSetting( "defaultFeatherAmount", 100 );
		flightSpeed = getDblSetting( "flightSpeed", 1.0 );
		falldamageWithoutFeather = getBooleanSetting( "falldamageWithoutFeather", false );
		timer = getServer().getScheduler();
		
//		PluginManager pm = server.getPluginManager();
		PlayerListener pListener = new PlayerListener(this);
		EntityListener eListener = new EntityListener(this);
		TimerTask task = new TimerTask(plugin);
		
		getServer().getPluginManager().registerEvents(pListener, plugin);
		getServer().getPluginManager().registerEvents(eListener, plugin);
/*		pm.registerEvent(Event.Type.PLAYER_QUIT, pListener, EventPriority.MONITOR, plugin );
		pm.registerEvent(Event.Type.PLAYER_MOVE, pListener, EventPriority.MONITOR, plugin );
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, EventPriority.MONITOR, plugin );
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, eListener, EventPriority.MONITOR, plugin );*/
	
		tTask = timer.scheduleSyncRepeatingTask( plugin, task, 1, 1 );
		
		System.out.println( "[Wings] Wings v" + this.getDescription().getVersion() + " loaded." );
		System.out.println( "[Wings] Hover multiplier set to " + hoverMultiplier );
		System.out.println( "[Wings] Hover boost multiplier set to " + hoverBoostMultiplier );
		System.out.println( "[Wings] Flying consumes feathers: " + ( flyingEatsFeathers ? "TRUE" : "FALSE" ) );
		
		if( flyingEatsFeathers == true )
			System.out.println( "[Wings] Default per-feather amount to consume: " + defaultFeatherAmount );
			
		System.out.println( "[Wings] Ops allowed to fly regardless: " + ( allowOps ? "TRUE" : "FALSE" ) );
	}
	
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args )
	{
		return false;
	}
    
    public static void checkConfig()
    {
    	File iniDir = new File( "plugins/Wings" );
    	
    	if( iniDir.exists() == false )
    	{
	    	if( iniDir.mkdir() == true )
	    		System.out.println( "[Wings] Created Settings.ini folder: plugins/Wings/" );
    	}
    	
    	File ini = new File(iniFile);
    	
    	if( ini.exists() == false )
    	{
    		if( createIniFile() == true )
    		{
    			System.out.println( "New Settings.ini file created with default settings." );
    		}
    	}
    	
    }
	
	public static Integer isFlying(final Player player)
	{
		if(flyingPlayers.containsKey(player))
			return flyingPlayers.get(player);
		else
			return 0;
	}

	public static Location getHoverLocation(final Player player)
	{
		if(hoverLocs.containsKey(player))
			return hoverLocs.get(player);
		else
			return player.getLocation();
	}

	public static void setFlying(final Player player, final Integer value)
	{
		flyingPlayers.put(player,value);
	}

	public static void setHoverLocation(final Player player )
	{
		hoverLocs.put(player,player.getLocation());
	}

	public static void setFeatherPoints(final Player player, int points )
	{
		featherPoints.put(player,points);
	}

	public static Integer getFeatherPoints(final Player player )
	{
		if( featherPoints.containsKey(player) == true )
			return( featherPoints.get(player) );
		else
		{
			featherPoints.put( player, defaultFeatherAmount );
			return( defaultFeatherAmount );
		}
	}

	public static void logOutput( String text )
	{
		System.out.println( "[Wings] " + text );
	}

	private void setupPermissions() 
	{
		if (Wings.Permissions == null) 
	    {
		    if(getServer().getPluginManager().isPluginEnabled("PermissionsEx"))
			{
	    		Wings.Permissions = PermissionsEx.getPermissionManager();
		    }
		    else 
	    	{
	    		logOutput("Permission system not detected, defaulting to OP");
	        }
	    }
	}

	public static boolean isAllowed( Player player )
	{
		if( Permissions == null )
		{
			return true;
		}
		
		if( Permissions.has(player, "wings.fly"))
		{
		    return true;
		}
		else
		{
			if( player.isOp() == true && allowOps == true )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

    public String[] getSetting( String which, String Default )
    {
        return( getSettingValue(iniFile, which, Default, "" ) );
    }

    public boolean getBooleanSetting( String which, boolean dValue )
    {
    	boolean retVal;
    	
    	String dString = ( dValue ? "true" : "false" );
    	
        if( getSettingValue(iniFile, which, dString, "" )[0].equalsIgnoreCase("true") )
        	retVal = true;
        else
        	retVal = false;
        
        return( retVal );
    }
    
	public String[] getSettingValue(String fileName, String optionName, String defaultValue, String splitValue)
    {
    	Boolean gotLine; // Verification variable
    	String[] returnValue = new String[100]; // Settings max at 100 values
    	String curLine;
    	
    	gotLine = false; // We don't have the settings found yet
    	
    	if( new File(fileName).exists() == false )
    	{
    		return( new String("File not found.ZQX").split("ZQX") );
    	}
		if(splitValue.equals("")) {
			splitValue = "afs4wfa3waawfa3dogrsijkge5ssioeguhwar3awwa3rar";
		}
    	
        try {
        	// Get the line from the file
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader in = new BufferedReader(new InputStreamReader(fstream));
			
			while(in.ready()) 
			{
				curLine = in.readLine().toString();
				if(curLine.split("=", 2)[0].equalsIgnoreCase(optionName)) {
					returnValue = new String[100];
					returnValue = curLine.split("=", 2)[1].split(splitValue);
					gotLine = true;
				}
			}
			
			in.close();
			
			// If the line does not exist, create it
			if(!gotLine) 
			{
                returnValue = defaultValue.split(splitValue);
                FileOutputStream out;
                PrintStream p;
                try {
	                out = new FileOutputStream(fileName, true);
	                p = new PrintStream( out );
	                p.println (optionName+"="+defaultValue);
	                p.close();
                } catch (Exception e) {
                	logOutput("Error writing to file");
                }
			}
		}
        catch (Exception e) {
        	logOutput("-=-");
        	logOutput("File input error: "+e.toString());
        	logOutput("File input error: "+e.getStackTrace().toString());
        	logOutput("-=-");
		}
		finally {
		}
		
		return returnValue;
    }

    public Integer getIntSetting( String item, Integer dValue )
    {
    	Integer retVal = Integer.parseInt( getSettingValue(iniFile, item, dValue.toString(), "" )[0]);
    	return retVal;
    }

    public double getDblSetting( String item, double d )
    {
    	double retVal = Double.parseDouble( getSettingValue(iniFile, item, Double.toString(d), "" )[0]);
    	return retVal;
    }

    public Float getFloatSetting( String item, Float dValue )
    {
    	Float retVal = Float.valueOf(getSettingValue(iniFile, item, dValue.toString(), "" )[0]);
    	return retVal;
    }
    
    // generate a new config file
    public static boolean createIniFile()
    {
    	boolean retVal = false;
    	
    	try 
    	{
			FileWriter outFile = new FileWriter(iniFile);
			PrintWriter outP = new PrintWriter(outFile);
			
			outP.println( "flightSpeed=1.1" );
			outP.println( "hoverMultiplier=0.0012410253" );
			outP.println( "hoverBoostMultiplier=0.00555" );
			outP.println( "allowOps=true" );
			outP.println( "flyingEatsFeathers=false" );
			outP.println( "defaultFeatherAmount=500" );
			outP.println( "falldamageWithoutFeather=false" );
			
			outP.close();
			retVal = true;
		} 
    	catch (IOException e) 
    	{
			logOutput( "Error writing to ini file." );
			retVal = false;
			e.printStackTrace();
		}
		
		return retVal;
    }
}
