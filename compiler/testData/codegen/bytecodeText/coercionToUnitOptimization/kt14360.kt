inline fun <reified T : Any> uninitializedEntry(): T {
    konst klass = T::class.java
    if (klass.isInterface) {
        throw RuntimeException()
    }
    else {
        return klass.newInstance()
    }
}

class ItemType

object ItemTypes {
    @JvmField
    konst IRON_SHOVEL: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_PICKAXE: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_AXE: ItemType = uninitializedEntry()

    @JvmField
    konst FLINT_AND_STEEL: ItemType = uninitializedEntry()

    @JvmField
    konst APPLE: ItemType = uninitializedEntry()

    @JvmField
    konst ARROW: ItemType = uninitializedEntry()

    @JvmField
    konst COAL: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_INGOT: ItemType = uninitializedEntry()

    @JvmField
    konst GOLD_INGOT: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_SWORD: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_SWORD: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_SHOVEL: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_PICKAXE: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_AXE: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_SWORD: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_SHOVEL: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_PICKAXE: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_AXE: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_SWORD: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_SHOVEL: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_PICKAXE: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_AXE: ItemType = uninitializedEntry()

    @JvmField
    konst STICK: ItemType = uninitializedEntry()

    @JvmField
    konst BOWL: ItemType = uninitializedEntry()

    @JvmField
    konst MUSHROOM_STEW: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_SWORD: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_SHOVEL: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_PICKAXE: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_AXE: ItemType = uninitializedEntry()

    @JvmField
    konst STRING: ItemType = uninitializedEntry()

    @JvmField
    konst GUNPOWDER: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_HOE: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_HOE: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_HOE: ItemType = uninitializedEntry()

    @JvmField
    konst SEEDS: ItemType = uninitializedEntry()

    @JvmField
    konst WHEAT: ItemType = uninitializedEntry()

    @JvmField
    konst BREAD: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER_CAP: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER_TUNIC: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER_PANTS: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER_BOOTS: ItemType = uninitializedEntry()

    @JvmField
    konst CHAIN_HELMET: ItemType = uninitializedEntry()

    @JvmField
    konst CHAIN_CHESTPLATE: ItemType = uninitializedEntry()

    @JvmField
    konst CHAIN_LEGGINGS: ItemType = uninitializedEntry()

    @JvmField
    konst CHAIN_BOOTS: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_HELMET: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_CHESTPLATE: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_LEGGINGS: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_BOOTS: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_HELMET: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_CHESTPLATE: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_LEGGINGS: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_BOOTS: ItemType = uninitializedEntry()

    @JvmField
    konst FLINT: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_PORKCHOP: ItemType = uninitializedEntry()

    @JvmField
    konst COOKED_PORKCHOP: ItemType = uninitializedEntry()

    @JvmField
    konst PAINTING: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_APPLE: ItemType = uninitializedEntry()

    @JvmField
    konst SIGN: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst BUCKET: ItemType = uninitializedEntry()

    @JvmField
    konst MINECART: ItemType = uninitializedEntry()

    @JvmField
    konst SADDLE: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE: ItemType = uninitializedEntry()

    @JvmField
    konst SNOWBALL: ItemType = uninitializedEntry()

    @JvmField
    konst BOAT: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER: ItemType = uninitializedEntry()

    @JvmField
    konst BRICK: ItemType = uninitializedEntry()

    @JvmField
    konst CLAY: ItemType = uninitializedEntry()

    @JvmField
    konst SUGAR_CANE: ItemType = uninitializedEntry()

    @JvmField
    konst PAPER: ItemType = uninitializedEntry()

    @JvmField
    konst BOOK: ItemType = uninitializedEntry()

    @JvmField
    konst SLIMEBALL: ItemType = uninitializedEntry()

    @JvmField
    konst MINECART_WITH_CHEST: ItemType = uninitializedEntry()

