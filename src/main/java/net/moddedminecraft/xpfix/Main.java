package net.moddedminecraft.xpfix;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "xpfix",
        name = "XPFix",
        version = "1.0"
)
public class Main {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    private Config config;

    //Where experience is stored after a player has died.
    private Map<String, Integer> experienceStorage;


    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        //initialize the config file
        this.config = new Config(this);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        //initialize the experience storage map
        experienceStorage = new HashMap<String, Integer>();
        //yay, we loaded fine!
        logger.info("XPFix loaded!");
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        //reinitialize the config file
        this.config = new Config(this);
    }

    //Events
    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
        //if a player dies, this will run.
        if (event.getTargetEntity() instanceof Player) {

            //get player from the event
            Player player = ((Player) event.getTargetEntity()).getPlayer().get();

            //get the experience currently stored by the player
            int totalExp = player.get(Keys.TOTAL_EXPERIENCE).get();

            //put that experience into the map
            experienceStorage.put(player.getName(), totalExp);

            //remove experience from the player before
            takeExperience(player);
        }
    }

    @Listener
    public void onPlayerRespawn(RespawnPlayerEvent event, @First Player player) {
        //get players name
        String playerName = player.getName();
        //if the experience storage map contains the player name, continue
        if (experienceStorage.containsKey(playerName)) {
            //give back the experience that is stored
            giveExperience(player, experienceStorage.get(playerName));

            //remove player from the storage map to prevent duplicated experience
            experienceStorage.remove(playerName);
        }
    }

    private void takeExperience(DataHolder target) {
        if (target.supports(Keys.TOTAL_EXPERIENCE)) {
            //Set experience to 0 after storing it
            target.offer(Keys.TOTAL_EXPERIENCE, 0);
        }
    }

    private void giveExperience(DataHolder target, int exp) {
        if (target.supports(Keys.TOTAL_EXPERIENCE)) {
            //configuration for percentage converted to a workable state
            Double expPercent = config.expPercent / 100;

            //get the experience after applying the percentage
            Double dividedExp = exp * expPercent;

            //give the experience back to the player
            target.offer(Keys.TOTAL_EXPERIENCE, dividedExp.intValue());
        }
    }


}
