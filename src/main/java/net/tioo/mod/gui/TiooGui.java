package net.tioo.mod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.tioo.mod.TiooMod;
import net.tioo.mod.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class TiooGui extends Screen {

    // ── Colors ────────────────────────────────────────────────────────────
    private static final int C_BG        = col(0,   0,   0,   224);
    private static final int C_HDR_BG    = col(20,  20,  20,  245);
    private static final int C_GREEN     = col(85,  255, 85,  255);
    private static final int C_GREEN_DIM = col(85,  255, 85,  13);
    private static final int C_WHITE     = col(255, 255, 255, 255);
    private static final int C_GRAY      = col(204, 204, 204, 255);
    private static final int C_DARK_BG   = col(0,   0,   0,   77);
    private static final int C_HOVER     = col(255, 255, 255, 10);
    private static final int C_DIVIDER   = col(51,  51,  51,  255);
    private static final int C_ROW_DIV   = col(255, 255, 255, 8);
    private static final int C_CB_BD     = col(102, 102, 102, 255);
    private static final int C_CB_BG     = col(0,   0,   0,   128);
    private static final int C_CB_ON     = col(255, 170, 0,   255);
    private static final int C_CB_IN     = col(255, 248, 224, 255);
    private static final int C_SL_TRACK  = col(51,  51,  51,  255);
    private static final int C_SL_FILL   = col(85,  255, 85,  120);
    private static final int C_SL_HANDLE = col(255, 119, 204, 255);
    private static final int C_SL_VAL    = col(255, 119, 204, 255);
    private static final int C_KB_BG     = col(255, 255, 255, 18);
    private static final int C_KB_BD     = col(68,  68,  68,  255);
    private static final int C_ARROW     = col(136, 136, 136, 255);
    private static final int C_LEGIT     = col(85,  255, 255, 255); // cyan for legit mode

    // ── Layout ────────────────────────────────────────────────────────────
    private static final int WIN_W = 210;
    private static final int HDR_H = 28;
    private static final int MOD_H = 22;
    private static final int ROW_H = 20;
    private static final int SLD_H = 36;
    private static final int PAD_L = 10;
    private static final int PAD_S = 16;
    private static final int CB    = 10;
    private static final int DOT   = 6;
    private static final int SL_H  = 3;
    private static final int SL_TW = 7;
    private static final int SL_TH = 13;

    // ── State ─────────────────────────────────────────────────────────────
    private int winX = 60, winY = 40;
    private boolean dragging; int dragDX, dragDY;
    private int dragSlider = -1;

    private final List<ModEntry>  modules = new ArrayList<>();
    private final List<SliderDef> sliders = new ArrayList<>();

    // ── Data classes ──────────────────────────────────────────────────────
    static class BoolSetting {
        String label; boolean val; boolean isLegit;
        BoolSetting(String l, boolean v) { label=l; val=v; isLegit=false; }
        BoolSetting(String l, boolean v, boolean leg) { label=l; val=v; isLegit=leg; }
    }
    static class SliderDef {
        String label; float val; float min; float max;
        boolean range; int guiY;
        Runnable onChange;
        SliderDef(String l, float v, float mn, float mx, boolean r, Runnable cb) {
            label=l; val=v; min=mn; max=mx; range=r; onChange=cb;
        }
    }
    static class ModEntry {
        Module mod; List<Object> settings = new ArrayList<>(); boolean expanded;
        ModEntry(Module m, boolean exp) { mod=m; expanded=exp; }
    }

    public TiooGui() {
        super(Text.literal("Tioo"));

        // ── TriggerBot ────────────────────────────────────────────────────
        ModEntry tb = new ModEntry(TiooMod.triggerBot, true);

        // Legit Mode — cyan highlight
        tb.settings.add(new BoolSetting("Legit Mode",
            TiooMod.triggerBot.isLegitMode(), true));

        // Work While Jumping
        tb.settings.add(new BoolSetting("Work While Jumping",
            TiooMod.triggerBot.isWorkWhileJump()));

        // While Ascending
        tb.settings.add(new BoolSetting("While Ascending",
            TiooMod.triggerBot.isWhileAscend()));

        // Random Delay slider
        SliderDef minSl = new SliderDef("Min Delay",
            TiooMod.triggerBot.getMinDelay(), 10, 300, false,
            () -> TiooMod.triggerBot.setMinDelay((int) sliders.get(0).val));
        SliderDef maxSl = new SliderDef("Max Delay",
            TiooMod.triggerBot.getMaxDelay(), 10, 300, false,
            () -> TiooMod.triggerBot.setMaxDelay((int) sliders.get(1).val));
        tb.settings.add(minSl);
        tb.settings.add(maxSl);
        sliders.add(minSl); // index 0
        sliders.add(maxSl); // index 1

        // Miss Chance slider
        SliderDef missSl = new SliderDef("Miss Chance",
            TiooMod.triggerBot.getMissChance(), 0, 30, false,
            () -> TiooMod.triggerBot.setMissChance((int) sliders.get(2).val));
        tb.settings.add(missSl);
        sliders.add(missSl); // index 2

        // Humanize Cooldown
        tb.settings.add(new BoolSetting("Humanize Cooldown",
            TiooMod.triggerBot.isHumanizeCool()));

        // Separator visual
        tb.settings.add("--- Combat Settings ---");

        tb.settings.add(new BoolSetting("Check Shield",
            TiooMod.triggerBot.isCheckShield()));
        tb.settings.add(new BoolSetting("Only Crit Sword",
            TiooMod.triggerBot.isOnlyCritSword()));
        tb.settings.add(new BoolSetting("Only Crit Axe",
            TiooMod.triggerBot.isOnlyCritAxe()));
        tb.settings.add(new BoolSetting("All Entities",
            TiooMod.triggerBot.isAllEntities()));
        tb.settings.add(new BoolSetting("Swing Hand",
            TiooMod.triggerBot.isSwingHand()));

        // Sword Delay (base Argon delay)
        SliderDef swordSl = new SliderDef("Sword Delay", 545, 400, 700, false, null);
        SliderDef axeSl   = new SliderDef("Axe Delay",   790, 600, 900, false, null);
        tb.settings.add(swordSl);
        tb.settings.add(axeSl);
        sliders.add(swordSl); // index 3
        sliders.add(axeSl);   // index 4

        tb.settings.add("BACKSLASH");
        modules.add(tb);

        // ── ShieldDisabler ────────────────────────────────────────────────
        ModEntry sd = new ModEntry(TiooMod.shieldDisabler, false);
        sd.settings.add(new BoolSetting("Switch Back",      TiooMod.shieldDisabler.isSwitchBack()));
        sd.settings.add(new BoolSetting("Require Axe",      TiooMod.shieldDisabler.isRequireHoldAxe()));
        SliderDef hitSl = new SliderDef("Hit Delay",    0, 0, 20, false, null);
        SliderDef swSl  = new SliderDef("Switch Delay", 0, 0, 20, false, null);
        sd.settings.add(hitSl);
        sd.settings.add(swSl);
        sliders.add(hitSl);
        sliders.add(swSl);
        modules.add(sd);
    }

    // ── Render ────────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int winH = totalH();

        // BG + border
        fill(ctx, winX, winY, winX+WIN_W, winY+winH, C_BG);
        fill(ctx, winX, winY, winX+WIN_W, winY+1, C_DIVIDER);
        fill(ctx, winX, winY+winH-1, winX+WIN_W, winY+winH, C_DIVIDER);
        fill(ctx, winX, winY, winX+1, winY+winH, C_DIVIDER);
        fill(ctx, winX+WIN_W-1, winY, winX+WIN_W, winY+winH, C_DIVIDER);

        // Header
        fill(ctx, winX, winY, winX+WIN_W, winY+HDR_H, C_HDR_BG);
        ctx.drawText(textRenderer, "Combat", winX+PAD_L, winY+8, C_WHITE, true);
        fill(ctx, winX, winY+HDR_H-3, winX+WIN_W, winY+HDR_H, C_GREEN);

        int curY = winY + HDR_H;

        for (int mi = 0; mi < modules.size(); mi++) {
            ModEntry m = modules.get(mi);
            boolean on = m.mod.isEnabled();

            if (on) {
                fill(ctx, winX, curY, winX+3, curY+MOD_H, C_GREEN);
                fill(ctx, winX+3, curY, winX+WIN_W, curY+MOD_H, C_GREEN_DIM);
            }
            if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+MOD_H)
                fill(ctx, winX, curY, winX+WIN_W, curY+MOD_H, C_HOVER);

            // Dot
            int dotX = winX+PAD_L, dotY = curY+MOD_H/2-DOT/2;
            if (on) {
                fill(ctx, dotX, dotY, dotX+DOT, dotY+DOT, C_GREEN);
            } else {
                fill(ctx, dotX, dotY, dotX+DOT, dotY+1, C_CB_BD);
                fill(ctx, dotX, dotY+DOT-1, dotX+DOT, dotY+DOT, C_CB_BD);
                fill(ctx, dotX, dotY, dotX+1, dotY+DOT, C_CB_BD);
                fill(ctx, dotX+DOT-1, dotY, dotX+DOT, dotY+DOT, C_CB_BD);
            }

            ctx.drawText(textRenderer, m.mod.getName(), winX+PAD_L+DOT+5, curY+7, C_WHITE, true);
            ctx.drawText(textRenderer, m.expanded?"v":">", winX+WIN_W-14, curY+7, C_ARROW, false);

            curY += MOD_H;

            if (!m.expanded && mi < modules.size()-1)
                fill(ctx, winX+8, curY-1, winX+WIN_W-8, curY, C_DIVIDER);

            if (m.expanded) {
                fill(ctx, winX, curY, winX+WIN_W, curY+settingsH(m), C_DARK_BG);

                for (Object s : m.settings) {
                    if (s instanceof BoolSetting bs) {
                        if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+ROW_H)
                            fill(ctx, winX, curY, winX+WIN_W, curY+ROW_H, C_HOVER);

                        // Label — legit mode pakai warna cyan
                        int lblCol = bs.isLegit ? C_LEGIT : C_GRAY;
                        ctx.drawText(textRenderer, bs.label, winX+PAD_S, curY+6, lblCol, false);

                        // Checkbox
                        int cbX = winX+WIN_W-PAD_S-CB, cbY = curY+ROW_H/2-CB/2;
                        if (bs.val) {
                            fill(ctx, cbX, cbY, cbX+CB, cbY+CB, C_CB_ON);
                            fill(ctx, cbX+3, cbY+3, cbX+CB-3, cbY+CB-3, C_CB_IN);
                        } else {
                            fill(ctx, cbX, cbY, cbX+CB, cbY+CB, C_CB_BG);
                            fill(ctx, cbX, cbY, cbX+CB, cbY+1, C_CB_BD);
                            fill(ctx, cbX, cbY+CB-1, cbX+CB, cbY+CB, C_CB_BD);
                            fill(ctx, cbX, cbY, cbX+1, cbY+CB, C_CB_BD);
                            fill(ctx, cbX+CB-1, cbY, cbX+CB, cbY+CB, C_CB_BD);
                        }
                        fill(ctx, winX, curY+ROW_H-1, winX+WIN_W, curY+ROW_H, C_ROW_DIV);
                        curY += ROW_H;

                    } else if (s instanceof SliderDef sd) {
                        sd.guiY = curY;
                        ctx.drawText(textRenderer, sd.label, winX+PAD_S, curY+5, C_GRAY, false);

                        // Value
                        String vstr = String.format("%.0f", sd.val) + (sd.label.contains("Chance") ? "%" : "ms");
                        if (sd.label.contains("Delay") && sd.max <= 30) vstr = String.format("%.0f", sd.val);
                        int vw = textRenderer.getWidth(vstr);
                        ctx.drawText(textRenderer, vstr, winX+WIN_W-PAD_S-vw, curY+5, C_SL_VAL, false);

                        // Track
                        int tX1 = winX+PAD_S, tX2 = winX+WIN_W-PAD_S;
                        int tY  = curY+SLD_H/2+5;
                        fill(ctx, tX1, tY, tX2, tY+SL_H, C_SL_TRACK);

                        // Fill track
                        float ratio = (sd.val - sd.min) / (sd.max - sd.min);
                        int fillX = tX1 + (int)(ratio * (tX2 - tX1));
                        fill(ctx, tX1, tY, fillX, tY+SL_H, C_SL_FILL);

                        // Thumb
                        int tPos = tX1 + (int)(ratio * (tX2 - tX1 - SL_TW));
                        fill(ctx, tPos, tY-(SL_TH-SL_H)/2, tPos+SL_TW, tY+SL_H+(SL_TH-SL_H)/2, C_SL_HANDLE);

                        fill(ctx, winX, curY+SLD_H-1, winX+WIN_W, curY+SLD_H, C_ROW_DIV);
                        curY += SLD_H;

                    } else if (s instanceof String str) {
                        if (str.startsWith("---")) {
                            // Section separator
                            int sw = textRenderer.getWidth(str);
                            ctx.drawText(textRenderer, str, winX+WIN_W/2-sw/2, curY+6, col(100,100,100,200), false);
                            fill(ctx, winX, curY+ROW_H-1, winX+WIN_W, curY+ROW_H, C_ROW_DIV);
                            curY += ROW_H;
                        } else {
                            // Keybind
                            ctx.drawText(textRenderer, "Keybind", winX+PAD_S, curY+6, C_GRAY, false);
                            int kw = textRenderer.getWidth(str);
                            int kx1=winX+WIN_W-PAD_S-kw-6, ky1=curY+4, kx2=winX+WIN_W-PAD_S+4, ky2=curY+ROW_H-4;
                            fill(ctx, kx1, ky1, kx2, ky2, C_KB_BG);
                            fill(ctx, kx1, ky1, kx2, ky1+1, C_KB_BD);
                            fill(ctx, kx1, ky2-1, kx2, ky2, C_KB_BD);
                            fill(ctx, kx1, ky1, kx1+1, ky2, C_KB_BD);
                            fill(ctx, kx2-1, ky1, kx2, ky2, C_KB_BD);
                            ctx.drawText(textRenderer, str, winX+WIN_W-PAD_S-kw-2, curY+6, C_ARROW, false);
                            fill(ctx, winX, curY+ROW_H-1, winX+WIN_W, curY+ROW_H, C_ROW_DIV);
                            curY += ROW_H;
                        }
                    }
                }
                if (mi < modules.size()-1)
                    fill(ctx, winX+8, curY, winX+WIN_W-8, curY+1, C_DIVIDER);
            }
        }
        super.render(ctx, mx, my, delta);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (mx>=winX && mx<=winX+WIN_W && my>=winY && my<=winY+HDR_H) {
            dragging=true; dragDX=(int)(mx-winX); dragDY=(int)(my-winY); return true;
        }

        int curY = winY+HDR_H;
        for (int mi = 0; mi < modules.size(); mi++) {
            ModEntry m = modules.get(mi);

            if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+MOD_H) {
                if (btn==0) m.expanded = !m.expanded;
                if (btn==1) m.mod.toggle();
                return true;
            }
            curY += MOD_H;

            if (m.expanded) {
                int settingIdx = 0;
                for (Object s : m.settings) {
                    int rowH = (s instanceof SliderDef) ? SLD_H : ROW_H;
                    if (s instanceof BoolSetting bs) {
                        if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+ROW_H) {
                            bs.val = !bs.val;
                            applyBool(mi, settingIdx, bs.val);
                            return true;
                        }
                    } else if (s instanceof SliderDef sd) {
                        int tX1=winX+PAD_S, tX2=winX+WIN_W-PAD_S;
                        int tY=curY+SLD_H/2+5;
                        if (mx>=tX1 && mx<=tX2 && my>=tY-8 && my<=tY+8) {
                            float ratio=(float)((mx-tX1)/(tX2-tX1));
                            sd.val = sd.min + ratio*(sd.max-sd.min);
                            sd.val = Math.max(sd.min, Math.min(sd.max, sd.val));
                            if (sd.onChange != null) sd.onChange.run();
                            dragSlider = sliders.indexOf(sd);
                            return true;
                        }
                    }
                    curY += rowH;
                    settingIdx++;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double ddx, double ddy) {
        if (dragging) { winX=(int)(mx-dragDX); winY=(int)(my-dragDY); return true; }
        if (dragSlider >= 0 && dragSlider < sliders.size()) {
            SliderDef sd = sliders.get(dragSlider);
            int tX1=winX+PAD_S, tX2=winX+WIN_W-PAD_S;
            float ratio=(float)((mx-tX1)/(tX2-tX1));
            sd.val = sd.min + ratio*(sd.max-sd.min);
            sd.val = Math.max(sd.min, Math.min(sd.max, sd.val));
            if (sd.onChange != null) sd.onChange.run();
            return true;
        }
        return super.mouseDragged(mx, my, btn, ddx, ddy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging=false; dragSlider=-1;
        return super.mouseReleased(mx, my, btn);
    }

    // Apply bool setting ke module
    private void applyBool(int mi, int si, boolean val) {
        if (mi == 0) { // TriggerBot
            // Hitung index bool saja (skip slider & string)
            int boolIdx = 0;
            for (Object s : modules.get(0).settings) {
                if (s instanceof BoolSetting bs) {
                    if (boolIdx == si) {
                        // Map berdasarkan label
                        switch (bs.label) {
                            case "Legit Mode"         -> TiooMod.triggerBot.setLegitMode(val);
                            case "Work While Jumping" -> TiooMod.triggerBot.setWorkWhileJump(val);
                            case "While Ascending"    -> TiooMod.triggerBot.setWhileAscend(val);
                            case "Humanize Cooldown"  -> TiooMod.triggerBot.setHumanizeCool(val);
                            case "Check Shield"       -> TiooMod.triggerBot.setCheckShield(val);
                            case "Only Crit Sword"    -> TiooMod.triggerBot.setOnlyCritSword(val);
                            case "Only Crit Axe"      -> TiooMod.triggerBot.setOnlyCritAxe(val);
                            case "All Entities"       -> TiooMod.triggerBot.setAllEntities(val);
                            case "Swing Hand"         -> TiooMod.triggerBot.setSwingHand(val);
                        }
                        return;
                    }
                    boolIdx++;
                }
            }
        } else if (mi == 1) { // ShieldDisabler
            for (Object s : modules.get(1).settings) {
                if (s instanceof BoolSetting bs) {
                    switch (bs.label) {
                        case "Switch Back" -> TiooMod.shieldDisabler.setSwitchBack(val);
                        case "Require Axe" -> TiooMod.shieldDisabler.setRequireHoldAxe(val);
                    }
                    return;
                }
            }
        }
    }

    @Override public boolean shouldPause()      { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }

    private int totalH() {
        int h = HDR_H;
        for (ModEntry m : modules) {
            h += MOD_H;
            if (m.expanded) h += settingsH(m);
        }
        return h;
    }
    private int settingsH(ModEntry m) {
        int h=0;
        for (Object s : m.settings) h += (s instanceof SliderDef) ? SLD_H : ROW_H;
        return h;
    }
    private void fill(DrawContext c,int x1,int y1,int x2,int y2,int color) {
        if (x2>x1 && y2>y1) c.fill(x1,y1,x2,y2,color);
    }
    private static int col(int r,int g,int b,int a) { return (a<<24)|(r<<16)|(g<<8)|b; }
}