    @JvmField
    konst EGG: ItemType = uninitializedEntry()

    @JvmField
    konst COMPASS: ItemType = uninitializedEntry()

    @JvmField
    konst FISHING_ROD: ItemType = uninitializedEntry()

    @JvmField
    konst CLOCK: ItemType = uninitializedEntry()

    @JvmField
    konst GLOWSTONE_DUST: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_FISH: ItemType = uninitializedEntry()

    @JvmField
    konst COOKED_FISH: ItemType = uninitializedEntry()

    @JvmField
    konst DYE: ItemType = uninitializedEntry()

    @JvmField
    konst BONE: ItemType = uninitializedEntry()

    @JvmField
    konst SUGAR: ItemType = uninitializedEntry()

    @JvmField
    konst CAKE: ItemType = uninitializedEntry()

    @JvmField
    konst BED: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE_REPEATER: ItemType = uninitializedEntry()

    @JvmField
    konst COOKIE: ItemType = uninitializedEntry()

    @JvmField
    konst FILLED_MAP: ItemType = uninitializedEntry()

    @JvmField
    konst SHEARS: ItemType = uninitializedEntry()

    @JvmField
    konst MELON: ItemType = uninitializedEntry()

    @JvmField
    konst PUMPKIN_SEEDS: ItemType = uninitializedEntry()

    @JvmField
    konst MELON_SEEDS: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_BEEF: ItemType = uninitializedEntry()

    @JvmField
    konst STEAK: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_CHICKEN: ItemType = uninitializedEntry()

    @JvmField
    konst COOKED_CHICKEN: ItemType = uninitializedEntry()

    @JvmField
    konst ROTTEN_FLESH: ItemType = uninitializedEntry()

    @JvmField
    konst BLAZE_ROD: ItemType = uninitializedEntry()

    @JvmField
    konst GHAST_TEAR: ItemType = uninitializedEntry()

    @JvmField
    konst GOLD_NUGGET: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_WART: ItemType = uninitializedEntry()

    @JvmField
    konst POTION: ItemType = uninitializedEntry()

    @JvmField
    konst GLASS_BOTTLE: ItemType = uninitializedEntry()

    @JvmField
    konst SPIDER_EYE: ItemType = uninitializedEntry()

    @JvmField
    konst FERMENTED_SPIDER_EYE: ItemType = uninitializedEntry()

    @JvmField
    konst BLAZE_POWDER: ItemType = uninitializedEntry()

    @JvmField
    konst MAGMA_CREAM: ItemType = uninitializedEntry()

    @JvmField
    konst BREWING_STAND: ItemType = uninitializedEntry()

    @JvmField
    konst CAULDRON: ItemType = uninitializedEntry()

    @JvmField
    konst GLISTERING_MELON: ItemType = uninitializedEntry()

    @JvmField
    konst SPAWN_EGG: ItemType = uninitializedEntry()

    @JvmField
    konst BOTTLE_O_ENCHANTING: ItemType = uninitializedEntry()

    @JvmField
    konst FIRE_CHARGE: ItemType = uninitializedEntry()

    @JvmField
    konst EMERALD: ItemType = uninitializedEntry()

    @JvmField
    konst ITEM_FRAME: ItemType = uninitializedEntry()

    @JvmField
    konst FLOWER_POT: ItemType = uninitializedEntry()

    @JvmField
    konst CARROT: ItemType = uninitializedEntry()

    @JvmField
    konst POTATO: ItemType = uninitializedEntry()

    @JvmField
    konst BAKED_POTATO: ItemType = uninitializedEntry()

    @JvmField
    konst POISONOUS_POTATO: ItemType = uninitializedEntry()

    @JvmField
    konst EMPTY_MAP: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_CARROT: ItemType = uninitializedEntry()

    @JvmField
    konst MOB_HEAD: ItemType = uninitializedEntry()

    @JvmField
    konst CARROT_ON_A_STICK: ItemType = uninitializedEntry()

