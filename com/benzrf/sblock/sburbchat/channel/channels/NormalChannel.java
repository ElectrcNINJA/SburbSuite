package com.benzrf.sblock.sburbchat.channel.channels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.benzrf.sblock.sburbchat.SburbChat;
import com.benzrf.sblock.sburbchat.User;
import com.benzrf.sblock.sburbchat.channel.AccessLevel;
import com.benzrf.sblock.sburbchat.channel.ChannelType;

public class NormalChannel implements Channel, Serializable
{
	public NormalChannel(){}
	public NormalChannel(String name, AccessLevel listeningAccess, AccessLevel sendingAccess, String creator)
	{
		this.name = name;
		this.listeningAccess = listeningAccess;
		this.sendingAccess = sendingAccess;
		this.owner = creator;
		this.modList.add(creator);
		this.approvedList.add(creator);
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getPrefix()
	{
		return ChatColor.WHITE + "[" + ChatColor.GOLD + this.name + ChatColor.WHITE + "] ";
	}
	
	@Override
	public void addAlias(String name, User sender)
	{
		if (sender.getName().equals(this.owner))
		{
			SburbChat.getInstance().getChannelManager().newAlias(name, this, sender);
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to add aliases to channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void removeAlias(String name, User sender)
	{
		if (sender.getName().equals(this.owner))
		{			
			SburbChat.getInstance().getChannelManager().deleteAlias(name, this, sender);
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to remove aliases from channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public boolean userJoin(User sender)
	{
		switch (listeningAccess)
		{
		case PUBLIC:
		{
			if (!banList.contains(sender.getName()) || sender.getName().equals(owner))
			{
				this.listening.add(sender);
				this.sendToAll(ChatColor.YELLOW + sender.getName() + ChatColor.YELLOW + " has joined " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You are banned from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
				return false;
			}
		}
		case PRIVATE:
		{
			if (modList.contains(sender.getName()))
			{
				this.listening.add(sender);
				this.sendToAll(ChatColor.YELLOW + sender.getName() + ChatColor.YELLOW + " has joined " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a " + ChatColor.BOLD + "private" + ChatColor.RESET + " channel!");
				return false;
			}
		}
		case REQUEST:
			if (approvedList.contains(sender.getName()))
			{
				this.listening.add(sender);
				this.sendToAll(ChatColor.YELLOW + sender.getName() + ChatColor.YELLOW + " has joined " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
				return true;
			}
			else if (banList.contains(sender.getName()))
			{
				sender.sendMessage(ChatColor.RED + "You are banned from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
				return false;
			}
			else
			{
				sender.sendMessage(ChatColor.GREEN + "Your request to join " + ChatColor.GOLD + this.name + ChatColor.GREEN + " has been sent.");
				this.sendToAll(ChatColor.YELLOW + sender.getName() + " has requested to join " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
				return false;
			}
		}
		return false;
	}
	
	@Override
	public void userLeave(User sender)
	{
		this.sendToAll(ChatColor.YELLOW + sender.getName() + ChatColor.YELLOW + " has left " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
		this.listening.remove(sender);
	}
	
	@Override
	public void setChat(AsyncPlayerChatEvent event, User sender)
	{
		if (this.muteList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You are muted in channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
			return;
		}
		switch (this.sendingAccess)
		{
		case PUBLIC:
			break;
		case PRIVATE:
			if (this.modList.contains(sender.getName()))
			{
				break;
			}
			else
			{
				return;
			}
		case REQUEST:
			if (approvedList.contains(sender.getName()))
			{
				break;
			}
			else
			{
				return;
			}
		}
		String msg = event.getMessage();
		msg = sender.hasPermission("sburbchat.chatcolor") ? msg.replaceAll("&([0-9a-fk-or])", ChatColor.COLOR_CHAR + "$1") : msg;
		this.sendToAll(this.getChatPrefix(sender, msg) + ((msg.startsWith("\\#") || msg.startsWith("#")) ? msg.substring(1) : msg));
	}
	
	@Override
	public String getChatPrefix(User sender, String message)
	{
		ChatColor color = ChatColor.WHITE;
		if (sender.getName().equals(this.owner))
		{
			color = ChatColor.AQUA;
		}
		else if (this.modList.contains(sender.getName()))
		{
			color = ChatColor.RED;
		}
		else if (sender.hasPermission("sburbchat.gname"))
		{
			color = ChatColor.GREEN;
		}
		return (message.startsWith("#") ? "* " : "<") + color + sender.getName() + ChatColor.WHITE + (message.startsWith("#") ? "" : "> ");
	}
	
	@Override
	public void setNick(String nick, User sender)
	{
		sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + this.name + ChatColor.RED + " is not a " + ChatColor.BOLD + "nick" + ChatColor.RED + " or " + ChatColor.BOLD + "rp" + ChatColor.RED + " channel!");
	}
	
	@Override
	public void removeNick(User sender)
	{
		sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is not a " + ChatColor.BOLD + "nick" + ChatColor.RED + " or " + ChatColor.BOLD + "rp" + ChatColor.RED + " channel!");
	}
	
	@Override
	public void setOwner(String name, User sender)
	{
		this.owner = this.name;
	}
	
	@Override
	public void addMod(User user, User sender)
	{
		if (sender.getName().equals(owner) && !modList.contains(user.getName()))
		{
			this.modList.add(user.getName());
			this.sendToAll(ChatColor.YELLOW + user.getName() + " is now a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.GREEN + "You are now a mod in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		}
		else if (!sender.getName().equals(owner))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to mod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is already a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void addMod(String user)
	{
		this.modList.add(user);
	}
	
	@Override
	public void removeMod(User user, User sender)
	{
		if (sender.getName().equals(this.owner) && this.modList.contains(user.getName()))
		{
			this.modList.remove(user.getName());
			this.sendToAll(ChatColor.YELLOW + user.getName() + " is no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.RED + "You are no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else if (!sender.getName().equals(this.owner))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to demod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void kickUser(User user, User sender)
	{
		if (modList.contains(sender.getName()) && listening.contains(user))
		{
			this.listening.remove(user);
			user.kickFrom(this);
			this.sendToAll(ChatColor.YELLOW + user.getName() + " has been kicked from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
		}
		else if (!modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to kick people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not chatting in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void banUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && !this.banList.contains(user.getName()))
		{
			this.banList.add(user.getName());
			this.listening.remove(user);
			user.banFrom(this);
			this.sendToAll(ChatColor.YELLOW + user.getName() + " has been banned from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
		}
		else if (!this.modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to ban people from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is already banned from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void unbanUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && this.banList.contains(user.getName()))
		{
			this.banList.remove(user.getName());
			user.sendMessage(ChatColor.GREEN + "You have been unbanned from " + ChatColor.GOLD + this.getName() + ChatColor.GREEN + "!");
			this.sendToAll(ChatColor.YELLOW + user.getName() + " has been unbanned from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
		}
		else if (!this.modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to unban people from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not banned from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void muteUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && !this.muteList.contains(user.getName()))
		{
			this.muteList.add(user.getName());
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.GREEN + " has been muted in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
			user.sendMessage(ChatColor.RED + "You have been muted in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else if (!this.modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to mute people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is already muted in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void unmuteUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && this.muteList.contains(user.getName()))
		{
			this.muteList.remove(user.getName());
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.GREEN + " has been unmuted in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		}
		else if (!this.modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to unmute people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not muted in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void approveUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && !this.approvedList.contains(user.getName()))
		{
			this.approvedList.add(user.getName());
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.GREEN + " has been approved in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
			user.sendMessage(ChatColor.GREEN + "You have been approved in channel " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		}
		else if (!this.modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to approve people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is already approved in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void deapproveUser(User user, User sender)
	{
		if (this.modList.contains(sender.getName()) && this.approvedList.contains(user.getName()))
		{
			approvedList.remove(user.getName());
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.GREEN + " has been deapproved in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
			user.sendMessage(ChatColor.RED + "You have been deapproved in channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else if (!modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to deapprove people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not approved in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}
	
	@Override
	public void disband(final User sender)
	{
		if (sender.getName().equals(this.owner) && !this.disband)
		{
			sender.sendMessageFromChannel(ChatColor.AQUA + "Are you sure you wish to continue? If so, please attempt to disband again within 10 seconds.", this);
			this.disband = true;
			SburbChat.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(SburbChat.getInstance(), new Runnable() {
				@Override
				public void run()
				{
					if (NormalChannel.this.disband)//SburbChat.getInstance().getChannelManager().getChannel(NormalChannel.this.name) != null)
					{
						sender.sendMessageFromChannel(ChatColor.AQUA + "Disband aborted.", NormalChannel.this);
						NormalChannel.this.disband = false;
					}
				}
			}, 200);
		}
		else if (!sender.getName().equals(this.owner))
		{
			sender.sendMessageFromChannel(ChatColor.AQUA + "Please do not attempt to disband channels that you are not the owner of.", this);
			this.listening.remove(sender);
			sender.kickFrom(this);
		}
		else
		{
			this.sendToAll(ChatColor.AQUA + "Channel " + ChatColor.GOLD + this.name + ChatColor.AQUA + " is being disbanded!");
			for (User u : this.listening)
			{
				u.kickFrom(this);
			}
			SburbChat.getInstance().getChannelManager().disbandChannel(this.name);
			this.disband = false;
			sender.sendMessage(ChatColor.AQUA + "Channel " + ChatColor.GOLD + this.name + ChatColor.AQUA + " has been disbanded.");
		}
	}
	
	@Override
	public AccessLevel getSAcess()
	{
		return this.sendingAccess;
	}
	
	@Override
	public AccessLevel getLAcess()
	{
		return this.listeningAccess;
	}
	
	@Override
	public Set<User> getUsers()
	{
		return this.listening;
	}
	
	protected void sendToAll(String s)
	{
		for (User u : this.listening)
		{
			u.sendMessageFromChannel(s, this);
		}
		Logger.getLogger("Minecraft").info(ChatColor.stripColor(this.getPrefix() + s));
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.listening = new HashSet<User>();
		SburbChat.getInstance().getChannelManager().registerChannel(this);
	}
	
	@Override
	public ChannelType getType()
	{
		return ChannelType.NORMAL;
	}
	
	protected String name;
	protected AccessLevel listeningAccess;
	protected AccessLevel sendingAccess;
	protected String owner;
	protected transient boolean disband = false;
	
	protected transient Set<User> listening = new HashSet<User>();
	protected List<String> modList = new ArrayList<String>();
	protected List<String> approvedList = new ArrayList<String>();
	protected List<String> banList = new ArrayList<String>();
	protected List<String> muteList = new ArrayList<String>();
	
	private static final long serialVersionUID = 7159274535690404352L;
}
