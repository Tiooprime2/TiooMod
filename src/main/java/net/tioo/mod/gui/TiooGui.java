package net.tioo.mod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.tioo.mod.TiooMod;
import net.tioo.mod.modules.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * TiooGui — Fix blur untuk 1.21.4
 *
 * Di 1.21.4 RenderSystem.setProjectionMatrix API berubah
 * Tidak bisa pakai VertexSorter lagi
 * Fix blur: pakai ctx.getMatrices().push/pop + scale manual
 */
public class TiooGui extends Screen {

    // Theme
    private static final int C_BG     = col(10,  10,  16,  235);
    private static final int C_HEADER = col(16,  16,  26,  255);
    private static final int C_ACCENT = col(80,  140, 255, 255);
    private static final int C_OFF    = col(22,  22,  35,  255);
    private static final int C_ON     = col(20,  55,  25,  255);
    private static final int C_BORDER = col(40,  40,  65,  255);
    private static final int C_GREEN  = col(50,  210, 120, 255);
    private static final int C_TEXT   = col(235, 235, 245, 255);
    private static final int C_MUTED  = col(130, 130, 160, 255);
    private static final int C_THUMB  = col(80,  140, 255, 200);
    private static final int C_SCRBG  = col(40,  40,  60,  180);

    // Sizes — dalam scaled coords biasa (bukan framebuffer)
    private static final int WIN_W = 195;
    private static final int WIN_H = 260;
    private static final int HDR_H = 18;
    private static final int BTN_H = 38;
    private static final int PAD   = 4;
    private static final int SUB_H = 15;

    // State
    private int winX = 80, winY = 50;
    private boolean drag; int dx, dy;
    private double scrOff, scrVel;
    private final List<Entry> entries = new ArrayList<>();

    public TiooGui() {
        super(Text.literal("Tioo"));
        entries.add(new Entry(TiooMod.triggerBot,     buildTB()));
        entries.add(new Entry(TiooMod.shieldDisabler, buildSD()));
    }

    private List<BS> buildTB() {
        List<BS> s = new ArrayList<>();
        s.add(new BS("All Entities",    TiooMod.triggerBot.isAllEntities(),   v -> TiooMod.triggerBot.setAllEntities(v)));
        s.add(new BS("Swing Hand",      TiooMod.triggerBot.isSwingHand(),     v -> TiooMod.triggerBot.setSwingHand(v)));
        s.add(new BS("Check Shield",    TiooMod.triggerBot.isCheckShield(),   v -> TiooMod.triggerBot.setCheckShield(v)));
        s.add(new BS("Only Crit Sword", TiooMod.triggerBot.isOnlyCritSword(),v -> TiooMod.triggerBot.setOnlyCritSword(v)));
        s.add(new BS("Only Crit Axe",   TiooMod.triggerBot.isOnlyCritAxe(),  v -> TiooMod.triggerBot.setOnlyCritAxe(v)));
        s.add(new BS("While Ascending", TiooMod.triggerBot.isWhileAscend(),   v -> TiooMod.triggerBot.setWhileAscend(v)));
        return s;
    }

