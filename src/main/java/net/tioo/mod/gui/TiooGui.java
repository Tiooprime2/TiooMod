package net.tioo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.tioo.mod.TiooMod;
import net.tioo.mod.modules.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * TiooGui — Modern dark-themed ClickGUI
 *
 * Scroll fixes:
 * 1. Scroll Consumption    — submenu scroll tidak bubble ke parent
 * 2. isHovered check       — scrollVelocity parent di-zero saat cursor di submenu
 * 3. Scissor Box           — RenderSystem.enableScissor untuk submenu
 * 4. Dynamic Height        — disable ghost scroll kalau content muat semua
 */
public class TiooGui extends Screen {

    // ─── Theme Colors ─────────────────────────────────────────────────────
    private static final int COLOR_BG          = rgba(10,  10,  16,  240);
    private static final int COLOR_HEADER      = rgba(16,  16,  26,  255);
    private static final int COLOR_ACCENT      = rgba(80,  140, 255, 255);
    private static final int COLOR_ACCENT_DARK = rgba(40,  80,  180, 255);
    private static final int COLOR_BTN_OFF     = rgba(22,  22,  35,  255);
    private static final int COLOR_BTN_ON      = rgba(20,  55,  25,  255);
    private static final int COLOR_BTN_BORDER  = rgba(40,  40,  65,  255);
    private static final int COLOR_BTN_ACTIVE  = rgba(50,  210, 120, 255);
    private static final int COLOR_TEXT        = rgba(235, 235, 245, 255);
    private static final int COLOR_TEXT_MUTED  = rgba(130, 130, 160, 255);
    private static final int COLOR_SCROLLBAR   = rgba(60,  60,  90,  180);
    private static final int COLOR_THUMB       = rgba(80,  140, 255, 200);

    // ─── Window size ──────────────────────────────────────────────────────
    private static final int WIN_W      = 200;
    private static final int WIN_H      = 260;
    private static final int HEADER_H   = 20;
    private static final int BTN_H      = 40;
    private static final int BTN_PAD    = 4;
    private static final int SUBM_ITEM_H = 16;

    // ─── Window position ──────────────────────────────────────────────────
    private int winX = 100, winY = 60;
    private boolean dragging    = false;
    private int     dragOffsetX = 0;
    private int     dragOffsetY = 0;

    // ─── Module buttons ───────────────────────────────────────────────────
    private final List<ModuleEntry> entries = new ArrayList<>();

    // ─── Window scroll ────────────────────────────────────────────────────
    private double scrollOffset   = 0;
    private double scrollVelocity = 0;

    // ─── Constructor ──────────────────────────────────────────────────────
    public TiooGui() {
        super(Text.literal("Tioo"));
        buildEntries();
    }

    private void buildEntries() {
        entries.clear();
        entries.add(new ModuleEntry(TiooMod.triggerBot,     buildTriggerBotSettings()));
        entries.add(new ModuleEntry(TiooMod.shieldDisabler, buildShieldDisablerSettings()));
    }

    private List<SettingEntry> buildTriggerBotSettings() {
        List<SettingEntry> s = new ArrayList<>();
        s.add(new BooleanSetting("All Entities",  "Attack mobs too",
            TiooMod.triggerBot.isAllEntities(),
            v -> TiooMod.triggerBot.setAllEntities(v)));
        s.add(new BooleanSetting("While Use",     "Trigger while eating",
            TiooMod.triggerBot.isWhileUse(),
            v -> TiooMod.triggerBot.setWhileUse(v)));
        s.add(new BooleanSetting("Swing Hand",    "Swing animation",
            TiooMod.triggerBot.isSwingHand(),
            v -> TiooMod.triggerBot.setSwingHand(v)));
        s.add(new BooleanSetting("While Ascending","Skip upward jump",
            TiooMod.triggerBot.isOnlyOnGround(),
            v -> TiooMod.triggerBot.setOnlyOnGround(v)));
        return s;
    }

    private List<SettingEntry> buildShieldDisablerSettings() {
        List<SettingEntry> s = new ArrayList<>();
        s.add(new BooleanSetting("Switch Back",   "Return to prev slot",
            TiooMod.shieldDisabler.isSwitchBack(),
            v -> TiooMod.shieldDisabler.setSwitchBack(v)));
        s.add(new BooleanSetting("Require Axe",   "Must hold axe",
            TiooMod.shieldDisabler.isRequireHoldAxe(),
            v -> TiooMod.shieldDisabler.setRequireHoldAxe(v)));
        return s;
    }

