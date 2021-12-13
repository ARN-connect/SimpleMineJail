package rubrub07.SimpleMineJail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SimpleMineJail extends JavaPlugin implements TabExecutor {

	PluginDescriptionFile pdffile = getDescription();
	List<String> pricelist = new ArrayList<>();
	public String rutaconf;
	public String version = pdffile.getVersion();
	public String name = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("config.prefix"));
	public FileConfiguration conf;

	public void onEnable() {
		registrarConfig();
		conf = this.getConfig();
		Bukkit.getConsoleSender().sendMessage(name + ChatColor.BLUE + " Plugin encendido (version: " + version + " )");
		pricelist = loadprices();
		registrarCommandos();
	}

	public void onDisable() {
		Bukkit.getConsoleSender()
				.sendMessage(name + ChatColor.BLUE + " Plugin desactivado (version: " + version + " )");
	}

	public void registrarCommandos() {
		this.getCommand("smj").setExecutor(this);
		this.getCommand("smj").setTabCompleter(this);
	}

	public void registrarConfig() {
		File config = new File(this.getDataFolder(), "config.yml");
		rutaconf = config.getPath();
		if (!config.exists()) {
			this.getConfig().options().copyDefaults(true);
			saveConfig();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		try {
			if (args[0].equalsIgnoreCase("jail")) {
				int uwu = 0;
				for (Player all : Bukkit.getServer().getOnlinePlayers()) {
					if (args[1].equals(all.getPlayer().getName().toString())) {
						if (!all.getPlayer().isOp()) {
							Inventory inv = all.getInventory();
							try {
								if (conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed") != "true") {
									conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.inv", itemStackArrayToBase64(inv.getContents()));
									conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.arm", itemStackArrayToBase64(all.getInventory().getArmorContents()));
									conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed", "true");
									saveConfig();
									all.getInventory().clear();
									org.bukkit.World w = Bukkit.getWorld(conf.getString("config.world.world"));
									Location loc = new Location(w, conf.getDouble("config.world.x"),
									conf.getDouble("config.world.y"), conf.getDouble("config.world.z"));
									all.teleport(loc);
									all.sendMessage(prefix("&7Has sido encarcelado debes trabajar para pagar tu salida"));

									if (sender instanceof Player) {
										Player sendex = (Player) sender;
										sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " ah sido encarcelado"));
									} else {
										ConsoleCommandSender sendex = Bukkit.getConsoleSender();
										sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " ah sido encarcelado"));
									}
									
									for (String s : pricelist) {
										all.sendMessage(prefix("&7" + s));
									}
									return true;
								}
							} catch (Exception e) {
								if (sender instanceof Player) {
									Player sendex = (Player) sender;
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " ya esta encarcelado"));
									return true;
								} else {
									ConsoleCommandSender sendex = Bukkit.getConsoleSender();
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " ya esta encarcelado"));
									return true;
								}
							}
						} else {
							if (sender instanceof Player) {
								Player sendex = (Player) sender;
								sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no se puede encarcelar"));
								return true;
							} else {
								ConsoleCommandSender sendex = Bukkit.getConsoleSender();
								sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no se puede encarcelar"));
								return true;
							}
						}
					} else {
						uwu++;
					}
					break;
				}
				if (uwu == Bukkit.getServer().getOnlinePlayers().size()) {
					if (sender instanceof Player) {
						Player sendex = (Player) sender;
						sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
						return true;
					} else {
						ConsoleCommandSender sendex = Bukkit.getConsoleSender();
						sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
						return true;
					}
				}
			}

			else if (args[0].equalsIgnoreCase("setjail")) {
				if (sender instanceof Player) {
					Player sendex = (Player) sender;
					if (sendex.hasPermission("smj.setjail") || sendex.hasPermission("*") || sendex.isOp()) {
						conf.set("config.world.world", sendex.getWorld().getName());
						conf.set("config.world.x", sendex.getLocation().getX());
						conf.set("config.world.y", sendex.getLocation().getY());
						conf.set("config.world.z", sendex.getLocation().getZ());
						saveConfig();
						sendex.sendMessage(prefix("&7Has cambiado la posicion de la carcel"));
						return true;
					} else {
						sendex.sendMessage(prefix("&7No tienes permisos para ejecutar este comando"));
						return true;
					}
				} else {
					ConsoleCommandSender sendex = Bukkit.getConsoleSender();
					sendex.sendMessage(prefix("&7Solo se puede ejecutar el comando como staff"));
					return true;
				}
			}

			else if (args[0].equalsIgnoreCase("unjail")) {
				if (sender instanceof Player) {
					Player sendex = (Player) sender;
					if (sendex.hasPermission("smj.unjail") || sendex.hasPermission("*") || sendex.isOp()) {
						int uwu = 0;
						for (Player all : Bukkit.getServer().getOnlinePlayers()) {
							if (args[1].equals(all.getPlayer().getName().toString())) {
								if (conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed").equalsIgnoreCase("true")) {
									conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed", "false");
									String bas64 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.inv");
									String bas642 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.arm");
									ItemStack[] i = itemStackArrayFromBase64(bas64);
									all.getInventory().setContents(i);
									ItemStack[] e = itemStackArrayFromBase64(bas642);
									all.getInventory().setArmorContents(e);
									all.sendMessage(prefix("&7Has sido liberado"));
									saveConfig();
									break;
								} else {
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no esta encarcelado"));
									break;
								}
							} else {
								uwu++;
							}
						}
						if (uwu == Bukkit.getServer().getOnlinePlayers().size()) {
							sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
						}

					}
				} else {
					ConsoleCommandSender sendex = Bukkit.getConsoleSender();
						int uwu = 0;
						for (Player all : Bukkit.getServer().getOnlinePlayers()) {
							if (args[1].equals(all.getPlayer().getName().toString())) {
								if (conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed").equalsIgnoreCase("true")) {
									String bas64 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.inv");
									String bas642 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.arm");
									ItemStack[] i = itemStackArrayFromBase64(bas64);
									all.getInventory().setContents(i);
									ItemStack[] e = itemStackArrayFromBase64(bas642);
									all.getInventory().setArmorContents(e);
									all.sendMessage(prefix("&7Has sido liberado"));
									conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed", "false");
									saveConfig();
									break;
								} else {
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no esta encarcelado"));
									break;
								}
							} else {
								uwu++;
							}
						}
						if (uwu == Bukkit.getServer().getOnlinePlayers().size()) {
							sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
						}

					
				}
			}

			else if (args[0].equalsIgnoreCase("claim")){
				if (sender instanceof Player) {
					Player sendex = (Player) sender;
					if (sendex.hasPermission("smj.claim") || sendex.hasPermission("*") || sendex.isOp()) {
						int uwu = 0;
						for (Player all : Bukkit.getServer().getOnlinePlayers()) {
							if (args[1].equals(all.getPlayer().getName().toString())) {
								if (conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed").equalsIgnoreCase("true")) {
									//contar objetos
									boolean success = true;
									int diamond_ore = getamount(all.getInventory(), Material.DIAMOND_ORE);
									int gold_ore = getamount(all.getInventory(), Material.GOLD_ORE);
									int iron_ore = getamount(all.getInventory(), Material.IRON_ORE);
									int coal_ore = getamount(all.getInventory(), Material.COAL_ORE);
									int redstone_ore = getamount(all.getInventory(), Material.REDSTONE_ORE);
									int lapis_ore = getamount(all.getInventory(), Material.LAPIS_ORE);
									int emerald_ore = getamount(all.getInventory(), Material.EMERALD_ORE);
									int quartz_ore = getamount(all.getInventory(), Material.NETHER_QUARTZ_ORE);
									int nether_gold_ore = getamount(all.getInventory(), Material.NETHER_GOLD_ORE);
									
									if(conf.getInt("config.price.diamond_ore") != diamond_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de diamantes necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.gold_ore") != gold_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de oro necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.iron_ore") != iron_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de hierro necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.coal_ore") != coal_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de carbones necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.redstone_ore") != redstone_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de redstone necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.lapis_ore") != lapis_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de lapis necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.emerald_ore") != emerald_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de emeralds necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.quartz_ore") != quartz_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de quartz necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.nether_gold_ore") != nether_gold_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de nether gold necesarios"));
										success = false;
									}

									if(success != false){
										all.sendMessage(prefix("&7Perfecto tienes todos los minerales requeridos puedes salir"));
										conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed", "false");
										saveConfig();
										String bas64 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.inv");
										String bas642 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.arm");
										ItemStack[] i = itemStackArrayFromBase64(bas64);
										all.getInventory().setContents(i);
										ItemStack[] e = itemStackArrayFromBase64(bas642);
										all.getInventory().setArmorContents(e);
										all.sendMessage(prefix("&7Has sido liberado"));
										return true;
									}

								} else {
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no esta encarcelado"));
									return true;
								}
							} else {
								uwu++;
							}
						}
						if (uwu == Bukkit.getServer().getOnlinePlayers().size()) {
							sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
							return true;
						}
					}
				} else {
					ConsoleCommandSender sendex = Bukkit.getConsoleSender();
						int uwu = 0;
						for (Player all : Bukkit.getServer().getOnlinePlayers()) {
							if (args[1].equals(all.getPlayer().getName().toString())) {
								if (conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed").equalsIgnoreCase("true")) {
									//contar objetos
									boolean success = true;
									int diamond_ore = getamount(all.getInventory(), Material.DIAMOND_ORE);
									int gold_ore = getamount(all.getInventory(), Material.GOLD_ORE);
									int iron_ore = getamount(all.getInventory(), Material.IRON_ORE);
									int coal_ore = getamount(all.getInventory(), Material.COAL_ORE);
									int redstone_ore = getamount(all.getInventory(), Material.REDSTONE_ORE);
									int lapis_ore = getamount(all.getInventory(), Material.LAPIS_ORE);
									int emerald_ore = getamount(all.getInventory(), Material.EMERALD_ORE);
									int quartz_ore = getamount(all.getInventory(), Material.NETHER_QUARTZ_ORE);
									int nether_gold_ore = getamount(all.getInventory(), Material.NETHER_GOLD_ORE);
									
									if(conf.getInt("config.price.diamond_ore") != diamond_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de diamantes necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.gold_ore") != gold_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de oro necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.iron_ore") != iron_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de hierro necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.coal_ore") != coal_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de carbones necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.redstone_ore") != redstone_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de redstone necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.lapis_ore") != lapis_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de lapis necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.emerald_ore") != emerald_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de emeralds necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.quartz_ore") != quartz_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de quartz necesarios"));
										success = false;
									}
									if(conf.getInt("config.price.nether_gold_ore") != nether_gold_ore){
										all.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no tiene el numero de nether gold necesarios"));
										success = false;
									}

									if(success != false){
										all.sendMessage(prefix("&7Perfecto tienes todos los minerales requeridos puedes salir"));
										conf.set("config.data.playerdata." + all.getPlayer().getName().toString() + "isjailed", "false");
										saveConfig();
										String bas64 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.inv");
										String bas642 = conf.getString("config.data.playerdata." + all.getPlayer().getName().toString() + ".inventory.arm");
										ItemStack[] i = itemStackArrayFromBase64(bas64);
										all.getInventory().setContents(i);
										ItemStack[] e = itemStackArrayFromBase64(bas642);
										all.getInventory().setArmorContents(e);
										all.sendMessage(prefix("&7Has sido liberado"));
										return true;
									}

								} else {
									sendex.sendMessage(prefix("&7El jugador " + all.getPlayer().getName().toString() + " no esta encarcelado"));
									return true;
								}
							} else {
								uwu++;
							}
						}
						if (uwu == Bukkit.getServer().getOnlinePlayers().size()) {
							sendex.sendMessage(prefix("&7El jugador " + args[1] + " no existe"));
							return true;
						}
					}
				}
			
		} catch (Exception e) {

			if (sender instanceof Player) {
				Player sendex = (Player) sender;
				sendex.sendMessage(prefix("&7<Ayuda de SMJ>"));
				sendex.sendMessage(prefix("&7/smj setjail || establece las cordenadas y el mundo de la jail minera"));
				sendex.sendMessage(prefix("&7/smj jail <player> || mete a la carcel minera a el jugador"));
				sendex.sendMessage(prefix("&7/smj unjail <player> || libera sin reclamar precio"));
				sendex.sendMessage(prefix("&7/smj claim <player> || reclama del inventario el precio de salida y lo libera"));
				return true;
			} else {
				ConsoleCommandSender sendex = Bukkit.getConsoleSender();
				sendex.sendMessage(prefix("&7<Ayuda de SMJ>"));
				sendex.sendMessage(prefix("&7/smj setjail || establece las cordenadas y el mundo de la jail minera"));
				sendex.sendMessage(prefix("&7/smj jail <player> || mete a la carcel minera a el jugador"));
				sendex.sendMessage(prefix("&7/smj unjail <player> || libera sin reclamar precio"));
				sendex.sendMessage(prefix("&7/smj claim <player> || reclama del inventario el precio de salida y lo libera"));
				return true;
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			List<String> list = new ArrayList<>();
			list.add("jail");
			list.add("setjail");
			list.add("unjail");
			list.add("claim");
			return list;
		}

		if (args.length == 2) {
			List<String> list = new ArrayList<>();
			for (Player all : Bukkit.getServer().getOnlinePlayers()) {
				list.add(all.getName());
			}

			if (list != null)
				Collections.sort(list);
			return list;
		}

		List<String> clean = new ArrayList<>();
		return clean;
	}

	public String prefix(String s) {
		//String b = ChatColor.translateAlternateColorCodes('&', conf.get("config.prefix").toString() + s);
		String b = ChatColor.translateAlternateColorCodes('&', name + s);
		return b;
	}

	public List<String> loadprices() {

		List<String> list = new ArrayList<>();

		if (conf.getString("config.price.diamond_ore") != null || conf.getInt("config.price.diamond_ore") != 0) {
			list.add("Diamond Ore : " + conf.getString("config.price.diamond_ore"));
		}
		if (conf.getString("config.price.emerald_ore") != null || conf.getInt("config.price.emerald_ore") != 0) {
			list.add("Emerald Ore : " + conf.getString("config.price.emerald_ore"));
		}
		if (conf.getString("config.price.lapis_ore") != null || conf.getInt("config.price.lapis_ore") != 0) {
			list.add("Lapis Ore : " + conf.getString("config.price.lapis_ore"));
		}
		if (conf.getString("config.price.redstone_ore") != null || conf.getInt("config.price.redstone_ore") != 0) {
			list.add("Redstone Ore : " + conf.getString("config.price.redstone_ore"));
		}
		if (conf.getString("config.price.quartz_ore") != null || conf.getInt("config.price.quartz_ore") != 0) {
			list.add("Quartz Ore : " + conf.getString("config.price.quartz_ore"));
		}
		if (conf.getString("config.price.coal_ore") != null || conf.getInt("config.price.coal_ore") != 0) {
			list.add("Coal Ore : " + conf.getString("config.price.coal_ore"));
		}
		if (conf.getString("config.price.gold_ore") != null || conf.getInt("config.price.gold_ore") != 0) {
			list.add("Gold Ore : " + conf.getString("config.price.gold_ore"));
		}
		if (conf.getString("config.price.iron_ore") != null || conf.getInt("config.price.iron_ore") != 0) {
			list.add("Iron Ore : " + conf.getString("config.price.iron_ore"));
		}
		if (conf.getString("config.price.nether_gold_ore") != null || conf.getInt("config.price.nether_gold_ore") != 0) {
			list.add("Nether Gold Ore : " + conf.getString("config.price.nether_gold_ore"));
		}

		return list;
	}


	public int getamount(Inventory inv, Material mat)
	{
		int amount = 0;
		for (ItemStack item : inv.getContents())
		{
			if (item != null && item.getItemMeta() != null && item.getType() == mat)
			{
				amount += item.getAmount();
			}
		}
		return amount;
	}
	

    /**
     * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
     * 
     * @param playerInventory to turn into an array of strings.
     * @return Array of strings: [ main content, armor content ]
     * @throws IllegalStateException
     */
    public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
    	//get the main content part, this doesn't return the armor
    	String content = toBase64(playerInventory);
    	String armor = itemStackArrayToBase64(playerInventory.getArmorContents());
    	
    	return new String[] { content, armor };
    }
    
    /**
     * 
     * A method to serialize an {@link ItemStack} array to Base64 String.
     * 
     * <p />
     * 
     * Based off of {@link #toBase64(Inventory)}.
     * 
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException
     */
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
    	try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // Write the size of the inventory
            dataOutput.writeInt(items.length);
            
            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    
    /**
     * A method to serialize an inventory to Base64 string.
     * 
     * <p />
     * 
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     * 
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     * 
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException
     */
    public static String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());
            
            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    
    /**
     * 
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     * 
     * <p />
     * 
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     * 
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     * 
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     * @throws IOException
     */
    public static Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
    
            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            
            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
    
    /**
     * Gets an array of ItemStacks from Base64 string.
     * 
     * <p />
     * 
     * Base off of {@link #fromBase64(String)}.
     * 
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
    	try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
            	items[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
