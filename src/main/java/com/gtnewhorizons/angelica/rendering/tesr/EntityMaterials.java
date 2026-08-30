package com.gtnewhorizons.angelica.rendering.tesr;

import com.gtnewhorizon.gtnhlib.client.renderer.MatrixHelper;
import com.gtnewhorizons.angelica.api.tesr.TesrMaterial;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.gtnewhorizons.angelica.glsm.states.BlendState;
import org.lwjgl.opengl.GL11;

public final class EntityMaterials {

    static final TesrMaterial CUTOUT = TesrMaterial.builder().cutout(0.1f).stream().build();
    static final TesrMaterial CUTOUT_HALF = TesrMaterial.builder().cutout(0.5f).stream().build();
    static final TesrMaterial SOLID = TesrMaterial.builder().stream().build();
    static final TesrMaterial TRANSLUCENT = TesrMaterial.builder().translucent().cutout(1f / 255f).noDepthWrite().stream().build();
    static final TesrMaterial TRANSLUCENT_DEPTH_WRITE = TesrMaterial.builder().translucent().cutout(1f / 255f).stream().build();
    static final TesrMaterial ADDITIVE = TesrMaterial.builder().additive().stream().build();
    static final TesrMaterial ADDITIVE_NO_DEPTH_WRITE = TesrMaterial.builder().additive().noDepthWrite().stream().build();
    static final TesrMaterial ADDITIVE_ALPHA = TesrMaterial.builder().additiveAlpha().stream().build();
    static final TesrMaterial ADDITIVE_ALPHA_NO_DEPTH_WRITE = TesrMaterial.builder().additiveAlpha().noDepthWrite().stream().build();
    static final TesrMaterial OVERLAY = TesrMaterial.builder().translucent().depthEqual().stream().build();
    static final TesrMaterial GLINT = TesrMaterial.builder().glint().depthEqual().noDepthWrite().unlit().stream()
        .special(TesrMaterial.SpecialRender.GLINT).build();

    // noCull 变体：实体渲染默认关剔除（-1/-1/1 缩放翻转绕序），批量绘制需与之一致，否则背面/外表面会被剔除。预 intern，无每次调用开销。
    static final TesrMaterial SOLID_NO_CULL = TesrMaterial.builder().noCull().stream().build();
    static final TesrMaterial CUTOUT_NO_CULL = TesrMaterial.builder().cutout(0.1f).noCull().stream().build();
    static final TesrMaterial CUTOUT_HALF_NO_CULL = TesrMaterial.builder().cutout(0.5f).noCull().stream().build();
    static final TesrMaterial TRANSLUCENT_NO_CULL = TesrMaterial.builder().translucent().cutout(1f / 255f).noDepthWrite().noCull().stream().build();
    static final TesrMaterial TRANSLUCENT_DEPTH_WRITE_NO_CULL = TesrMaterial.builder().translucent().cutout(1f / 255f).noCull().stream().build();
    static final TesrMaterial ADDITIVE_NO_CULL = TesrMaterial.builder().additive().noCull().stream().build();
    static final TesrMaterial ADDITIVE_NO_DEPTH_WRITE_NO_CULL = TesrMaterial.builder().additive().noDepthWrite().noCull().stream().build();
    static final TesrMaterial ADDITIVE_ALPHA_NO_CULL = TesrMaterial.builder().additiveAlpha().noCull().stream().build();
    static final TesrMaterial ADDITIVE_ALPHA_NO_DEPTH_WRITE_NO_CULL = TesrMaterial.builder().additiveAlpha().noDepthWrite().noCull().stream().build();
    static final TesrMaterial GLINT_NO_CULL = TesrMaterial.builder().glint().depthEqual().noDepthWrite().unlit().noCull().stream()
        .special(TesrMaterial.SpecialRender.GLINT).build();

    private EntityMaterials() {}

    static TesrMaterial fromCurrentState() {
        final boolean textured = GLStateManager.getTextures().getTextureUnitStates(0).isEnabled();
        final boolean texAnimated = textured && !MatrixHelper.isIdentity(GLStateManager.getTextures().getTextureUnitMatrix(0));
        final BlendState blend = GLStateManager.getBlendState();
        final boolean cullEnabled = GLStateManager.getCullState().isEnabled();
        return fromState(textured, texAnimated,
            GLStateManager.getBlendMode().isEnabled(), blend.getSrcRgb(), blend.getDstRgb(),
            GLStateManager.getAlphaTest().isEnabled(),
            GLStateManager.getAlphaState().getFunction(), GLStateManager.getAlphaState().getReference(),
            GLStateManager.getDepthState().getFunc(), GLStateManager.getDepthState().isEnabled(),
            cullEnabled);
    }

    static TesrMaterial fromState(boolean textured, boolean blend, int srcRgb, int dstRgb, boolean alphaTest, int alphaFunc, float alphaRef, int depthFunc, boolean depthMask) {
        return fromState(textured, false, blend, srcRgb, dstRgb, alphaTest, alphaFunc, alphaRef, depthFunc, depthMask, true);
    }

