package com.seuservidor.nocollisionmanager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoCollisionManager extends JavaPlugin {

    private LuckPerms luckPerms;
    private static final String BYPASS_PERMISSION = "nocollision.bypass";

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms não foi encontrado! Desativando o plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        this.luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onLuckPermsGroupChange);
        setupTeams();
        getLogger().info("NoCollisionManager ativado com sucesso no modo 'Bypass'!");
    }

    private void setupTeams() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team add sem_colisao \"Sem Colisão\"");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify sem_colisao collisionRule never");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team add com_colisao \"Com Colisão\"");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify com_colisao collisionRule always");
    }

    private void onLuckPermsGroupChange(UserDataRecalculateEvent event) {
        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(this, () -> updatePlayerTeam(player), 1L);
        }
    }

    public void updatePlayerTeam(Player player) {
        if (player == null) return;
        User user = this.luckPerms.getPlayerAdapter(Player.class).getUser(player);
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        if (permissionData.checkPermission(BYPASS_PERMISSION).asBoolean()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team join com_colisao " + player.getName());
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team join sem_colisao " + player.getName());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("NoCollisionManager desativado.");
    }
}
