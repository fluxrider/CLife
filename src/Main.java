// Written by David Lareau on January 2014

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class Main extends JPanel {

	// Widgets
	private ZoomImage view;

	public Main(BufferedImage first_generation) {
		super(new BorderLayout());

		// create widgets
		JButton next = new JButton("Next Iteration");
		JButton save = new JButton("Save");
		this.view = new ZoomImage(first_generation);

		// default states
		frame.getRootPane().setDefaultButton(next);
		next.requestFocus();

		// layout 
		this.add(next, BorderLayout.SOUTH);
		this.add(view, BorderLayout.CENTER);
		this.add(save, BorderLayout.NORTH);

		// events
		next.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				LifeC op = new LifeC();
				op.defaultSetup();
				BufferedImage next = op.execute(view.getImage());
				view.setImage(next, true);
			}
		});
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					String filename = System.currentTimeMillis() + ".png";
					ImageIO.write(view.getImage(), "png", new File(filename));
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	// Main
	public static JFrame frame;

	public static void main(String[] args) throws IOException {
		// load user specified image
		JFileChooser chooser = new JFileChooser("res/");
		chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "bmp"));
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// create the window with the image loaded for the first 'life' generation
			BufferedImage image = ImageIO.read(chooser.getSelectedFile());
			frame = new JFrame("Layout Viewer (Launcher)");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new Main(image));
			frame.setSize(800, 600);
			frame.setLocationRelativeTo(null); // centers in screen
			frame.setVisible(true);
		}
	}

}