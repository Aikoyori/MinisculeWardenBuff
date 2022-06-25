package aikoyori.wardenloots.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Random;
import java.util.Set;


@Mixin(WardenEntity.class)
public abstract class WardenLootsMixin extends MobEntity {
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


        Identifier identifier = (Identifier) allLootTables.toArray()[randomise.nextInt()%(allLootTables.size())];

        LootTable lootTable = warden.world.getServer().getLootManager().getTable(identifier);
        LootContext.Builder builder = warden.getLootContextBuilder(causedByPlayer, source);
        lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), warden::dropStack);
        identifier = this.getLootTable();
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
