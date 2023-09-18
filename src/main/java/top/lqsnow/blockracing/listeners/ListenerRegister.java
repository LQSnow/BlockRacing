package top.lqsnow.blockracing.listeners;



public class ListenerRegister {
   public ListenerRegister() {
      ListenerManager.add("[BlockRacing] BasicEventListener", new BasicEventListener());
      ListenerManager.add("[BlockRacing] InventoryEventListener", new InventoryEventListener());
   }
}
