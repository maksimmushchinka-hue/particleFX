package dev.particlefx.core;

import dev.particlefx.fx.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** Registry of all 50 animations with metadata. */
public final class AnimationRegistry {
    public interface Factory { Fx create(Location origin, EffectSettings s, Emitter out); }

    public record FxInfo(String id, String name, String desc, String particles, Material icon, Factory factory) {}

    private final Map<String, FxInfo> byId = new LinkedHashMap<>();
    private final List<FxInfo> order = new ArrayList<>();

    private void add(String id, String name, String desc, String particles, Material icon, Factory f) {
        FxInfo info = new FxInfo(id, name, desc, particles, icon, f);
        byId.put(id, info);
        order.add(info);
    }

    public AnimationRegistry() {
        add("supernova", "Сверхновая", "Сжатие ядра, расширяющиеся оболочки, резкий выброс лучей", "FLASH, FLAME, DUST, CRIT", Material.NETHER_STAR, Supernova::new);
        add("galactic_vortex", "Галактический вихрь", "Четыре закрученных рукава внезапно раскрываются", "DUST, FLASH", Material.END_CRYSTAL, GalacticVortex::new);
        add("black_hole", "Чёрная дыра", "Спиральное падение к центру и резкий выброс наружу", "PORTAL, REVERSE_PORTAL, CRIT, DUST", Material.OBSIDIAN, BlackHole::new);
        add("galaxy_collision", "Столкновение галактик", "Два облака сближаются и взрываются дугами", "DUST, FLASH, EXPLOSION_EMITTER", Material.BLUE_ICE, GalaxyCollision::new);
        add("nebula", "Космическая туманность", "Объёмные цветные облака с последовательными вспышками", "DUST, FLASH, GLOW", Material.AMETHYST_SHARD, Nebula::new);
        add("saturn_rings", "Кольца Сатурна", "Планета и кольца растут, затем резко меняют наклон", "DUST, FLASH", Material.LIGHT_BLUE_DYE, SaturnRings::new);
        add("meteor_shower", "Метеоритный дождь", "Потоки метеоров с хвостами и финальное падение", "FLAME, DUST, LAVA, EXPLOSION", Material.BLAZE_ROD, MeteorShower::new);
        add("comet", "Комета", "Яркая комета с вращающимся хвостом, который раскрывается", "END_ROD, DUST, FLASH", Material.SNOWBALL, Comet::new);
        add("planetary_system", "Планетарная система", "Планеты на наклонных орбитах с финальным импульсом", "DUST, GLOW", Material.SLIME_BALL, PlanetarySystem::new);
        add("atom", "Атом", "Ядро и три электрона на наклонных орбитах со светящимся следом", "DUST, GLOW, DUST_PLUME", Material.PRISMARINE_SHARD, Atom::new);

        add("lightning", "Молния", "Зубчатые вертикальные разряды с резкой вспышкой удара", "ELECTRIC_SPARK, DUST, FLASH, EXPLOSION", Material.LIGHTNING_ROD, WeatherFx.Lightning::new);
        add("storm_front", "Грозовой фронт", "Молнии по очереди, затем одновременный залп", "ELECTRIC_SPARK, SMOKE, FLASH", Material.TRIDENT, WeatherFx.StormFront::new);
        add("fire_tornado", "Огненный торнадо", "Спираль огня с внезапным расширением", "FLAME, SMALL_FLAME, DUST", Material.CAMPFIRE, WeatherFx.FireTornado::new);
        add("ice_tornado", "Ледяной торнадо", "Снежная спираль и резкий разлёт осколков", "SNOWFLAKE, DUST, GLOW", Material.BLUE_ICE, WeatherFx.IceTornado::new);
        add("waterfall", "Водяной водопад", "Дуга падающей воды превращается в расходящиеся волны", "SPLASH, NAUTILUS, DOLPHIN, DUST", Material.WATER_BUCKET, WeatherFx.Waterfall::new);
        add("tsunami", "Цунами", "Растущая волна резко прокатывается наружу", "NAUTILUS, DUST, SPLASH", Material.FISHING_ROD, WeatherFx.Tsunami::new);
        add("volcano", "Вулкан", "Столб поднимается, затем масштабное извержение", "FLAME, LAVA, LARGE_SMOKE, DUST", Material.MAGMA_BLOCK, WeatherFx.Volcano::new);
        add("earthquake", "Землетрясение", "Концентрические кольца и расходящиеся разломы", "DUST, ASH, CRIT, FLASH", Material.COBBLESTONE, WeatherFx.Earthquake::new);
        add("sandstorm", "Песчаная буря", "Вращающийся песчаный столб с расширением у основания", "DUST, ASH, DUST_PLUME", Material.SAND, WeatherFx.Sandstorm::new);
        add("vortex", "Водоворот", "Слои вращаются и резко втягиваются к центру", "CLOUD, SMOKE, POOF", Material.COD_BUCKET, WeatherFx.Vortex::new);

        add("stone_golem", "Каменный голем", "Силуэт собирается снизу вверх и рассыпается", "DUST, ASH, POOF", Material.IRON_BLOCK, CreatureFx.StoneGolem::new);
        add("dragon", "Дракон", "Силуэт пролетает по дуге и выпускает огненную волну", "DUST, FLAME, EXPLOSION_EMITTER", Material.DRAGON_EGG, CreatureFx.Dragon::new);
        add("phoenix", "Феникс", "Крылья раскрываются, птица возрождается из вспышки", "FLAME, DUST, TOTEM_OF_UNDYING, END_ROD", Material.BLAZE_POWDER, CreatureFx.Phoenix::new);
        add("fire_meteor", "Огненный метеор", "Падение по дуге и расширяющийся взрыв", "FLAME, DUST, LARGE_SMOKE", Material.FIRE_CHARGE, CreatureFx.FireMeteor::new);
        add("ice_crown", "Ледяная корона", "Шипы растут по окружности и резко разлетаются", "SNOWFLAKE, DUST, GLOW", Material.PACKED_ICE, CreatureFx.IceCrown::new);
        add("solar_eclipse", "Солнечное затмение", "Кольца вращаются вокруг тёмного диска и вспыхивают", "DUST, GLOW, FIREWORK, FLASH", Material.BLACK_DYE, CreatureFx.SolarEclipse::new);

        add("moon_portal", "Лунный портал", "Рамки раскрываются в большой портал", "END_ROD, PORTAL, FLASH", Material.CRYING_OBSIDIAN, MagicFx.MoonPortal::new);
        add("magic_portal", "Магический портал", "Спиральный проход расширяется и схлопывается", "PORTAL, DUST, POOF", Material.OBSERVER, MagicFx.MagicPortal::new);
        add("rune", "Руна", "Геометрическая руна прорисовывается и взрывается", "WAX_ON, DUST, GLOW", Material.WAXED_COPPER_INGOT, MagicFx.Rune::new);
        add("magic_circle", "Магический круг", "Окружности и символы вращаются в разных направлениях", "ENCHANT, WITCH, DUST, FLASH", Material.ENCHANTING_TABLE, MagicFx.MagicCircle::new);
        add("energy_sphere", "Энергетическая сфера", "Оболочки собираются и резко расходятся", "ELECTRIC_SPARK, DUST, GLOW", Material.SPONGE, MagicFx.EnergySphere::new);
        add("energy_beam", "Энергетический луч", "Спирали сходятся в луч и создают ударную волну", "DUST, END_ROD, GLOW, ELECTRIC_SPARK", Material.CONDUIT, MagicFx.EnergyBeam::new);
        add("pulsating_shield", "Пульсирующий щит", "Купол пульсирует и отражает волны наружу", "ELECTRIC_SPARK, DUST, GLOW", Material.SHIELD, MagicFx.PulsatingShield::new);
        add("crystal", "Кристалл", "Многогранная фигура вырастает и рассыпается", "GLOW, CRIT, DUST", Material.AMETHYST_BLOCK, MagicFx.Crystal::new);
        add("crystal_explosion", "Кристаллический взрыв", "Кристаллические лучи разлетаются из центра", "GLOW, DUST, CRIT", Material.QUARTZ, MagicFx.CrystalExplosion::new);

        add("star_rain", "Звёздный дождь", "Частицы поднимаются, разворачиваются и падают", "FIREWORK, DUST, GLOW", Material.GLOWSTONE_DUST, ShapeFx.StarRain::new);
        add("spiral_galaxy", "Спиральная галактика", "Рукава вращаются и постепенно раскрываются", "DUST, FLASH", Material.GRAY_DYE, ShapeFx.SpiralGalaxy::new);
        add("geometric_cube", "Геометрический куб", "Рёбра вычерчиваются, куб распадается на грани", "WAX_ON, DUST", Material.WHITE_WOOL, ShapeFx.GeometricCube::new);
        add("tesseract", "Тессеракт", "Два встречно вращающихся куба резко раздвигаются", "GLOW, END_ROD, FLASH", Material.ENDER_EYE, ShapeFx.Tesseract::new);
        add("hourglass", "Песочные часы", "Песок перетекает между половинами, форма переворачивается", "WAX_ON, DUST, FLASH", Material.GLASS_BOTTLE, ShapeFx.Hourglass::new);
        add("dna_spiral", "ДНК-спираль", "Две спирали с перемычками быстро раскручиваются", "DUST, END_ROD", Material.SLIME_BALL, ShapeFx.DnaSpiral::new);
        add("heart", "Сердце из частиц", "Контур пульсирует и превращается в кольца", "DUST, HEART, GLOW", Material.RED_DYE, ShapeFx.Heart::new);
        add("flower_burst", "Цветочный взрыв", "Лепестки раскрываются несколькими слоями", "DUST, CHERRY_LEAVES", Material.PINK_TULIP, ShapeFx.FlowerBurst::new);
        add("tree_of_life", "Дерево жизни", "Ствол и ветви вырастают, по кроне идёт вспышка", "DUST_PLUME, DUST, FLASH", Material.OAK_SAPLING, ShapeFx.TreeOfLife::new);

        add("star_explosion", "Звёздный взрыв", "Звезда пульсирует и выбрасывает лучи", "FIREWORK, DUST, FLASH, EXPLOSION_EMITTER", Material.GOLD_NUGGET, FinalFx.StarExplosion::new);
        add("double_spiral", "Двойная спираль", "Встречные спирали пересекаются и взрываются", "DUST, GLOW, ELECTRIC_SPARK", Material.PURPLE_DYE, FinalFx.DoubleSpiral::new);
        add("light_arch", "Арка света", "Широкая дуга вырастает и распадается волнами", "END_ROD, DUST, GLOW", Material.RAIL, FinalFx.LightArch::new);
        add("energy_pillar", "Энергетическая колонна", "Кольца поднимаются, сжимаясь и расширяясь", "DUST, GLOW, END_ROD, FLASH", Material.BEACON, FinalFx.EnergyPillar::new);
        add("shockwave", "Шоковая волна", "Кольца расходятся в горизонтальной и вертикальной плоскостях", "DUST, GLOW, FLASH, EXPLOSION", Material.WIND_CHARGE, FinalFx.Shockwave::new);
        add("apocalypse", "Финальный апокалипсис", "Вихрь, вспышка, кольца, лучи и финальное схлопывание", "DUST, FLAME, FLASH, GLOW, FIREWORK, POOF", Material.TNT, FinalFx.Apocalypse::new);
    }

    public FxInfo get(String id) { return byId.get(id); }
    public List<FxInfo> all() { return order; }
    public int size() { return order.size(); }
}
