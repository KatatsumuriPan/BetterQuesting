package betterquesting.quests.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper NBT saving/loading
 */
public class TaskRegistry
{
	static HashMap<String, Class<? extends TaskBase>> taskRegistry = new HashMap<String, Class<? extends TaskBase>>();
	
	public static void RegisterTask(Class<? extends TaskBase> task, String idName)
	{
		try
		{
			ModContainer mod = Loader.instance().activeModContainer();
			
			if(idName.contains(":"))
			{
				throw new IllegalArgumentException("Illegal character(s) used in task ID name");
			}
			
			if(task == null)
			{
				throw new NullPointerException("Tried to register null task");
			} else if(mod == null)
			{
				throw new IllegalArgumentException("Tried to register a task without an active mod instance");
			}
			
			try
			{
				task.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Task is missing a default constructor with 0 arguemnts");
			}
			
			String fullName = mod.getModId() + ":" + idName;
			
			if(taskRegistry.containsKey(fullName) || taskRegistry.containsValue(task))
			{
				throw new IllegalStateException("Cannot register dupliate task type '" + fullName + "'");
			}
			
			taskRegistry.put(fullName, task);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register task", e);
		}
	}
	
	public static String GetID(Class<? extends TaskBase> task)
	{
		for(String idName : taskRegistry.keySet())
		{
			if(taskRegistry.get(idName) == task)
			{
				return idName;
			}
		}
		
		return null;
	}
	
	public static Class<? extends TaskBase> GetTask(String idName)
	{
		return taskRegistry.get(idName);
	}
	
	public static ArrayList<String> GetTypeList()
	{
		return new ArrayList<String>(taskRegistry.keySet());
	}
	
	public static TaskBase InstatiateTask(String idName)
	{
		try
		{
			Class<? extends TaskBase> task = taskRegistry.get(idName);
			
			if(task == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + idName + "'! Are you missing a task pack?");
				return null;
			}
			
			return taskRegistry.get(idName).newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate quest: " + idName, e);
			return null;
		}
	}
}
