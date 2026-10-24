package com.geooda1.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Geooda1.MODID)
public class Geooda1 {
    public static final String MODID = "geooda1";
    public static final int MAX_LIVES = 2;
    /** Tiempo (en milisegundos reales) que tienen los demas para revivirte: 1 hora. */
    public static final long DOWNED_MILLIS = 60L * 60L * 1000L;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final RegistryObject<Item> GOLDEN_HEART = ITEMS.register("golden_heart",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));
    public static final RegistryObject<Block> HEART_JAR = BLOCKS.register("heart_jar", HeartJarBlock::new);
    public static final RegistryObject<Item> HEART_JAR_ITEM = ITEMS.register("heart_jar",
            () -> new BlockItem(HEART_JAR.get(), new Item.Properties()));
    public static final RegistryObject<EntityType<FallenBodyEntity>> FALLEN_BODY = ENTITIES.register("fallen_body",
            () -> EntityType.Builder.<FallenBodyEntity>of(FallenBodyEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.4F).clientTrackingRange(10).updateInterval(2).build("fallen_body"));

    public Geooda1() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::attributes);
        bus.addListener((net.minecraftforge.event.BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(GOLDEN_HEART);
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(HEART_JAR_ITEM);
        });
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }

    private void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(ModNet::register);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(FALLEN_BODY.get(), FallenBodyEntity.createAttributes().build());
    }
}