    private List<BS> buildSD() {
        List<BS> s = new ArrayList<>();
        s.add(new BS("Switch Back", TiooMod.shieldDisabler.isSwitchBack(),     v -> TiooMod.shieldDisabler.setSwitchBack(v)));
        s.add(new BS("Require Axe", TiooMod.shieldDisabler.isRequireHoldAxe(),v -> TiooMod.shieldDisabler.setRequireHoldAxe(v)));
        return s;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Scroll update
        scrOff += scrVel; scrVel *= 0.80;
        if (Math.abs(scrVel) < 0.01) scrVel = 0;
        int totH = totH(), visH = WIN_H - HDR_H;
        scrOff = Math.max(0, Math.min(scrOff, Math.max(0, totH - visH)));

        // Window BG + header
        fill(ctx, winX, winY, winX+WIN_W, winY+WIN_H, C_BG);
        border(ctx, winX, winY, WIN_W, WIN_H, C_BORDER);
        fill(ctx, winX, winY, winX+WIN_W, winY+HDR_H, C_HEADER);
        fill(ctx, winX+WIN_W/4, winY, winX+WIN_W*3/4, winY+1, C_ACCENT);
        ctx.drawText(textRenderer, "✦ Tioo", winX+6, winY+5, C_TEXT, false);
        ctx.drawText(textRenderer, "v1.0",   winX+WIN_W-24, winY+5, C_MUTED, false);

        // Entries
        int y = winY + HDR_H + PAD - (int)scrOff;
        for (Entry e : entries) {
            if (y+BTN_H > winY+HDR_H && y < winY+WIN_H)
                renderBtn(ctx, e, winX+PAD, y, mouseX, mouseY);
            y += BTN_H + PAD;
            if (e.exp) {
                int sh = subH(e);
                if (y > winY+HDR_H && y < winY+WIN_H)
                    renderSub(ctx, e, winX+PAD, y, mouseX, mouseY);
                y += sh + PAD;
            }
        }

        // Scrollbar
        if (totH > visH) {
            int sbx=winX+WIN_W-4, sby=winY+HDR_H, sbh=visH;
            int th = Math.max(14, (int)(sbh*(float)visH/totH));
            int ty = sby+(int)((sbh-th)*(float)(scrOff/Math.max(1,totH-visH)));
            fill(ctx, sbx, sby, sbx+3, sby+sbh, C_SCRBG);
            fill(ctx, sbx, ty,  sbx+3, ty+th,   C_THUMB);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderBtn(DrawContext ctx, Entry e, int x, int y, int mx, int my) {
        boolean on = e.mod.isEnabled();
        int w = WIN_W-PAD*2;
        fill(ctx, x, y, x+w, y+BTN_H, on ? C_ON : C_OFF);
        border(ctx, x, y, w, BTN_H, on ? C_GREEN : C_BORDER);
        fill(ctx, x, y+4, x+3, y+BTN_H-4, on ? C_GREEN : C_MUTED);
        ctx.drawText(textRenderer, e.mod.getName(),        x+7, y+5,  C_TEXT,  false);
        ctx.drawText(textRenderer, e.mod.getDescription(), x+7, y+16, C_MUTED, false);
        int bx=x+w-26, by=y+BTN_H/2-5;
        fill(ctx, bx, by, bx+22, by+11, on ? col(20,60,25,200) : col(30,30,45,200));
        ctx.drawText(textRenderer, on?"ON":"OFF", bx+2, by+2, on ? C_GREEN : C_MUTED, false);
        ctx.drawText(textRenderer, e.exp?"▲":"▼", x+w-12, y+5, C_MUTED, false);
    }

    private void renderSub(DrawContext ctx, Entry e, int x, int y, int mx, int my) {
        int w=WIN_W-PAD*2, h=subH(e);
        fill(ctx, x, y, x+w, y+h, col(14,14,22,245));
        border(ctx, x, y, w, h, C_BORDER);
        int sy = y+2-(int)e.subScr;
        for (BS s : e.settings) {
            if (sy+SUB_H > y && sy < y+h) {
                if (mx>=x && mx<=x+w && my>=sy && my<=sy+SUB_H)
                    fill(ctx, x, sy, x+w, sy+SUB_H, col(255,255,255,15));
                ctx.drawText(textRenderer, s.label,         x+5,   sy+3, C_TEXT, false);
                ctx.drawText(textRenderer, s.val?"✓":"✗", x+w-12, sy+3,
                    s.val ? C_GREEN : col(200,70,70,255), false);
            }
            sy += SUB_H;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        int y = winY+HDR_H+PAD-(int)scrOff;
        for (Entry e : entries) {
            y += BTN_H+PAD;
            if (e.exp) {
                int sh = subH(e);
                if (mx>=winX+PAD && mx<=winX+WIN_W-PAD && my>=y && my<=y+sh) {
                    int rawH = e.settings.size()*SUB_H+4;
                    if (rawH > 80) {
                        scrVel = 0;
                        e.subScr = Math.max(0, Math.min(e.subScr-v*6, rawH-80));
                    }
                    return true;
                }
                y += sh+PAD;
            }
        }
        if (mx>=winX && mx<=winX+WIN_W && my>=winY && my<=winY+WIN_H)
            if (totH() > WIN_H-HDR_H) scrVel -= v*8;
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (mx>=winX && mx<=winX+WIN_W && my>=winY && my<=winY+HDR_H) {
            drag=true; dx=(int)(mx-winX); dy=(int)(my-winY); return true;
        }
        int y = winY+HDR_H+PAD-(int)scrOff;
        for (Entry e : entries) {
            int x=winX+PAD, w=WIN_W-PAD*2;
            if (mx>=x && mx<=x+w && my>=y && my<=y+BTN_H) {
                if (btn==0) e.mod.toggle();
                else if (btn==1) { e.exp=!e.exp; e.subScr=0; }
                return true;
            }
            y += BTN_H+PAD;
            if (e.exp) {
                int sy = y-(int)e.subScr;
                for (BS s : e.settings) {
                    if (mx>=x+4 && mx<=x+w-4 && my>=sy && my<=sy+SUB_H) {
                        s.val=!s.val; s.onChange.accept(s.val); return true;
                    }
                    sy += SUB_H;
                }
                y += subH(e)+PAD;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double ddx, double ddy) {
        if (drag) { winX=(int)(mx-dx); winY=(int)(my-dy); return true; }
        return super.mouseDragged(mx, my, btn, ddx, ddy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        drag=false; return super.mouseReleased(mx,my,btn);
    }

    @Override public boolean shouldPause()      { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }

    private int totH() {
        int t=0;
        for (Entry e : entries) { t+=BTN_H+PAD; if(e.exp) t+=subH(e)+PAD; }
        return t;
    }
    private int subH(Entry e) { return Math.min(e.settings.size()*SUB_H+4, 80); }
    private void fill(DrawContext c,int x1,int y1,int x2,int y2,int color) { c.fill(x1,y1,x2,y2,color); }
    private void border(DrawContext c,int x,int y,int w,int h,int col) {
        c.fill(x,y,x+w,y+1,col); c.fill(x,y+h-1,x+w,y+h,col);
        c.fill(x,y,x+1,y+h,col); c.fill(x+w-1,y,x+w,y+h,col);
    }
    private static int col(int r,int g,int b,int a) { return (a<<24)|(r<<16)|(g<<8)|b; }

    private static class Entry {
        Module mod; List<BS> settings; boolean exp; double subScr;
        Entry(Module m, List<BS> s) { mod=m; settings=s; }
    }
    private static class BS {
        String label; boolean val; java.util.function.Consumer<Boolean> onChange;
        BS(String l, boolean v, java.util.function.Consumer<Boolean> c) { label=l; val=v; onChange=c; }
    }
}