    // ─── RENDER ───────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Update scroll
        scrollOffset   += scrollVelocity;
        scrollVelocity *= 0.82;
        if (Math.abs(scrollVelocity) < 0.01) scrollVelocity = 0;

        // Clamp scroll
        int totalH    = getTotalContentHeight();
        int visibleH  = WIN_H - HEADER_H;
        double maxScroll = Math.max(0, totalH - visibleH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // ── Window background ──
        drawRect(ctx, winX, winY, winX + WIN_W, winY + WIN_H, COLOR_BG);
        // Border
        drawBorder(ctx, winX, winY, WIN_W, WIN_H, COLOR_BTN_BORDER);
        // Header
        drawRect(ctx, winX, winY, winX + WIN_W, winY + HEADER_H, COLOR_HEADER);
        // Accent line top
        drawRect(ctx, winX + WIN_W/4, winY, winX + WIN_W*3/4, winY + 1, COLOR_ACCENT);
        // Title
        ctx.drawText(textRenderer, "✦ Tioo", winX + 6, winY + 6, COLOR_TEXT, false);
        // Version
        ctx.drawText(textRenderer, "v1.0", winX + WIN_W - 24, winY + 6, COLOR_TEXT_MUTED, false);

        // ── Scissor untuk content area ──────────────────────────────────────
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int sy  = (int)(MinecraftClient.getInstance().getWindow().getHeight()
                  - (winY + WIN_H) * scale);

        RenderSystem.enableScissor(
            (int)(winX * scale),
            (int)(sy),
            (int)(WIN_W * scale),
            (int)((WIN_H - HEADER_H) * scale)
        );

        // ── Render entries ──────────────────────────────────────────────────
        int y = winY + HEADER_H + BTN_PAD - (int) scrollOffset;
        for (ModuleEntry entry : entries) {
            renderEntry(ctx, entry, winX + BTN_PAD, y, mouseX, mouseY, delta);
            y += BTN_H + BTN_PAD;
            if (entry.expanded) {
                int subH = getSubmenuHeight(entry);
                y += subH + BTN_PAD;
            }
        }

        RenderSystem.disableScissor();

        // ── Scrollbar ──────────────────────────────────────────────────────
        if (totalH > visibleH) {
            renderScrollbar(ctx, totalH, visibleH);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderEntry(DrawContext ctx, ModuleEntry entry,
                             int x, int y, int mouseX, int mouseY, float delta) {
        int w    = WIN_W - BTN_PAD * 2;
        boolean on = entry.module.isEnabled();

        // Background card
        int bgColor = on ? COLOR_BTN_ON : COLOR_BTN_OFF;
        drawRect(ctx, x, y, x + w, y + BTN_H, bgColor);
        drawBorder(ctx, x, y, w, BTN_H, on ? COLOR_BTN_ACTIVE : COLOR_BTN_BORDER);

        // Accent bar kiri
        drawRect(ctx, x, y + 4, x + 3, y + BTN_H - 4,
            on ? COLOR_BTN_ACTIVE : COLOR_TEXT_MUTED);

        // Module name
        ctx.drawText(textRenderer, entry.module.getName(),
            x + 8, y + 6, COLOR_TEXT, false);

        // Description
        ctx.drawText(textRenderer, entry.module.getDescription(),
            x + 8, y + 18, COLOR_TEXT_MUTED, false);

        // Badge ON/OFF
        String badge    = on ? "ON" : "OFF";
        int badgeColor  = on ? COLOR_BTN_ACTIVE : COLOR_TEXT_MUTED;
        int badgeX      = x + w - 26;
        int badgeY      = y + BTN_H/2 - 5;
        drawRect(ctx, badgeX, badgeY, badgeX + 22, badgeY + 11,
            on ? rgba(20,60,25,200) : rgba(30,30,45,200));
        ctx.drawText(textRenderer, badge, badgeX + 3, badgeY + 2, badgeColor, false);

        // Arrow expand/collapse
        String arrow = entry.expanded ? "▲" : "▼";
        ctx.drawText(textRenderer, arrow, x + w - 12, y + 6, COLOR_TEXT_MUTED, false);

        // Render submenu kalau expanded
        if (entry.expanded) {
            renderSubmenu(ctx, entry, x, y + BTN_H, w, mouseX, mouseY);
        }
    }

    private void renderSubmenu(DrawContext ctx, ModuleEntry entry,
                               int x, int y, int w, int mouseX, int mouseY) {
        int rawH     = entry.settings.size() * SUBM_ITEM_H + 4;
        int visibleH = Math.min(rawH, 80);

        // Background submenu
        drawRect(ctx, x, y, x + w, y + visibleH, rgba(14, 14, 22, 240));
        drawBorder(ctx, x, y, w, visibleH, COLOR_BTN_BORDER);

        // ── FIX 3: SCISSOR untuk submenu ──────────────────────────────────
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int subSy = (int)(MinecraftClient.getInstance().getWindow().getHeight()
                    - (y + visibleH) * scale);

        RenderSystem.enableScissor(
            (int)(x * scale),
            subSy,
            (int)(w * scale),
            (int)(visibleH * scale)
        );

        // Render settings dengan scroll
        int sy2 = y + 2 - (int) entry.submenuScroll;
        for (SettingEntry setting : entry.settings) {
            renderSetting(ctx, setting, x + 4, sy2, w - 8, mouseX, mouseY);
            sy2 += SUBM_ITEM_H;
        }

        RenderSystem.disableScissor();

        // Scrollbar submenu kalau perlu
        if (rawH > 80) {
            renderSubmenuScrollbar(ctx, entry, x + w - 3, y, visibleH, rawH);
        }

        // Apply submenu scroll velocity
        entry.submenuScroll   += entry.submenuScrollVelocity;
        entry.submenuScrollVelocity *= 0.82;
        if (Math.abs(entry.submenuScrollVelocity) < 0.01) entry.submenuScrollVelocity = 0;
        double maxS = Math.max(0, rawH - 80);
        entry.submenuScroll = Math.max(0, Math.min(entry.submenuScroll, maxS));
    }

    private void renderSetting(DrawContext ctx, SettingEntry setting,
                               int x, int y, int w, int mouseX, int mouseY) {
        // Hover highlight
        if (mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+SUBM_ITEM_H) {
            drawRect(ctx, x, y, x+w, y+SUBM_ITEM_H, rgba(255,255,255,15));
        }

        if (setting instanceof BooleanSetting bs) {
            ctx.drawText(textRenderer, bs.label,     x + 2, y + 4, COLOR_TEXT,       false);
            ctx.drawText(textRenderer, bs.value ? "✓" : "✗",
                x + w - 10, y + 4,
                bs.value ? COLOR_BTN_ACTIVE : rgba(200, 70, 70, 255), false);
        }
    }

    private void renderScrollbar(DrawContext ctx, int totalH, int visibleH) {
        int sbX  = winX + WIN_W - 3;
        int sbY  = winY + HEADER_H;
        int sbH  = WIN_H - HEADER_H;
        float tr = (float) visibleH / totalH;
        int th   = Math.max(16, (int)(sbH * tr));
        float sr = (float)(scrollOffset / Math.max(1, totalH - visibleH));
        int ty   = sbY + (int)((sbH - th) * sr);

        drawRect(ctx, sbX, sbY, sbX + 3, sbY + sbH, COLOR_SCROLLBAR);
        drawRect(ctx, sbX, ty,  sbX + 3, ty + th,   COLOR_THUMB);
    }

    private void renderSubmenuScrollbar(DrawContext ctx, ModuleEntry entry,
                                        int x, int y, int visibleH, int totalH) {
        float tr = (float) visibleH / totalH;
        int th   = Math.max(8, (int)(visibleH * tr));
        float sr = (float)(entry.submenuScroll / Math.max(1, totalH - visibleH));
        int ty   = y + (int)((visibleH - th) * sr);

        drawRect(ctx, x, y,  x + 3, y + visibleH, rgba(30,30,50,150));
        drawRect(ctx, x, ty, x + 3, ty + th,       rgba(80,140,255,160));
    }

    // ─── MOUSE EVENTS ─────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horiz, double vert) {

        // ── FIX 1 & 2: Cek submenu scroll dulu ────────────────────────────
        int y = winY + HEADER_H + BTN_PAD - (int) scrollOffset;
        for (ModuleEntry entry : entries) {
            int entryBottom = y + BTN_H;
            if (entry.expanded) {
                int subH = getSubmenuHeight(entry);
                int subY = entryBottom;

                // FIX 2: isHovered check — cursor di submenu?
                if (mouseX >= winX + BTN_PAD && mouseX <= winX + WIN_W - BTN_PAD
                 && mouseY >= subY && mouseY <= subY + subH) {

                    // FIX 4: Cek dynamic height
                    int rawH = entry.settings.size() * SUBM_ITEM_H + 4;
                    if (rawH <= 80) {
                        // Content muat semua — no ghost scroll
                        entry.submenuScrollVelocity = 0;
                        entry.submenuScroll         = 0;
                    } else {
                        // FIX 1: CONSUME scroll — apply ke submenu saja
                        scrollVelocity = 0;  // stop parent!
                        entry.submenuScrollVelocity -= vert * 8;
                    }
                    return true; // consumed
                }
                entryBottom += subH + BTN_PAD;
            }
            y = entryBottom + BTN_PAD;
        }

        // Normal window scroll
        if (mouseX >= winX && mouseX <= winX + WIN_W
         && mouseY >= winY && mouseY <= winY + WIN_H) {

            // FIX 4: Ghost scroll prevention
            int totalH   = getTotalContentHeight();
            int visibleH = WIN_H - HEADER_H;
            if (totalH <= visibleH) {
                scrollVelocity = 0;
                scrollOffset   = 0;
                return true;
            }

            scrollVelocity -= vert * 10;
        }

        return super.mouseScrolled(mouseX, mouseY, horiz, vert);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Drag header
        if (mouseX >= winX && mouseX <= winX + WIN_W
         && mouseY >= winY && mouseY <= winY + HEADER_H) {
            dragging    = true;
            dragOffsetX = (int)(mouseX - winX);
            dragOffsetY = (int)(mouseY - winY);
            return true;
        }

        // Click entries
        int y = winY + HEADER_H + BTN_PAD - (int) scrollOffset;
        for (ModuleEntry entry : entries) {
            int x = winX + BTN_PAD;
            int w = WIN_W - BTN_PAD * 2;

            if (mouseX >= x && mouseX <= x + w
             && mouseY >= y && mouseY <= y + BTN_H) {
                if (button == 0) {
                    entry.module.toggle();
                } else if (button == 1) {
                    entry.expanded = !entry.expanded;
                    entry.submenuScroll = 0;
                }
                return true;
            }
            y += BTN_H + BTN_PAD;

            if (entry.expanded) {
                int subH = getSubmenuHeight(entry);
                // Click settings
                int sy2 = y - (int) entry.submenuScroll;
                for (SettingEntry setting : entry.settings) {
                    if (mouseX >= x+4 && mouseX <= x+w-4
                     && mouseY >= sy2 && mouseY <= sy2 + SUBM_ITEM_H) {
                        if (setting instanceof BooleanSetting bs) {
                            bs.value = !bs.value;
                            bs.onChange.accept(bs.value);
                        }
                        return true;
                    }
                    sy2 += SUBM_ITEM_H;
                }
                y += subH + BTN_PAD;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double deltaX, double deltaY) {
        if (dragging) {
            winX = (int)(mouseX - dragOffsetX);
            winY = (int)(mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    // ─── HELPERS ──────────────────────────────────────────────────────────
    private int getTotalContentHeight() {
        int total = 0;
        for (ModuleEntry entry : entries) {
            total += BTN_H + BTN_PAD;
            if (entry.expanded) {
                total += getSubmenuHeight(entry) + BTN_PAD;
            }
        }
        return total;
    }

    private int getSubmenuHeight(ModuleEntry entry) {
        int rawH = entry.settings.size() * SUBM_ITEM_H + 4;
        return Math.min(rawH, 80);
    }

    private void drawRect(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y2, color);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,     y,     x+w,   y+1,   color); // top
        ctx.fill(x,     y+h-1, x+w,   y+h,   color); // bottom
        ctx.fill(x,     y,     x+1,   y+h,   color); // left
        ctx.fill(x+w-1, y,     x+w,   y+h,   color); // right
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ─── INNER CLASSES ────────────────────────────────────────────────────
    private static class ModuleEntry {
        Module             module;
        List<SettingEntry> settings;
        boolean            expanded           = false;
        double             submenuScroll      = 0;
        double             submenuScrollVelocity = 0;

        ModuleEntry(Module module, List<SettingEntry> settings) {
            this.module   = module;
            this.settings = settings;
        }
    }

    private static abstract class SettingEntry {
        String label;
        SettingEntry(String label) { this.label = label; }
    }

    private static class BooleanSetting extends SettingEntry {
        boolean                          value;
        java.util.function.Consumer<Boolean> onChange;

        BooleanSetting(String label, String desc, boolean value,
                       java.util.function.Consumer<Boolean> onChange) {
            super(label);
            this.value    = value;
            this.onChange = onChange;
        }
    }
}
