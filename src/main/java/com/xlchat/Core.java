package com.xlchat;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;


@Mod(Core.MODID)
public final class Core {
    public static final String MODID = "xlchat";

    public Core() {
        NeoForge.EVENT_BUS.register(ChatFilterMod.class);
    }
}