    static TesrMaterial fromState(boolean textured, boolean texAnimated, boolean blend, int srcRgb, int dstRgb, boolean alphaTest, int alphaFunc, float alphaRef, int depthFunc, boolean depthMask) {
        return fromState(textured, texAnimated, blend, srcRgb, dstRgb, alphaTest, alphaFunc, alphaRef, depthFunc, depthMask, true);
    }

    /**
     * Batched parts must keep the exact GL state (including back-face culling) they had at queue
     * time. Vanilla entity rendering disables culling (the -1/-1/1 scale flips winding, so culling
     * would hide the outer faces), so parts queued while culling is off get a noCull material,
     * otherwise the batch draw would cull their back/outer faces and they turn transparent.
     */
    static TesrMaterial fromState(boolean textured, boolean texAnimated, boolean blend, int srcRgb, int dstRgb, boolean alphaTest, int alphaFunc, float alphaRef, int depthFunc, boolean depthMask, boolean cullEnabled) {
        if (!textured) {
            if (blend && depthFunc == GL11.GL_EQUAL && srcRgb == GL11.GL_SRC_ALPHA && dstRgb == GL11.GL_ONE_MINUS_SRC_ALPHA) {
                // Overlay decal: matched by identity in ModelPartBatcher, keep the shared instance
                return OVERLAY;
            }
            return null;
        }
        if (texAnimated) {
            if (blend && !depthMask && depthFunc == GL11.GL_EQUAL && srcRgb == GL11.GL_SRC_COLOR && dstRgb == GL11.GL_ONE) {
                return pick(GLINT, GLINT_NO_CULL, cullEnabled);
            }
            if (blend && (depthFunc == GL11.GL_LEQUAL || depthFunc == GL11.GL_LESS)) {
                if (srcRgb == GL11.GL_ONE && dstRgb == GL11.GL_ONE) {
                    return depthMask ? pick(ADDITIVE, ADDITIVE_NO_CULL, cullEnabled)
                        : pick(ADDITIVE_NO_DEPTH_WRITE, ADDITIVE_NO_DEPTH_WRITE_NO_CULL, cullEnabled);
                }
                if (srcRgb == GL11.GL_SRC_ALPHA && dstRgb == GL11.GL_ONE) {
                    return depthMask ? pick(ADDITIVE_ALPHA, ADDITIVE_ALPHA_NO_CULL, cullEnabled)
                        : pick(ADDITIVE_ALPHA_NO_DEPTH_WRITE, ADDITIVE_ALPHA_NO_DEPTH_WRITE_NO_CULL, cullEnabled);
                }
            }
            return null;
        }
        if (depthFunc != GL11.GL_LEQUAL && depthFunc != GL11.GL_LESS) {
            return null;
        }
        if (!blend) {
            if (!alphaTest) return pick(SOLID, SOLID_NO_CULL, cullEnabled);
            if (alphaFunc == GL11.GL_GREATER && Math.abs(alphaRef - 0.1f) < 1e-4f) return pick(CUTOUT, CUTOUT_NO_CULL, cullEnabled);
            if (alphaFunc == GL11.GL_GREATER && Math.abs(alphaRef - 0.5f) < 1e-4f) return pick(CUTOUT_HALF, CUTOUT_HALF_NO_CULL, cullEnabled);
            return null;
        }
        if (srcRgb == GL11.GL_SRC_ALPHA && dstRgb == GL11.GL_ONE_MINUS_SRC_ALPHA) {
            return depthMask ? pick(TRANSLUCENT_DEPTH_WRITE, TRANSLUCENT_DEPTH_WRITE_NO_CULL, cullEnabled)
                : pick(TRANSLUCENT, TRANSLUCENT_NO_CULL, cullEnabled);
        }
        if (srcRgb == GL11.GL_ONE && dstRgb == GL11.GL_ONE) {
            return depthMask ? pick(ADDITIVE, ADDITIVE_NO_CULL, cullEnabled)
                : pick(ADDITIVE_NO_DEPTH_WRITE, ADDITIVE_NO_DEPTH_WRITE_NO_CULL, cullEnabled);
        }
        if (srcRgb == GL11.GL_SRC_ALPHA && dstRgb == GL11.GL_ONE) {
            return depthMask ? pick(ADDITIVE_ALPHA, ADDITIVE_ALPHA_NO_CULL, cullEnabled)
                : pick(ADDITIVE_ALPHA_NO_DEPTH_WRITE, ADDITIVE_ALPHA_NO_DEPTH_WRITE_NO_CULL, cullEnabled);
        }
        return null;
    }

    /** 剔除关闭时选用 noCull 变体，保证批量绘制与即时渲染一致。两个方向都是预 intern 的共享常量，每次调用零分配、零查找。 */
    private static TesrMaterial pick(TesrMaterial normal, TesrMaterial noCull, boolean cullEnabled) {
        return cullEnabled ? normal : noCull;
    }
}
