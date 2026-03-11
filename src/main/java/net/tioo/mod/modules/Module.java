package net.tioo.mod.modules;

public abstract class Module {

    private final String name;
    private final String description;
    private boolean enabled = false;

    public Module(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else         onDisable();
    }

    public void onEnable()  {}
    public void onDisable() {}

    public String  getName()        { return name; }
    public String  getDescription() { return description; }
    public boolean isEnabled()      { return enabled; }
    public void    setEnabled(boolean v) {
        if (v != enabled) toggle();
    }
}
