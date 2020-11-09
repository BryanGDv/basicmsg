package code.commands;

import code.CacheManager;
import code.modules.player.PlayerStatic;
import code.registry.ConfigManager;
import code.Manager;
import code.modules.player.PlayerMessage;
import code.bukkitutils.SoundManager;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import code.utils.Configuration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReplyCommand implements CommandClass {

    private final Manager manager;
    private final CacheManager cache;

    public ReplyCommand(Manager manager, CacheManager cache){
        this.manager = manager;
        this.cache = cache;
    }
    @Command(names = {"reply", "r"})
    public boolean onCommand(CommandSender sender, @OptArg @Text String message) {

        ConfigManager files = manager.getFiles();
        PlayerMessage playersender = manager.getPlayerMethods().getSender();

        SoundManager sound = manager.getSounds();

        Configuration players = files.getPlayers();
        Configuration config = files.getCommand();
        Configuration command = files.getCommand();
        Configuration lang = files.getMessages();

        if(!(sender instanceof Player)) {
            System.out.println(playersender.getMessage(lang.getString("error.console")));
            return true;
        }

        Player player = (Player) sender;
        UUID playeruuid = player.getUniqueId();

        if (!(manager.getPathManager().isCommandEnabled("reply"))){
            playersender.sendMessage(sender, lang.getString("error.command-disabled")
                    .replace("%player%", player.getName())
                    .replace("%command%", "reply"));
            return true;
        }

        if (message.trim().isEmpty()) {
            playersender.sendMessage(sender, lang.getString("error.no-arg"));
            playersender.sendMessage(sender, "&8- &fUsage: &a/reply [message]");
            sound.setSound(player.getUniqueId(), "sounds.error");

            return true;
        }


        Map<UUID, UUID> reply = cache.getReply();

        if (cache.getReply().containsKey(player.getUniqueId())) {

            OfflinePlayer target = Bukkit.getPlayer(reply.get(player.getUniqueId()));

            if (message.equalsIgnoreCase("-player")){
                playersender.sendMessage(player, command.getString("commands.msg-reply.talked")
                .replace("%player%", target.getName()));
                sound.setSound(player.getUniqueId(), "sounds.on-reply-player");
                return true;
            }

            CommandSender targetsender = target.getPlayer();

            if (sender.hasPermission(config.getString("config.perms.msg-color"))){
                message = PlayerStatic.setColor(message);
            }

            playersender.sendMessage(player, PlayerStatic.setColor(command.getString ("commands.msg-reply.player")
                    .replace("%player%", sender.getName())
                    .replace("%arg-1%", target.getName()))
                    , message, true);
            sound.setSound(player.getUniqueId(), "sounds.on-reply");

            List<String> ignoredlist = players.getStringList("players." + playeruuid + ".players-ignored");

            if (!(ignoredlist.contains(target.getName()))) {
                playersender.sendMessage(targetsender, PlayerStatic.setColor(command.getString("commands.msg-reply.player")
                                .replace("%player%", sender.getName())
                                .replace("%arg-1%", target.getName()))
                        , message, true);
                reply.replace(reply.get(player.getUniqueId()), player.getUniqueId());
                sound.setSound(player.getUniqueId(), "sounds.on-reply");
            }

        } else {
            playersender.sendMessage(sender, lang.getString("error.no-reply"));
            sound.setSound(player.getUniqueId(), "sounds.error");

        }
        return true;
    }
}


