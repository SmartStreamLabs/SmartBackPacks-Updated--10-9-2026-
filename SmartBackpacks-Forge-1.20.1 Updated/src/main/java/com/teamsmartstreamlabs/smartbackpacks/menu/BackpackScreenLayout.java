package com.teamsmartstreamlabs.smartbackpacks.menu;

public final class BackpackScreenLayout {
    public static final int SLOT_SIZE = 18;
    public static final int BACKPACK_COLUMNS = 9;
    public static final int MAX_VISIBLE_BACKPACK_ROWS = 6;

    public static final int UPGRADE_PANEL_WIDTH = 52;
    public static final int MAIN_PANEL_WIDTH = 250;
    public static final int IMAGE_WIDTH = UPGRADE_PANEL_WIDTH + MAIN_PANEL_WIDTH;

    public static final int TOP_BAR_HEIGHT = 28;
    public static final int BACKPACK_SLOT_X = UPGRADE_PANEL_WIDTH + 8;
    public static final int BACKPACK_SLOT_Y = 32;

    public static final int UPGRADE_SLOT_X = 26;
    public static final int UPGRADE_SLOT_Y = 34;
    public static final int UPGRADE_SLOT_STEP_Y = SLOT_SIZE;
    public static final int UPGRADE_TOGGLE_X = 9;
    public static final int UPGRADE_TOGGLE_SIZE = 8;
    public static final int TRASH_PANEL_X = IMAGE_WIDTH - 56;
    public static final int TRASH_PANEL_Y = BACKPACK_SLOT_Y + 3;
    public static final int TRASH_PANEL_WIDTH = 48;
    public static final int TRASH_PANEL_HEIGHT = 98;
    public static final int TRASH_SLOT_X = TRASH_PANEL_X + 15;
    public static final int TRASH_SLOT_Y = TRASH_PANEL_Y + 28;
    public static final int TRASH_BUTTON_X = TRASH_PANEL_X + 6;
    public static final int TRASH_RESTORE_BUTTON_Y = TRASH_SLOT_Y + 24;
    public static final int TRASH_DELETE_BUTTON_Y = TRASH_SLOT_Y + 44;
    public static final int TRASH_BUTTON_WIDTH = 36;
    public static final int TRASH_BUTTON_HEIGHT = 16;

    public static final int PLAYER_INVENTORY_X = BACKPACK_SLOT_X;
    public static final int PLAYER_INVENTORY_GAP_Y = 22;
    public static final int PLAYER_HOTBAR_OFFSET_Y = 58;

    private BackpackScreenLayout() {
    }

    public static int playerInventoryY(int visibleBackpackRows) {
        return BACKPACK_SLOT_Y + visibleBackpackRows * SLOT_SIZE + PLAYER_INVENTORY_GAP_Y;
    }

    public static int inventoryLabelY(int visibleBackpackRows) {
        return playerInventoryY(visibleBackpackRows) - 11;
    }

    public static int imageHeight(int visibleBackpackRows, int upgradeSlotCount) {
        int playerInventoryBottom = playerInventoryY(visibleBackpackRows) + PLAYER_HOTBAR_OFFSET_Y + SLOT_SIZE;
        int upgradePanelBottom = UPGRADE_SLOT_Y + upgradeSlotCount * UPGRADE_SLOT_STEP_Y + 8;
        return Math.max(playerInventoryBottom, upgradePanelBottom);
    }
}
