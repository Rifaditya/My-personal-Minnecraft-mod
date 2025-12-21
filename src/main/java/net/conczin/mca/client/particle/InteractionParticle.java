package net.conczin.mca.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Interaction particle for MCA.
 * Updated for 1.21.11:
 * - TextureSheetParticle renamed to SingleQuadParticle
 * - Constructor now takes TextureAtlasSprite
 * - getRenderType() replaced with getLayer()
 * - ParticleProvider.createParticle now takes RandomSource
 */
public class InteractionParticle extends SingleQuadParticle {
    // Constructor now takes sprite parameter in 1.21.11
    protected InteractionParticle(ClientLevel world, double x, double y, double z, TextureAtlasSprite sprite) {
        super(world, x, y, z, sprite);
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.1D;
        this.quadSize *= 1.5F;
        this.lifetime = 20;
        this.hasPhysics = false;
    }

    // getRenderType() replaced with getLayer() in 1.21.11
    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float tickDelta) {
        return 0.3F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (this.y == this.yo) {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }

            this.xd *= 0.86F;
            this.yd *= 0.86F;
            this.zd *= 0.86F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }

    // Updated for 1.21.11: createParticle now takes RandomSource as last parameter
    public record Factory(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(
                SimpleParticleType options,
                ClientLevel level,
                double x, double y, double z,
                double xd, double yd, double zd,
                RandomSource random) {
            // Use spriteSet.get(random) instead of pickSprite()
            InteractionParticle particle = new InteractionParticle(level, x, y + 0.5D, z, this.spriteSet.get(random));
            particle.setColor(1.0F, 1.0F, 1.0F);
            return particle;
        }
    }
}
