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
    private static final int C_BG         = col(0,   0,   0,   224); // rgba(0,0,0,0.88)
    private static final int C_HEADER_BG  = col(20,  20,  20,  245);
    private static final int C_GREEN      = col(85,  255, 85,  255); // #55ff55
    private static final int C_GREEN_DIM  = col(85,  255, 85,  13);  // bg tint aktif
    private static final int C_WHITE      = col(255, 255, 255, 255);
    private static final int C_GRAY       = col(204, 204, 204, 255); // #cccccc setting label
    private static final int C_DARK_BG    = col(0,   0,   0,   77);  // settings bg
    private static final int C_HOVER      = col(255, 255, 255, 10);
    private static final int C_DIVIDER    = col(51,  51,  51,  255);
    private static final int C_ROW_DIV    = col(255, 255, 255, 8);
    // Checkbox
    private static final int C_CB_BORDER  = col(102, 102, 102, 255); // unchecked border
    private static final int C_CB_BG      = col(0,   0,   0,   128);
    private static final int C_CB_ON      = col(255, 170, 0,   255); // orange checked
    private static final int C_CB_ON_IN   = col(255, 248, 224, 255); // inner dot
    // Slider
    private static final int C_SL_TRACK   = col(51,  51,  51,  255);
    private static final int C_SL_HANDLE  = col(255, 119, 204, 255); // pink
    private static final int C_SL_VAL     = col(255, 119, 204, 255);
    private static final int C_KEYBIND_BG = col(255, 255, 255, 18);
    private static final int C_KEYBIND_BD = col(68,  68,  68,  255);
    private static final int C_ARROW      = col(136, 136, 136, 255);

    // ── Layout ────────────────────────────────────────────────────────────
    private static final int WIN_W    = 200;
    private static final int HDR_H    = 28;   // header Combat
    private static final int MOD_H    = 22;   // tinggi module header row
    private static final int ROW_H    = 20;   // tinggi setting row
    private static final int SLD_H    = 34;   // tinggi slider row
    private static final int PAD_L    = 10;   // left pad module
    private static final int PAD_S    = 18;   // left pad setting
    private static final int CB_SIZE  = 10;   // checkbox size
    private static final int SL_H     = 3;    // slider track height
    private static final int SL_TW    = 7;    // slider thumb width
    private static final int SL_TH    = 13;   // slider thumb height
    private static final int DOT      = 6;    // active dot size

    // ── State ─────────────────────────────────────────────────────────────
    private int winX = 60, winY = 40;
    private boolean dragging; int dragDX, dragDY;

    // Slider drag state
    private int dragSlider = -1; // index slider yang sedang di-drag
    private int dragSliderStartX;

    private final List<ModEntry> modules = new ArrayList<>();
    private final List<SliderDef> sliders = new ArrayList<>();

    // ── Data classes ──────────────────────────────────────────────────────
    static class BoolSetting {
        String label; boolean val;
        BoolSetting(String l, boolean v) { label=l; val=v; }
    }
    static class SliderDef {
        String label; float val; float min; float max; float spread;
        boolean range; // true = tampil "min - max", false = tampil nilai saja
        int guiY; // posisi Y saat render, diupdate tiap frame
        SliderDef(String l, float v, float mn, float mx, float sp, boolean r) {
            label=l; val=v; min=mn; max=mx; spread=sp; range=r;
        }
    }
    static class ModEntry {
        Module mod;
        List<Object> settings = new ArrayList<>(); // BoolSetting atau SliderDef atau String(keybind)
        boolean expanded;
        ModEntry(Module m, boolean exp) { mod=m; expanded=exp; }
    }

    public TiooGui() {
        super(Text.literal("Tioo"));

        // ── TriggerBot ────────────────────────────────────────────────────
        ModEntry tb = new ModEntry(TiooMod.triggerBot, true);
        tb.settings.add(new BoolSetting("Work In Screen",    false));
        tb.settings.add(new BoolSetting("While Use",         false));
        tb.settings.add(new BoolSetting("On Left Click",     false));
        tb.settings.add(new BoolSetting("All Items",         false));
        SliderDef swordSlider = new SliderDef("Sword Delay", 545, 0, 1000, 5, true);
        SliderDef axeSlider   = new SliderDef("Axe Delay",   790, 0, 1000, 10, true);
        tb.settings.add(swordSlider);
        tb.settings.add(axeSlider);
        sliders.add(swordSlider);
        sliders.add(axeSlider);
        tb.settings.add(new BoolSetting("Check Shield",      false));
        tb.settings.add(new BoolSetting("While Ascending",   false));
        tb.settings.add(new BoolSetting("Same Player",       false));
        tb.settings.add(new BoolSetting("Only Crit Sword",   false));
        tb.settings.add(new BoolSetting("Only Crit Axe",     false));
        tb.settings.add(new BoolSetting("Swing Hand",        true));
        tb.settings.add(new BoolSetting("Click Simulation",  false));
        tb.settings.add(new BoolSetting("Stray Bypass",      false));
        tb.settings.add(new BoolSetting("All Entities",      false));
        tb.settings.add(new BoolSetting("Use Shield",        false));
        SliderDef shieldTimeSlider = new SliderDef("Shield Time", 350, 100, 1000, 0, false);
        tb.settings.add(shieldTimeSlider);
        sliders.add(shieldTimeSlider);
        tb.settings.add("BACKSLASH"); // keybind (String = keybind display)
        modules.add(tb);

        // ── ShieldDisabler ────────────────────────────────────────────────
        ModEntry sd = new ModEntry(TiooMod.shieldDisabler, false);
        sd.settings.add(new BoolSetting("Switch Back",       true));
        sd.settings.add(new BoolSetting("Require Axe",       false));
        sd.settings.add(new BoolSetting("Stun",              false));
        sd.settings.add(new BoolSetting("Click Simulation",  false));
        SliderDef hitSlider    = new SliderDef("Hit Delay",    0, 0, 20, 0, false);
        SliderDef switchSlider = new SliderDef("Switch Delay", 0, 0, 20, 0, false);
        sd.settings.add(hitSlider);
        sd.settings.add(switchSlider);
        sliders.add(hitSlider);
        sliders.add(switchSlider);
        modules.add(sd);
    }

    // ── Total height of window ────────────────────────────────────────────
    private int totalH() {
        int h = HDR_H;
        for (ModEntry m : modules) {
            h += MOD_H;
            if (m.expanded) {
                for (Object s : m.settings) {
                    if (s instanceof SliderDef) h += SLD_H;
                    else h += ROW_H;
                }
            }
        }
        return h;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int winH = totalH();

        // ── Window BG ─────────────────────────────────────────────────────
        fill(ctx, winX, winY, winX+WIN_W, winY+winH, C_BG);
        // Outline
        fill(ctx, winX,          winY,          winX+WIN_W,   winY+1,     C_DIVIDER);
        fill(ctx, winX,          winY+winH-1,   winX+WIN_W,   winY+winH,  C_DIVIDER);
        fill(ctx, winX,          winY,          winX+1,        winY+winH,  C_DIVIDER);
        fill(ctx, winX+WIN_W-1,  winY,          winX+WIN_W,   winY+winH,  C_DIVIDER);

        // ── Header ────────────────────────────────────────────────────────
        fill(ctx, winX, winY, winX+WIN_W, winY+HDR_H, C_HEADER_BG);
        ctx.drawText(textRenderer, "Combat", winX+PAD_L, winY+8, C_WHITE, true);
        // Green underline
        fill(ctx, winX, winY+HDR_H-3, winX+WIN_W, winY+HDR_H, C_GREEN);

        // ── Modules ───────────────────────────────────────────────────────
        int curY = winY + HDR_H;

        for (int mi = 0; mi < modules.size(); mi++) {
            ModEntry m = modules.get(mi);
            boolean on = m.mod.isEnabled();

            // Active left border
            if (on) {
                fill(ctx, winX,   curY, winX+3, curY+MOD_H, C_GREEN);
                fill(ctx, winX+3, curY, winX+WIN_W, curY+MOD_H, C_GREEN_DIM);
            }

            // Hover
            if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+MOD_H)
                fill(ctx, winX, curY, winX+WIN_W, curY+MOD_H, C_HOVER);

            // Active dot
            int dotX = winX+PAD_L;
            int dotY = curY + MOD_H/2 - DOT/2;
            if (on) {
                fill(ctx, dotX, dotY, dotX+DOT, dotY+DOT, C_GREEN);
            } else {
                // Outline dot
                fill(ctx, dotX, dotY, dotX+DOT, dotY+1, C_CB_BORDER);
                fill(ctx, dotX, dotY+DOT-1, dotX+DOT, dotY+DOT, C_CB_BORDER);
                fill(ctx, dotX, dotY, dotX+1, dotY+DOT, C_CB_BORDER);
                fill(ctx, dotX+DOT-1, dotY, dotX+DOT, dotY+DOT, C_CB_BORDER);
            }

            // Module name
            ctx.drawText(textRenderer, m.mod.getName(), winX+PAD_L+DOT+5, curY+7, C_WHITE, true);

            // Arrow
            String arrow = m.expanded ? "v" : ">";
            ctx.drawText(textRenderer, arrow, winX+WIN_W-14, curY+7, C_ARROW, false);

            curY += MOD_H;

            // Divider antara module header dan settings
            if (!m.expanded && mi < modules.size()-1) {
                fill(ctx, winX+8, curY-1, winX+WIN_W-8, curY, C_DIVIDER);
            }

            // ── Settings ──────────────────────────────────────────────────
            if (m.expanded) {
                fill(ctx, winX, curY, winX+WIN_W, curY + settingsH(m), C_DARK_BG);

                for (Object s : m.settings) {
                    if (s instanceof BoolSetting bs) {
                        // Row hover
                        if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+ROW_H)
                            fill(ctx, winX, curY, winX+WIN_W, curY+ROW_H, C_HOVER);

                        // Label
                        ctx.drawText(textRenderer, bs.label, winX+PAD_S, curY+6, C_GRAY, false);

                        // Checkbox
                        int cbX = winX+WIN_W-PAD_S-CB_SIZE;
                        int cbY = curY + ROW_H/2 - CB_SIZE/2;
                        if (bs.val) {
                            fill(ctx, cbX, cbY, cbX+CB_SIZE, cbY+CB_SIZE, C_CB_ON);
                            // Inner dot
                            int id = 3;
                            fill(ctx, cbX+id, cbY+id, cbX+CB_SIZE-id, cbY+CB_SIZE-id, C_CB_ON_IN);
                        } else {
                            fill(ctx, cbX, cbY, cbX+CB_SIZE, cbY+CB_SIZE, C_CB_BG);
                            // Border
                            fill(ctx, cbX, cbY, cbX+CB_SIZE, cbY+1, C_CB_BORDER);
                            fill(ctx, cbX, cbY+CB_SIZE-1, cbX+CB_SIZE, cbY+CB_SIZE, C_CB_BORDER);
                            fill(ctx, cbX, cbY, cbX+1, cbY+CB_SIZE, C_CB_BORDER);
                            fill(ctx, cbX+CB_SIZE-1, cbY, cbX+CB_SIZE, cbY+CB_SIZE, C_CB_BORDER);
                        }
                        // Row divider
                        fill(ctx, winX, curY+ROW_H-1, winX+WIN_W, curY+ROW_H, C_ROW_DIV);
                        curY += ROW_H;

                    } else if (s instanceof SliderDef sd) {
                        sd.guiY = curY; // simpan Y untuk drag

                        // Label
                        ctx.drawText(textRenderer, sd.label, winX+PAD_S, curY+5, C_GRAY, false);

                        // Value text
                        String valStr;
                        if (sd.range) {
                            valStr = String.format("%.0f-%.0f", sd.val - sd.spread/2, sd.val + sd.spread/2);
                        } else {
                            valStr = String.format("%.0f", sd.val);
                        }
                        int vw = textRenderer.getWidth(valStr);
                        ctx.drawText(textRenderer, valStr, winX+WIN_W-PAD_S-vw, curY+5, C_SL_VAL, false);

                        // Track
                        int tX1 = winX+PAD_S;
                        int tX2 = winX+WIN_W-PAD_S;
                        int tY  = curY + SLD_H/2 + 4;
                        fill(ctx, tX1, tY, tX2, tY+SL_H, C_SL_TRACK);

                        // Thumb
                        float ratio = (sd.val - sd.min) / (sd.max - sd.min);
                        int tPos = tX1 + (int)(ratio * (tX2 - tX1 - SL_TW));
                        fill(ctx, tPos, tY - (SL_TH-SL_H)/2, tPos+SL_TW, tY + SL_H + (SL_TH-SL_H)/2, C_SL_HANDLE);

                        // Row divider
                        fill(ctx, winX, curY+SLD_H-1, winX+WIN_W, curY+SLD_H, C_ROW_DIV);
                        curY += SLD_H;

                    } else if (s instanceof String keybind) {
                        // Keybind row
                        ctx.drawText(textRenderer, "Keybind", winX+PAD_S, curY+6, C_GRAY, false);
                        int kbw = textRenderer.getWidth(keybind);
                        fill(ctx, winX+WIN_W-PAD_S-kbw-6, curY+4, winX+WIN_W-PAD_S+4, curY+ROW_H-4, C_KEYBIND_BG);
                        // border
                        int kbX1 = winX+WIN_W-PAD_S-kbw-6;
                        int kbY1 = curY+4;
                        int kbX2 = winX+WIN_W-PAD_S+4;
                        int kbY2 = curY+ROW_H-4;
                        fill(ctx, kbX1, kbY1, kbX2, kbY1+1, C_KEYBIND_BD);
                        fill(ctx, kbX1, kbY2-1, kbX2, kbY2, C_KEYBIND_BD);
                        fill(ctx, kbX1, kbY1, kbX1+1, kbY2, C_KEYBIND_BD);
                        fill(ctx, kbX2-1, kbY1, kbX2, kbY2, C_KEYBIND_BD);
                        ctx.drawText(textRenderer, keybind, winX+WIN_W-PAD_S-kbw-2, curY+6, C_ARROW, false);
                        fill(ctx, winX, curY+ROW_H-1, winX+WIN_W, curY+ROW_H, C_ROW_DIV);
                        curY += ROW_H;
                    }
                }

                // Divider antara module
                if (mi < modules.size()-1)
                    fill(ctx, winX+8, curY, winX+WIN_W-8, curY+1, C_DIVIDER);
            }
        }

        super.render(ctx, mx, my, delta);
    }

    private int settingsH(ModEntry m) {
        int h = 0;
        for (Object s : m.settings) h += (s instanceof SliderDef) ? SLD_H : ROW_H;
        return h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int curY = winY + HDR_H;

        // Drag window header
        if (mx>=winX && mx<=winX+WIN_W && my>=winY && my<=winY+HDR_H) {
            dragging=true; dragDX=(int)(mx-winX); dragDY=(int)(my-winY); return true;
        }

        for (ModEntry m : modules) {
            // Klik module header
            if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+MOD_H) {
                if (btn==0) m.expanded = !m.expanded;
                if (btn==1) m.mod.toggle();
                return true;
            }
            curY += MOD_H;

            if (m.expanded) {
                for (Object s : m.settings) {
                    int rowH = (s instanceof SliderDef) ? SLD_H : ROW_H;

                    if (s instanceof BoolSetting bs) {
                        // Klik checkbox
                        int cbX = winX+WIN_W-PAD_S-CB_SIZE;
                        int cbY = curY + ROW_H/2 - CB_SIZE/2;
                        if (mx>=cbX-4 && mx<=cbX+CB_SIZE+4 && my>=cbY-4 && my<=cbY+CB_SIZE+4) {
                            bs.val = !bs.val;
                            return true;
                        }
                        // Klik row juga toggle
                        if (mx>=winX && mx<=winX+WIN_W && my>=curY && my<=curY+ROW_H) {
                            bs.val = !bs.val;
                            return true;
                        }
                    } else if (s instanceof SliderDef sd) {
                        // Klik slider track — mulai drag
                        int tX1 = winX+PAD_S;
                        int tX2 = winX+WIN_W-PAD_S;
                        int tY  = curY + SLD_H/2 + 4;
                        if (mx>=tX1 && mx<=tX2 && my>=tY-8 && my<=tY+8) {
                            float ratio = (float)((mx - tX1) / (tX2 - tX1));
                            sd.val = sd.min + ratio * (sd.max - sd.min);
                            sd.val = Math.max(sd.min, Math.min(sd.max, sd.val));
                            dragSlider = sliders.indexOf(sd);
                            return true;
                        }
                    }
                    curY += rowH;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double ddx, double ddy) {
        if (dragging) {
            winX = (int)(mx - dragDX);
            winY = (int)(my - dragDY);
            return true;
        }
        if (dragSlider >= 0 && dragSlider < sliders.size()) {
            SliderDef sd = sliders.get(dragSlider);
            int tX1 = winX+PAD_S;
            int tX2 = winX+WIN_W-PAD_S;
            float ratio = (float)((mx - tX1) / (tX2 - tX1));
            sd.val = sd.min + ratio * (sd.max - sd.min);
            sd.val = Math.max(sd.min, Math.min(sd.max, sd.val));
            return true;
        }
        return super.mouseDragged(mx, my, btn, ddx, ddy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging = false;
        dragSlider = -1;
        return super.mouseReleased(mx, my, btn);
    }

    @Override public boolean shouldPause()      { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }

    private void fill(DrawContext c, int x1, int y1, int x2, int y2, int color) {
        if (x2 > x1 && y2 > y1) c.fill(x1, y1, x2, y2, color);
    }

    private static int col(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
