package net.moddedminecraft.xpfix;


import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;

public class Config {

    private final Main plugin;

    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode config;

    public double expPercent;

    public Config(Main main) throws IOException, ObjectMappingException {
        plugin = main;
        loader = HoconConfigurationLoader.builder().setPath(plugin.defaultConf).build();
        config = loader.load();
        configCheck();
    }

    public void configCheck() throws IOException, ObjectMappingException {
        //create config file if it doesn't exist
        if (!Files.exists(plugin.defaultConf)) {
            Files.createFile(plugin.defaultConf);
        }

        //set the percentage from the config file or use default if none is found or it is invalid
        expPercent = check(config.getNode("exp-percent"), 50, "How much experience should the player receive back upon respawn. (As a percentage)").getDouble();

        //save configuration
        loader.save(config);
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }
}
