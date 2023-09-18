package top.lqsnow.blockracing.listeners;

import org.bukkit.event.Listener;

public interface IListener extends Listener {
    void register();
    void unregister();
}
