package mod.akrivus.clones.init;

import java.io.InputStream;

import mod.akrivus.clones.block.BlockChroningMachine;
import mod.akrivus.clones.block.BlockCloneReturnPad;
import mod.akrivus.clones.block.BlockCloningMachine;
import mod.akrivus.clones.client.render.RenderClone;
import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.SkillBase;
import mod.akrivus.clones.skills.pack.BreedLivestock;
import mod.akrivus.clones.skills.pack.CollectLiquids;
import mod.akrivus.clones.skills.pack.Come;
import mod.akrivus.clones.skills.pack.CutDownTrees;
import mod.akrivus.clones.skills.pack.Defend;
import mod.akrivus.clones.skills.pack.Follow;
import mod.akrivus.clones.skills.pack.HarvestBeetroots;
import mod.akrivus.clones.skills.pack.HarvestCacti;
import mod.akrivus.clones.skills.pack.HarvestCarrots;
import mod.akrivus.clones.skills.pack.HarvestMelons;
import mod.akrivus.clones.skills.pack.HarvestNetherWart;
import mod.akrivus.clones.skills.pack.HarvestPotatoes;
import mod.akrivus.clones.skills.pack.HarvestPumpkins;
import mod.akrivus.clones.skills.pack.HarvestReeds;
import mod.akrivus.clones.skills.pack.HarvestWheat;
import mod.akrivus.clones.skills.pack.KillOtherClones;
import mod.akrivus.clones.skills.pack.KillOtherEntities;
import mod.akrivus.clones.skills.pack.KillOtherPlayers;
import mod.akrivus.clones.skills.pack.Look;
import mod.akrivus.clones.skills.pack.MakeBridge;
import mod.akrivus.clones.skills.pack.MakeStairs;
import mod.akrivus.clones.skills.pack.MilkCows;
import mod.akrivus.clones.skills.pack.MowGrass;
import mod.akrivus.clones.skills.pack.PickFlowers;
import mod.akrivus.clones.skills.pack.PlantSaplings;
import mod.akrivus.clones.skills.pack.Stop;
import mod.akrivus.clones.tileentity.TileEntityChroningMachine;
import mod.akrivus.clones.tileentity.TileEntityCloningMachine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

@SuppressWarnings({ "unchecked", "deprecation" })
@Mod(modid = Clones.MODID, version = Clones.VERSION)
public class Clones {
    public static final String MCVERSION = "@mcversion";
    public static final String VERSION = "@version";
    public static final String MODID = "clones";
    
    public static final BlockChroningMachine CHRONING_MACHINE = new BlockChroningMachine();
    public static final BlockCloningMachine CLONING_MACHINE = new BlockCloningMachine();
    public static final BlockCloneReturnPad CLONE_RETURN_PAD = new BlockCloneReturnPad();
    
