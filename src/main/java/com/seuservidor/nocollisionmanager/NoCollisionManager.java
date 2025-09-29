package me.example;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class NoCollisionManager extends JavaPlugin {

    private LuckPerms luckPerms;
    private Team noCollisionTeam;
    private Team collisionTeam;

    @Override
    public void onEnable() {
        // Verificar dependência do LuckPerms
        if (!setupLuckPerms()) {
            getLogger().severe("LuckPerms não encontrado! Plugin desabilitado.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        // Criar teams via comandos de console (garante execução no thread principal)
        createTeams();

        getLogger().info("NoCollisionManager ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        // Limpar teams se existirem
        if (noCollisionTeam != null) {
            noCollisionTeam.unregister();
        }
        if (collisionTeam != null) {
            collisionTeam.unregister();
        }
        getLogger().info("NoCollisionManager desativado.");
    }

    private boolean setupLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }
        luckPerms = LuckPermsProvider.get();
        return luckPerms != null;
    }

    private void createTeams() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) return;

        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();

        // Team sem colisão (NEVER)
        noCollisionTeam = mainScoreboard.getTeam("sem_colisao");
        if (noCollisionTeam == null) {
            noCollisionTeam = mainScoreboard.registerNewTeam("sem_colisao");
        }
        noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        // Team com colisão (ALWAYS)
        collisionTeam = mainScoreboard.getTeam("com_colisao");
        if (collisionTeam == null) {
            collisionTeam = mainScoreboard.registerNewTeam("com_colisao");
        }
        collisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);

        getLogger().info("Teams criados: sem_colisao (NEVER) e com_colisao (ALWAYS).");
    }

    /**
     * Método utilitário para mover um jogador para o team correto baseado na permissão.
     */
    public void updatePlayerCollision(Player player) {
        if (player == null || !player.isOnline()) return;

        // Remover de teams existentes para evitar conflitos
        if (noCollisionTeam.hasEntry(player.getName())) {
            noCollisionTeam.removeEntry(player.getName());
        }
        if (collisionTeam.hasEntry(player.getName())) {
            collisionTeam.removeEntry(player.getName());
        }

        // Verificar permissão
        boolean hasBypass = luckPerms.getUser Manager().getUser (player.getUniqueId())
                .map(user -> user.getCachedData().getPermissionData().checkPermission("nocollision.bypass"))
                .orElse(false);

        // Adicionar ao team apropriado
        if (hasBypass) {
            collisionTeam.addEntry(player.getName());
            getLogger().info("Jogador " + player.getName() + " adicionado a com_colisao (bypass ativado).");
        } else {
            noCollisionTeam.addEntry(player.getName());
            getLogger().info("Jogador " + player.getName() + " adicionado a sem_colisao (sem colisão).");
        }
    }

    // Getters para o listener
    public LuckPerms getLuckPerms() { return luckPerms; }
    public Team getNoCollisionTeam() { return noCollisionTeam; }
    public Team getCollisionTeam() { return collisionTeam; }
}
