/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Game;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;

import Client.JConfig;
import Client.Launcher;
import Client.NotificationsHandler;
import Client.Settings;
import Client.TrayHandler;
import Client.Util;

/**
 * Singleton class that handles packaging the client into a JFrame and starting the applet.
 */
public class Game extends JFrame implements AppletStub, ComponentListener, WindowListener {
	
	// Singleton
	private static Game instance = null;
	
	private JConfig m_config = new JConfig();
	private Applet m_applet = null;

	// Coordinates
	private static Point m_coords = null;
	private static Color backgroundGray =  new Color(40, 40, 40);
	private static Color borderGray =  new Color(30, 30, 30);

	private Game() {
		// Empty private constructor to prevent extra instances from being created.
	}

	public void setApplet(Applet applet) {
		m_applet = applet;
		m_applet.setStub(this);
	}
	
	public Applet getApplet() {
		return m_applet;
	}
	
	/**
	 * Builds the main game client window and adds the applet to it.
	 */
	public void start() {
		if (m_applet == null)
			return;

		setDefaultLookAndFeelDecorated(true);
		
		// Set window icon
		setIconImage(Launcher.icon.getImage());
		
		// Set window properties
		setResizable(true);
		addWindowListener(this);
		setMinimumSize(new Dimension(1, 1));

		// Customize window frame
//		setUndecorated(true);
		Container pane = getContentPane();
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JPanel sidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {

			}

			@Override public void mousePressed(MouseEvent e) {
				m_coords = e.getPoint();
				m_coords.x += 4;
				m_coords.y += 4;
			}

			@Override public void mouseReleased(MouseEvent e) {
				m_coords = null;
			}

			@Override public void mouseEntered(MouseEvent e) {

			}

			@Override public void mouseExited(MouseEvent e) {

			}
		});
		topPanel.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseDragged(MouseEvent e) {
				Point curCoords = e.getLocationOnScreen();
				setLocation(curCoords.x - m_coords.x, curCoords.y - m_coords.y);
			}

			@Override public void mouseMoved(MouseEvent e) {

			}
		});

		final JButton bExit = new JButton("X");
		bExit.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				JComponent comp = (JComponent) e.getSource();
				Window win = SwingUtilities.getWindowAncestor(comp);
				win.dispose();
			}
		});

		topPanel.add(bExit);
		topPanel.setBackground(borderGray);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(topPanel, c);

		contentPanel.setBackground(Color.BLUE);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(contentPanel, c);

		sidePanel.setBackground(Color.RED);
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(sidePanel, c);

		getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		getRootPane().setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, borderGray));

		// Add applet to window
//		setContentPane(m_applet);
		getContentPane().setBackground(backgroundGray);
		getContentPane().setPreferredSize(new Dimension(512, 346));
		addComponentListener(this);
		pack();
		
		// Hide cursor if software cursor
		Settings.checkSoftwareCursor();
		
		// Position window and make it visible
		setLocationRelativeTo(null);
		setVisible(true);
		
		Reflection.Load();
		Renderer.init();
		
		if (!Util.isMacOS() && Settings.CUSTOM_CLIENT_SIZE) {
			Game.getInstance().resizeFrameWithContents();
		}
	}
	
	public JConfig getJConfig() {
		return m_config;
	}
	
	/**
	 * Starts the game applet.
	 */
	public void launchGame() {
		m_config.changeWorld(Settings.WORLD);
		m_applet.init();
		m_applet.start();
	}
	
	@Override
	public void setTitle(String title) {
		String t = "rscplus";
		
		if (title != null)
			t = t + " (" + title + ")";
		
		super.setTitle(t);
	}
	
	/*
	 * AppletStub methods
	 */
	
	@Override
	public final URL getCodeBase() {
		return m_config.getURL("codebase");
	}
	
	@Override
	public final URL getDocumentBase() {
		return getCodeBase();
	}
	
	@Override
	public final String getParameter(String key) {
		return m_config.parameters.get(key);
	}
	
	@Override
	public final AppletContext getAppletContext() {
		return null;
	}
	
	@Override
	public final void appletResize(int width, int height) {
	}
	
	/*
	 * WindowListener methods
	 */
	
	@Override
	public final void windowClosed(WindowEvent e) {
		if (m_applet == null)
			return;
		
		m_applet.stop();
		m_applet.destroy();
	}
	
	@Override
	public final void windowClosing(WindowEvent e) {
		dispose();
		Launcher.getConfigWindow().disposeJFrame();
		TrayHandler.removeTrayIcon();
		NotificationsHandler.closeNotificationSoundClip();
		NotificationsHandler.disposeNotificationHandler();
	}
	
	@Override
	public final void windowOpened(WindowEvent e) {
	}
	
	@Override
	public final void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public final void windowActivated(WindowEvent e) {
	}
	
	@Override
	public final void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public final void windowIconified(WindowEvent e) {
	}
	
	/*
	 * ComponentListener methods
	 */
	
	@Override
	public final void componentHidden(ComponentEvent e) {
	}
	
	@Override
	public final void componentMoved(ComponentEvent e) {
	}
	
	@Override
	public final void componentResized(ComponentEvent e) {
		if (m_applet == null)
			return;
		
		// Handle minimum size and launch game
		// TODO: This is probably a bad spot and should be moved
		if (getMinimumSize().width == 1) {
			setMinimumSize(getSize());
			launchGame();
			
			// This workaround appears to be for a bug in the macOS JVM
			// Without it, mac users get very angry
			if (Util.isMacOS()) {
				setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
				setLocationRelativeTo(null);
			}
		}
		
		Renderer.resize(getContentPane().getWidth(), getContentPane().getHeight());
	}
	
	@Override
	public final void componentShown(ComponentEvent e) {
	}
	
	/**
	 * Gets the game client instance. It makes one if one doesn't exist.
	 * 
	 * @return The game client instance
	 */
	public static Game getInstance() {
		if (instance == null) {
			synchronized (Game.class) {
				instance = new Game();
			}
		}
		return instance;
	}
	
	/**
	 * Resizes the Game window to match the X and Y values stored in Settings. The applet's size will be recalculated on
	 * the next rendering tick.
	 */
	public void resizeFrameWithContents() {
		int windowWidth = Settings.CUSTOM_CLIENT_SIZE_X + getInsets().left + getInsets().right;
		int windowHeight = Settings.CUSTOM_CLIENT_SIZE_Y + getInsets().top + getInsets().bottom;
		setSize(windowWidth, windowHeight);
		setLocationRelativeTo(null);
	}
	
}
