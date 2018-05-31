/*
 * Copyright 2017 Kaidan Gustave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.mhghapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Simple encapsulation of TrayIcon API
 * instances for MHGH App.
 *
 * @author Kaidan Gustave
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "MagicConstant"})
public final class TrayManager
{
    public static final TrayManager INSTANCE = new TrayManager();

    private static final Logger LOG = LoggerFactory.getLogger(TrayManager.class);
    private static final String ICON_RESOURCE = "/images/icon.jpg";
    private static final String TOOL_TIP = "MHGH App";

    private final SystemTray tray;
    private final Image trayImage;
    private final TrayIcon trayIcon;
    private final PopupMenu popupMenu;

    private Runnable onOpenPopupClicked = () -> {};
    private Runnable onExitPopupClicked = () -> {};

    private boolean notifying = false;
    private boolean hidden = true;

    private TrayManager()
    {
        SystemTray tray = null;
        Image trayImage = null;
        TrayIcon trayIcon = null;
        PopupMenu popupMenu = null;

        try
        {
            if(SystemTray.isSupported())
            {
                tray = SystemTray.getSystemTray();
                trayImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource(ICON_RESOURCE));
                trayIcon = new TrayIcon(trayImage, TOOL_TIP);
                popupMenu = new PopupMenu();
            }
            else
            {
                LOG.debug("System tray not supported, creating empty tray manager!");
            }
        }
        catch(Throwable t)
        {
            LOG.error("Encountered an error while starting system tray!", t);
            tray = null;
            trayImage = null;
            trayIcon = null;
            popupMenu = null;
        }

        this.tray = tray;
        this.trayImage = trayImage;
        this.trayIcon = trayIcon;
        this.popupMenu = popupMenu;

        afterInitializing();
    }

    public boolean isSupported()
    {
        return tray != null;
    }

    public boolean isNotifying()
    {
        return notifying;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setNotifying(boolean notifying)
    {
        this.notifying = notifying;
    }

    public void setOnOpenPopupClicked(Runnable onOpenPopupClicked)
    {
        this.onOpenPopupClicked = onOpenPopupClicked;
    }

    public void setOnExitPopupClicked(Runnable onExitPopupClicked)
    {
        this.onExitPopupClicked = onExitPopupClicked;
    }

    public void showTrayIcon()
    {
        if(isSupported())
        {
            if(isHidden())
            {
                try
                {
                    tray.add(trayIcon);
                    hidden = false;
                }
                catch(AWTException e)
                {
                    logAwtErrorWhile("adding TrayIcon to SystemTray", e);
                }
            }
        }
    }

    public void hideTrayIcon()
    {
        if(isSupported())
        {
            if(!isHidden())
            {
                tray.remove(trayIcon);
                hidden = true;
            }
        }
    }

    public void sendInfo(String message)
    {
        sendInfo(TOOL_TIP, message);
    }

    public void sendInfo(String caption, String message)
    {
        send(caption, message, TrayIcon.MessageType.INFO);
    }

    public void sendError(String message)
    {
        sendError(TOOL_TIP, message);
    }

    public void sendError(String caption, String message)
    {
        send(caption, message, TrayIcon.MessageType.ERROR);
    }

    private void afterInitializing()
    {
        if(isSupported())
        {
            trayIcon.setImage(trayImage);
            trayIcon.setToolTip(TOOL_TIP);

            // set up popup menu
            popupMenu.add(createOpenMenuItem());
            popupMenu.add(createExitMenuItem());
            popupMenu.setName(TOOL_TIP);
            popupMenu.setLabel(TOOL_TIP);
            trayIcon.setPopupMenu(popupMenu);
            trayIcon.setActionCommand(TOOL_TIP);
            trayIcon.addActionListener(action ->
            {
                if(action.getActionCommand().equals(TOOL_TIP))
                {
                    onOpenPopupClicked.run();
                }
            });
        }
    }

    private MenuItem createOpenMenuItem()
    {
        MenuItem openMenuItem = new MenuItem();

        openMenuItem.setName("Open");
        openMenuItem.setLabel("Open " + TOOL_TIP);
        openMenuItem.addActionListener(action ->
        {
            if(action.getActionCommand().equals("Open " + TOOL_TIP))
            {
                onOpenPopupClicked.run();
            }
        });

        return openMenuItem;
    }

    private MenuItem createExitMenuItem()
    {
        MenuItem exitMenuItem = new MenuItem();

        exitMenuItem.setName("Exit");
        exitMenuItem.setLabel("Exit " + TOOL_TIP);
        exitMenuItem.addActionListener(action ->
        {
            if(action.getActionCommand().equals("Exit " + TOOL_TIP))
            {
                onExitPopupClicked.run();
            }
        });

        return exitMenuItem;
    }

    private void send(String caption, String message, TrayIcon.MessageType type)
    {
        if(isSupported())
        {
            trayIcon.displayMessage(caption, message, type);
        }
    }

    private void logAwtErrorWhile(String what, AWTException e)
    {
        LOG.error("Encountered an AWT exception while " + what, e);
    }
}