    @JvmField
    konst PUMPKIN_PIE: ItemType = uninitializedEntry()

    @JvmField
    konst ENCHANTED_BOOK: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE_COMPARATOR: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_BRICK: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_QUARTZ: ItemType = uninitializedEntry()

    @JvmField
    konst MINECART_WITH_TNT: ItemType = uninitializedEntry()

    @JvmField
    konst MINECART_WITH_HOPPER: ItemType = uninitializedEntry()

    @JvmField
    konst HOPPER: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_RABBIT: ItemType = uninitializedEntry()

    @JvmField
    konst COOKED_RABBIT: ItemType = uninitializedEntry()

    @JvmField
    konst RABBIT_STEW: ItemType = uninitializedEntry()

    @JvmField
    konst RABBIT_FOOT: ItemType = uninitializedEntry()

    @JvmField
    konst RABBIT_HIDE: ItemType = uninitializedEntry()

    @JvmField
    konst LEATHER_HORSE_ARMOR: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_HORSE_ARMOR: ItemType = uninitializedEntry()

    @JvmField
    konst GOLDEN_HORSE_ARMOR: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_HORSE_ARMOR: ItemType = uninitializedEntry()

    @JvmField
    konst LEAD: ItemType = uninitializedEntry()

    @JvmField
    konst NAME_TAG: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_MUTTON: ItemType = uninitializedEntry()

    @JvmField
    konst SPRUCE_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst BIRCH_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst JUNGLE_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst ACACIA_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst DARK_OAK_DOOR: ItemType = uninitializedEntry()

    @JvmField
    konst SPLASH_POTION: ItemType = uninitializedEntry()

    @JvmField
    konst BEETROOT: ItemType = uninitializedEntry()

    @JvmField
    konst BEETROOT_SEEDS: ItemType = uninitializedEntry()

    @JvmField
    konst BEETROOT_SOUP: ItemType = uninitializedEntry()

    @JvmField
    konst RAW_SALMON: ItemType = uninitializedEntry()

    @JvmField
    konst CLOWNFISH: ItemType = uninitializedEntry()

    @JvmField
    konst PUFFERFISH: ItemType = uninitializedEntry()

    @JvmField
    konst COOKED_SALMON: ItemType = uninitializedEntry()

    @JvmField
    konst ENCHANTED_GOLDEN_APPLE: ItemType = uninitializedEntry()

    /////////////////// BLOCKS ///////////////////

    @JvmField
    konst STONE: ItemType = uninitializedEntry()

    @JvmField
    konst GRASS_BLOCK: ItemType = uninitializedEntry()

    @JvmField
    konst DIRT: ItemType = uninitializedEntry()

    @JvmField
    konst COBBLESTONE: ItemType = uninitializedEntry()

    @JvmField
    konst WOOD_PLANKS: ItemType = uninitializedEntry()

    @JvmField
    konst SAPLING: ItemType = uninitializedEntry()

    @JvmField
    konst BEDROCK: ItemType = uninitializedEntry()

    @JvmField
    konst WATER_BUCKET: ItemType = uninitializedEntry()

    @JvmField
    konst LAVA_BUCKET: ItemType = uninitializedEntry()

    @JvmField
    konst SAND: ItemType = uninitializedEntry()

    @JvmField
    konst GRAVEL: ItemType = uninitializedEntry()

    @JvmField
    konst GOLD_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst COAL_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst WOOD: ItemType = uninitializedEntry()

    @JvmField
    konst LEAVES: ItemType = uninitializedEntry()

    @JvmField
    konst SPONGE: ItemType = uninitializedEntry()

    @JvmField
    konst GLASS: ItemType = uninitializedEntry()

    @JvmField
    konst LAPIS_LAZULI_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst LAPIS_LAZULI_BLOCK: ItemType = uninitializedEntry()

    @JvmField
    konst DISPENSER: ItemType = uninitializedEntry()

