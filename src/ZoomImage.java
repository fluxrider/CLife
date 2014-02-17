/* 
 * Written by David Lareau on August 8, 2009
 * 
 * A single zoomable image panel.
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class ZoomImage extends JPanel implements ActionListener, MouseListener, MouseWheelListener, MouseMotionListener {

	// Constants
	public static String[] defaultScaleChoices = { ".10", ".20", ".50", ".75", "1.00", "2.00", "4.00", "8.00" };

	// Attributes
	public ResizableImage imagePanel;

	public JComboBox<String> scaleTextBox;

	private JScrollPane scrollPane;

	private JScrollBar hb;

	private JScrollBar vb;

	private int hbMovementRequest;

	private int vbMovementRequest;

	public int hoveredX, hoveredY;

	// Construct
	public ZoomImage(BufferedImage image) {
		super(new BorderLayout());

		// create main components (image, zoom, status bar)
		this.imagePanel = new ResizableImage(image, 1);
		this.scaleTextBox = new JComboBox<String>(defaultScaleChoices);
		this.scrollPane = new JScrollPane(imagePanel);
		this.hb = scrollPane.getHorizontalScrollBar();
		this.vb = scrollPane.getVerticalScrollBar();

		// zoom control
		scaleTextBox.setEditable(true);
		scaleTextBox.addActionListener(this);
		scaleTextBox.setSelectedIndex(4);

		// Layout
		JPanel imageRegion = new JPanel(new BorderLayout());
		imageRegion.add(scrollPane, BorderLayout.CENTER);
		imageRegion.add(scaleTextBox, BorderLayout.SOUTH);
		JPanel allRegions = new JPanel(new BorderLayout());
		allRegions.add(imageRegion, BorderLayout.CENTER);
		// Finalize Layout
		this.add(allRegions, BorderLayout.CENTER);

		// events to trigger context menu
		imagePanel.addMouseListener(this);
		imagePanel.addMouseWheelListener(this);
		imagePanel.addMouseMotionListener(this);

		hoveredX = hoveredY = -1;
	}

	// Action Listener
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// JCombo Box for zoom
		if (source == scaleTextBox) {
			try {
				// try to parse text box
				double scale = Double.parseDouble(scaleTextBox.getSelectedItem().toString());
				imagePanel.setScale(scale);
			} catch (NumberFormatException _e) {
				_e.printStackTrace();
			}
		}
	}

	// MouseListener
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		hoveredX = hoveredY = -1;
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		// report clicked color
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = (int) (e.getX() / imagePanel.scale);
			int y = (int) (e.getY() / imagePanel.scale);
			if (imagePanel.image != null) {
				if (x >= 0 && x < imagePanel.image.getWidth() && y >= 0 && y < imagePanel.image.getHeight()) {
					//int rgb = imagePanel.image.getRGB(x, y);
					//					System.out.printf("Pixel clicked color: (%d,%d)=(%d,%d,%d)=gray(%d)\n", x, y, C.r(rgb), C.g(rgb), C.b(rgb), C.grayDX(rgb));
				}
			}
		}
	}

	// MouseWheelListener
	public void mouseWheelMoved(MouseWheelEvent e) {
		// try to parse text box
		try {
			double previousScale = imagePanel.getScale();
			double scale = Double.parseDouble(scaleTextBox.getSelectedItem().toString());
			boolean magnify = e.getWheelRotation() < 0;
			int n = Math.abs(e.getWheelRotation());
			for (int i = 0; i < n; i++) {
				if (magnify) scale *= 1.4;
				else scale *= 0.6;
			}
			scaleTextBox.setSelectedItem(scale);
			int x = e.getX();
			int y = e.getY();
			double nx = x / previousScale * scale;
			double ny = y / previousScale * scale;
			hbMovementRequest = (int) (nx - x);
			vbMovementRequest = (int) (ny - y);
			// TODO I've tried to handle movement request in adjustmentValueChanged just that didn't give good result so they are back here (does not work well when adjusting to a new value that didn't exist in old bound)  
			if (hbMovementRequest != 0) {
				hb.setValue(hb.getValue() + hbMovementRequest);
				hbMovementRequest = 0;
			}
			if (vbMovementRequest != 0) {
				vb.setValue(vb.getValue() + vbMovementRequest);
				vbMovementRequest = 0;
			}
		} catch (NumberFormatException _e) {
			_e.printStackTrace();
		}
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		hoveredX = hoveredY = -1;
		int x = (int) (e.getX() / imagePanel.scale);
		int y = (int) (e.getY() / imagePanel.scale);
		if (imagePanel.image != null) {
			if (x >= 0 && x < imagePanel.image.getWidth() && y >= 0 && y < imagePanel.image.getHeight()) {
				hoveredX = x;
				hoveredY = y;
			}
		}
	}

	// Get/Set Image
	public BufferedImage getImage() {
		return imagePanel.image;
	}

	public void setImage(BufferedImage image, boolean repaint) {
		imagePanel.setImage(image);
		if (repaint) repaint();
	}

	// Inner Class
	public class ResizableImage extends JPanel {

		// Attributes
		public BufferedImage image;

		public int W, H;

		public double scale;

		// Construct
		public ResizableImage(BufferedImage image, double scale) {
			setImage(image);
			setScale(scale);
		}

		// Methods
		public void paint(Graphics g) {
			g.setColor(Color.MAGENTA);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(image, 0, 0, (int) (W * scale), (int) (H * scale), null);
		}

		public void setScale(double scale) {
			this.scale = scale;
			setPreferredSize(new Dimension((int) (W * scale), (int) (H * scale)));
			revalidate();
			repaint();
		}

		public void setImage(BufferedImage image) {
			this.image = image;
			int oldW = W;
			int oldH = H;
			W = image == null ? 100 : image.getWidth(null);
			H = image == null ? 100 : image.getHeight(null);
			if ((oldW != W || oldH != H) && scale != 0) setScale(scale);
		}

		public double getScale() {
			return scale;
		}

	}

}
