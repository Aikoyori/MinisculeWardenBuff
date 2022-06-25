package aikoyori.wardenloots.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.Set;


@Mixin(WardenEntity.class)
public abstract class WardenLootsMixin extends MobEntity {
    private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(this.getDisplayName(), BossBar.Color.BLUE, BossBar.Style.PROGRESS);
    private static Set allLootTables = LootTables.getAll();
    private static Random randomise = new Random();
    protected WardenLootsMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "<init>",constant = @Constant(intValue = 5))
    int XPModifier(int constant)
    {
        return 30000;
    }
    @ModifyConstant(method = "addAttributes",constant = @Constant(doubleValue = 500.0))
    private static double HPmodification(double constant)
    {
        return 4000.0;
    }
    @ModifyArg(method = "tryAttack",at=@At(value = "INVOKE",target = "Lnet/minecraft/entity/ai/brain/task/SonicBoomTask;cooldown(Lnet/minecraft/entity/LivingEntity;I)V"),index = 1)
    int cooldownisreduced(int cooldown){return 30;}

    @Inject(method = "readCustomDataFromNbt",at = @At("TAIL"))
    public void readCustomDataFromNbtXD(NbtCompound nbt, CallbackInfo ci) {
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    @Inject(method = "mobTick",at = @At("TAIL"))
    protected void mobTicker(CallbackInfo ci) {

        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if(this.getY()<this.getWorld().getBottomY() && !this.isAiDisabled())
        {
            this.teleport(this.getX(),this.world.getTopY(),this.getZ());
            this.addVelocity(0.0,-10.0,0.0);
            this.fallDistance = 0.0f;
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source instanceof ProjectileDamageSource) {
            return false;
        }
        boolean bl2 = super.damage(source, amount);
        return bl2;
    }

    public void dropLoot(DamageSource source, boolean causedByPlayer) {
        //System.out.println("AMONG YUS");
        WardenEntity warden = (WardenEntity)(Object)this;



        Identifier identifier = this.getLootTable();
        LootTable lootTable = warden.world.getServer().getLootManager().getTable(identifier);
        LootContext.Builder builder = warden.getLootContextBuilder(causedByPlayer, source);

        lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), warden::dropStack);

        identifier = (Identifier) allLootTables.toArray()[(randomise.nextInt(0,allLootTables.size()))];
        builder = warden.getLootContextBuilder(causedByPlayer, source);
        lootTable = warden.world.getServer().getLootManager().getTable(identifier);
        lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), warden::dropStack);


        /*
        for(Object item :allLootTables.toArray())
        {
            /*
            if(randomise.nextDouble()>0.9)
            {
                Identifier identifier = (Identifier) item;
                LootTable lootTable = warden.world.getServer().getLootManager().getTable(identifier);
                LootContext.Builder builder = warden.getLootContextBuilder(causedByPlayer, source);
                lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), warden::dropStack);
            }

        }*/
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient && this.isAlive() && this.age % 10 == 0) {
            this.heal(1.0F);
        }
    }
}