    @JvmField
    konst SANDSTONE: ItemType = uninitializedEntry()

    @JvmField
    konst NOTE_BLOCK: ItemType = uninitializedEntry()

    @JvmField
    konst DETECTOR_RAIL: ItemType = uninitializedEntry()

    @JvmField
    konst STICKY_PISTON: ItemType = uninitializedEntry()

    @JvmField
    konst COBWEB: ItemType = uninitializedEntry()

    @JvmField
    konst TALL_GRASS: ItemType = uninitializedEntry()

    @JvmField
    konst DEAD_BUSH: ItemType = uninitializedEntry()

    @JvmField
    konst WOOL: ItemType = uninitializedEntry()

    @JvmField
    konst YELLOW_FLOWER: ItemType = uninitializedEntry()

    @JvmField
    konst RED_FLOWER: ItemType = uninitializedEntry()

    @JvmField
    konst BROWN_MUSHROOM: ItemType = uninitializedEntry()

    @JvmField
    konst RED_MUSHROOM: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_GOLD: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_IRON: ItemType = uninitializedEntry()

    @JvmField
    konst DOUBLE_STONE_SLAB: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_SLAB: ItemType = uninitializedEntry()

    @JvmField
    konst BRICKS: ItemType = uninitializedEntry()

    @JvmField
    konst TNT: ItemType = uninitializedEntry()

    @JvmField
    konst BOOKSHELF: ItemType = uninitializedEntry()

    @JvmField
    konst MOSS_STONE: ItemType = uninitializedEntry()

    @JvmField
    konst OBSIDIAN: ItemType = uninitializedEntry()

    @JvmField
    konst TORCH: ItemType = uninitializedEntry()

    @JvmField
    konst FIRE: ItemType = uninitializedEntry()

    @JvmField
    konst MOB_SPAWNER: ItemType = uninitializedEntry()

    @JvmField
    konst OAK_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst CHEST: ItemType = uninitializedEntry()

    @JvmField
    konst DIAMOND_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_DIAMOND: ItemType = uninitializedEntry()

    @JvmField
    konst CRAFTING_TABLE: ItemType = uninitializedEntry()

    @JvmField
    konst FARMLAND: ItemType = uninitializedEntry()

    @JvmField
    konst FURNACE: ItemType = uninitializedEntry()

    @JvmField
    konst LADDER: ItemType = uninitializedEntry()

    @JvmField
    konst RAIL: ItemType = uninitializedEntry()

    @JvmField
    konst COBBLESTONE_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst LEVER: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_PRESSURE_PLATE: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_PRESSURE_PLATE: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst GLOWING_REDSTONE_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE_TORCH: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_BUTTON: ItemType = uninitializedEntry()

    @JvmField
    konst SNOW_LAYER: ItemType = uninitializedEntry()

    @JvmField
    konst ICE: ItemType = uninitializedEntry()

    @JvmField
    konst SNOW: ItemType = uninitializedEntry()

    @JvmField
    konst CACTUS: ItemType = uninitializedEntry()

    @JvmField
    konst FENCE: ItemType = uninitializedEntry()

    @JvmField
    konst PUMPKIN: ItemType = uninitializedEntry()

    @JvmField
    konst NETHERRACK: ItemType = uninitializedEntry()

    @JvmField
    konst SOUL_SAND: ItemType = uninitializedEntry()

    @JvmField
    konst GLOWSTONE: ItemType = uninitializedEntry()

    @JvmField
    konst JACK_O_LANTERN: ItemType = uninitializedEntry()

    @JvmField
    konst TRAPDOOR: ItemType = uninitializedEntry()

    @JvmField
    konst MONSTER_EGG: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_BRICK: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_BARS: ItemType = uninitializedEntry()

    @JvmField
    konst GLASS_PANE: ItemType = uninitializedEntry()

    @JvmField
    konst VINE: ItemType = uninitializedEntry()