    @Instance
    public static Clones instance;
    public static SentenceModel sentModel;
    public static SentenceDetectorME sentDetector;
    public static POSModel posModel;
    public static POSTaggerME posTagger;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
    	try {
    		InputStream input = null;
    		input = this.getClass().getClassLoader().getResourceAsStream("assets/clones/lang/processing/en-sent.bin");
    		Clones.sentModel = new SentenceModel(input);
    		Clones.sentDetector = new SentenceDetectorME(Clones.sentModel);
    		input = this.getClass().getClassLoader().getResourceAsStream("assets/clones/lang/processing/en-pos-perceptron.bin");
    		Clones.posModel = new POSModel(input);
    		Clones.posTagger = new POSTaggerME(Clones.posModel);
    	}
    	catch (Exception ex) {
    		CrashReport.makeCrashReport(ex, "Something went wrong loading OpenNLP.");
    	}
    }
	@EventHandler
    public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new Events());
		GameRegistry.registerTileEntity(TileEntityChroningMachine.class, "chroning_machine");
		GameRegistry.registerTileEntity(TileEntityCloningMachine.class, "cloning_machine");
		EntityRegistry.registerModEntity(new ResourceLocation("clones:clone"), EntityClone.class, "clone", 0, Clones.instance, 256, 1, true, 0xAA7D66, 0x00AFAF);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			try {
				Class<RenderClone> render = (Class<RenderClone>) this.getClass().getClassLoader().loadClass("mod/akrivus/clones/client/render/RenderClone");
				RenderingRegistry.registerEntityRenderingHandler(EntityClone.class, render.newInstance());
			}
			catch (Exception ex) {
				CrashReport.makeCrashReport(ex, "Something went wrong registering Clones.");
			}
		}
		Clones.addBlock(Clones.CHRONING_MACHINE);
		Clones.addBlock(Clones.CLONING_MACHINE);
		Clones.addBlock(Clones.CLONE_RETURN_PAD);
		GameRegistry.addRecipe(new ItemStack(Clones.CHRONING_MACHINE), "OCO", "DPD", "SRS", 'O', Blocks.OBSIDIAN, 'C', Items.CLOCK, 'D', Items.DIAMOND, 'P', Items.ENDER_PEARL, 'S', Blocks.COBBLESTONE, 'R', Items.REDSTONE);
		GameRegistry.addRecipe(new ItemStack(Clones.CLONING_MACHINE), "OGO", "DHD", "SRS", 'O', Blocks.OBSIDIAN, 'G', Blocks.GOLD_BLOCK, 'D', Items.DIAMOND, 'H', Blocks.HOPPER, 'S', Blocks.COBBLESTONE, 'R', Items.REDSTONE);
		GameRegistry.addRecipe(new ItemStack(Clones.CLONE_RETURN_PAD), "OPO", "SOS", "SRS", 'O', Blocks.OBSIDIAN, 'P', Items.ENDER_PEARL, 'S', Blocks.COBBLESTONE, 'R', Items.REDSTONE);
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		Clones.addSkill(MakeBridge.class);
		Clones.addSkill(CutDownTrees.class);
		Clones.addSkill(PickFlowers.class);
		Clones.addSkill(KillOtherClones.class);
		Clones.addSkill(KillOtherEntities.class);
		Clones.addSkill(KillOtherPlayers.class);
		Clones.addSkill(Follow.class);
		Clones.addSkill(Stop.class);
		Clones.addSkill(Come.class);
		Clones.addSkill(HarvestBeetroots.class);
		Clones.addSkill(HarvestCarrots.class);
		Clones.addSkill(HarvestNetherWart.class);
		Clones.addSkill(HarvestPotatoes.class);
		Clones.addSkill(HarvestWheat.class);
		Clones.addSkill(HarvestReeds.class);
		Clones.addSkill(HarvestCacti.class);
		Clones.addSkill(MakeStairs.class);
		Clones.addSkill(HarvestMelons.class);
		Clones.addSkill(HarvestPumpkins.class);
		Clones.addSkill(MowGrass.class);
		Clones.addSkill(PlantSaplings.class);
		Clones.addSkill(BreedLivestock.class);
		Clones.addSkill(Look.class);
		Clones.addSkill(MilkCows.class);
		Clones.addSkill(CollectLiquids.class);
		Clones.addSkill(Defend.class);
	}
	public static void addSkill(Class<? extends SkillBase> skillToAdd) {
		try {
			SkillBase addedSkill = skillToAdd.newInstance();
			int index = 0;
			switch (addedSkill.priority()) {
			default:
				if (EntityClone.SKILLS.isEmpty()) {
					EntityClone.SKILLS.add(skillToAdd);
				}
				else {
					for (Class<? extends SkillBase> skillClass : EntityClone.SKILLS) {
						SkillBase skill = skillClass.newInstance();
						if (skill.priority().ordinal() > addedSkill.priority().ordinal() || index == EntityClone.SKILLS.size() - 1) {
							EntityClone.SKILLS.add(index, skillToAdd);
							return;
						}
						++index;
					}
				}
			case LOW:
				EntityClone.SKILLS.add(skillToAdd);
				return;
			}
		}
		catch (Exception ex) {
			CrashReport.makeCrashReport(ex, "Something went wrong registering skills.");
		}
	}
	public static void addBlock(Block block) {
		ResourceLocation location = new ResourceLocation("clones:" + block.getUnlocalizedName().replaceFirst("tile\\.", ""));
		GameRegistry.register(block, location);
		addItem(new ItemBlock(block));
	}
	public static void addItem(Item item) {
		ResourceLocation location = new ResourceLocation("clones:" + item.getUnlocalizedName().replaceFirst("item\\.|tile\\.", ""));
		GameRegistry.register(item, location);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
}
