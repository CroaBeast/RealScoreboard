package josegamerpt.realscoreboard.scoreboard;

import josegamerpt.realscoreboard.RealScoreboard;
import josegamerpt.realscoreboard.config.PlayerData;
import josegamerpt.realscoreboard.config.Config;
import josegamerpt.realscoreboard.scoreboard.fastscoreboard.FastBoard;
import josegamerpt.realscoreboard.utils.Text;
import me.croabeast.iridiumapi.IridiumAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScoreboardTask extends BukkitRunnable {

    private final Player player;
    private final FastBoard fastBoard;
    private final RealScoreboard rs;

    public ScoreboardTask(Player p, RealScoreboard rs) {
        this.player = p;
        this.fastBoard = new FastBoard(p);
        this.rs = rs;
    }

    @Override
    public void run() {
        if (!this.player.isOnline())
        {
            this.cancel();
            return;
        }

        PlayerData playerData = RealScoreboard.getInstance().getDatabaseManager().getPlayerData(this.player.getUniqueId());

        if (Objects.requireNonNull(Config.file().getList("Config.Disabled-Worlds")).
                contains(this.player.getWorld().getName()) || !playerData.isScoreboardON()) {
            if (!this.fastBoard.isDeleted() || this.fastBoard.getLines().size() != 0) {
                this.fastBoard.updateLines();
            }
            return;
        }

        try {
            RScoreboard rsb = this.rs.getScoreboardManager().getScoreboard(this.player);

            List<String> list = rsb.getLines().stream().map(s -> {
                s = s.matches("(?i)%blank%") ?
                        (Text.randomColor() + "§r" + Text.randomColor()) :
                        rs.getPlaceholders().setPlaceHolders(player, s);
                return IridiumAPI.process(player, s);
            }).collect(Collectors.toList());

            String title = rsb.getTitle();
            if (Config.file().getBoolean("Config.Use-Placeholders-In-Scoreboard-Titles")) {
                title = this.rs.getPlaceholders().setPlaceHolders(this.player, title);
            }

            this.fastBoard.updateTitle(IridiumAPI.process(title));
            this.fastBoard.updateLines(list);
        } catch (Exception e) {
            rs.getLogger().log(Level.SEVERE, "[ERROR] RealScoreboard threw an error while trying to display the scoreboard for " + this.player.getName());
            e.printStackTrace();
        }
    }
}