    @JvmField
    konst FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst BRICK_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst STONE_BRICK_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst MYCELIUM: ItemType = uninitializedEntry()

    @JvmField
    konst LILY_PAD: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_BRICK_FENCE: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_BRICK_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst ENCHANTMENT_TABLE: ItemType = uninitializedEntry()

    @JvmField
    konst END_PORTAL_FRAME: ItemType = uninitializedEntry()

    @JvmField
    konst END_STONE: ItemType = uninitializedEntry()

    @JvmField
    konst REDSTONE_LAMP: ItemType = uninitializedEntry()

    @JvmField
    konst DROPPER: ItemType = uninitializedEntry()

    @JvmField
    konst ACTIVATOR_RAIL: ItemType = uninitializedEntry()

    @JvmField
    konst COCOA: ItemType = uninitializedEntry()

    @JvmField
    konst SANDSTONE_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst EMERALD_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst TRIPWIRE_HOOK: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_EMERALD: ItemType = uninitializedEntry()

    @JvmField
    konst SPRUCE_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst BIRCH_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst JUNGLE_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst COBBLESTONR_WALL: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_BUTTON: ItemType = uninitializedEntry()

    @JvmField
    konst ANVIL: ItemType = uninitializedEntry()

    @JvmField
    konst TRAPPED_CHEST: ItemType = uninitializedEntry()

    @JvmField
    konst WEIGHTED_PRESSURE_PLATE_LIGHT: ItemType = uninitializedEntry()

    @JvmField
    konst WEIGHTED_PRESSURE_PLATE_HEAVY: ItemType = uninitializedEntry()

    @JvmField
    konst DAYLIGHT_SENSOR: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_REDSTONE: ItemType = uninitializedEntry()

    @JvmField
    konst NETHER_QUARTZ_ORE: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_QUARTZ: ItemType = uninitializedEntry()

    @JvmField
    konst QUARTZ_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst WOODEN_SLAB: ItemType = uninitializedEntry()

    @JvmField
    konst STAINED_CLAY: ItemType = uninitializedEntry()

    @JvmField
    konst ACACIA_LEAVES: ItemType = uninitializedEntry()

    @JvmField
    konst ACACIA_WOOD: ItemType = uninitializedEntry()

    @JvmField
    konst ACACIA_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst DARK_OAK_WOOD_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst SLIME_BLOCK: ItemType = uninitializedEntry()

    @JvmField
    konst IRON_TRAPDOOR: ItemType = uninitializedEntry()

    @JvmField
    konst HEY_BALE: ItemType = uninitializedEntry()

    @JvmField
    konst CARPET: ItemType = uninitializedEntry()

    @JvmField
    konst HARDENED_CLAY: ItemType = uninitializedEntry()

    @JvmField
    konst BLOCK_OF_COAL: ItemType = uninitializedEntry()

    @JvmField
    konst PACKED_ICE: ItemType = uninitializedEntry()

    @JvmField
    konst SUNFLOWER: ItemType = uninitializedEntry()

    @JvmField
    konst INVERTED_DAYLIGHT_SENSOR: ItemType = uninitializedEntry()

    @JvmField
    konst RED_SANDSTONE: ItemType = uninitializedEntry()

    @JvmField
    konst RED_SANDSTONE_STAIRS: ItemType = uninitializedEntry()

    @JvmField
    konst RED_SANDSTONE_SLAB: ItemType = uninitializedEntry()

    @JvmField
    konst SPRUCE_FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst BIRCH_FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst JUNGLE_FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst DARK_OAK_FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst ACACIA_FENCE_GATE: ItemType = uninitializedEntry()

    @JvmField
    konst JUNGLE_GOOR: ItemType = uninitializedEntry()

    @JvmField
    konst GRASS_PATH: ItemType = uninitializedEntry()

    @JvmField
    konst PODZOL: ItemType = uninitializedEntry()
}

// 0 GETSTATIC