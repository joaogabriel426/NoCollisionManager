package me.example;

import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEventListener implements Listener {

    private final NoCollisionManager plugin;

    public PlayerEventListener(NoCollisionManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Agendar atualização assíncrona para garantir que o LuckPerms carregue os dados do usuário
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.updatePlayerCollision(event.getPlayer());
        }, 1L); // Delay de 1 tick para estabilidade
    }

    @EventHandler
    public void onPermissionChange(UserDataRecalculateEvent event) {
        // Verificar se o usuário é um jogador online
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = plugin.getServer().getPlayer(event.getUser ().getUniqueId());
            if (player != null && player.isOnline()) {
                plugin.updatePlayerCollision(player);
            }
        });
    }
}
