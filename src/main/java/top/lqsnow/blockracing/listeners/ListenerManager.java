package top.lqsnow.blockracing.listeners;

import org.bukkit.Bukkit;
import top.lqsnow.blockracing.Main;

import java.util.HashMap;
import java.util.logging.Logger;

public class ListenerManager {
   private static final Logger log = Bukkit.getLogger();
   private static HashMap<String, IListener> map;

   public static void enable() {
      log.info("启用监听器管理器...");
      map = new HashMap<>();
      add("[BlockRacing] BasicEventListener", new BasicEventListener());
      add("[BlockRacing] InventoryEventListener", new InventoryEventListener());
   }

   public static void disable() {
      log.info("禁用监听器管理器...");
      map.values().forEach(IListener::unregister);
      map.clear();
   }

   public static void add(String key, IListener listener) {
      if (map.containsKey(key)) {
         remove(key);
      }

      map.put(key, listener);
      log.info("注册监听器 " + key);
      Bukkit.getPluginManager().registerEvents(listener, Main.getInstance());
   }

   public static void remove(String key) {
      log.info("注销监听器 " + key);
      if (map.containsKey(key)) {
         (map.get(key)).unregister();
         map.remove(key);
      }

   }

   public static IListener get(String key) {
      return map.getOrDefault(key, null);
   }
}
