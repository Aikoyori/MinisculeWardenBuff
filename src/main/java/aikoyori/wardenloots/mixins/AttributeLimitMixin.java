package aikoyori.wardenloots.mixins;

import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityAttributes.class)
public class AttributeLimitMixin {
    /*
    @ModifyConstant(method = "<clinit>",constant = @Constant(floatValue = 1024.0f))
    private static float attributeHacker(float constant)
    {

        return 4096.0f;
    }*/
    @ModifyArg(method = "<clinit>",at=@At(value = "INVOKE",target = "Lnet/minecraft/entity/attribute/ClampedEntityAttribute;<init>(Ljava/lang/String;DDD)V"),index = 3)
    private static double attributeHacker(double fallback)
    {

        return 4096.0f;
    }

}
