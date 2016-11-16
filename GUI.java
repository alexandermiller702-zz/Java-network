import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;

/**
 * This is a template GUI that you can use for your mapping program. It is an
 * *abstract class*, which means you'll need to extend it in your own program.
 * For a simple example of how to do this, have a look at the SquaresExample
 * class.
 * 
 * This GUI uses Swing, not the first-year UI library. Swing is not the focus of
 * this course, but it would be to your benefit if you took some time to
 * understand how this class works.
 * 
 * @author tony
 */
public abstract class GUI {
	/**
	 * defines the different types of movement the user can perform, the
	 * appropriate one is passed to your code when the move(Move) method is
	 * called.
	 */
	public enum Move {
		NORTH, SOUTH, EAST, WEST, ZOOM_IN, ZOOM_OUT
	};

	// these are the methods you need to implement.

	/**
	 * Is called when the drawing area is redrawn and performs all the logic for
	 * the actual drawing, which is done with the passed Graphics object.
	 */
	protected abstract void redraw(Graphics g);

	/**
	 * Is called when the mouse is clicked (actually, when the mouse is
	 * released), and is passed the MouseEvent object for that click.
	 */
	protected abstract void onClick(MouseEvent e);

	/**
	 * Is called whenever the search box is updated. Use getSearchBox to get the
	 * JTextField object that is the search box itself.
	 */
	protected abstract void onSearch();

	/**
	 * Is called whenever a navigation button is pressed. An instance of the
	 * Move enum is passed, representing the button clicked by the user.
	 */
	protected abstract void onMove(Move m);

	/**
	 * Is called when the user has successfully selected a directory to load the
	 * data files from. File objects representing the four files of interested
	 * are passed to the method. The fourth File, polygons, might be null if it
	 * isn't present in the directory.
	 * 
	 * @param nodes
	 *            a File for nodeID-lat-lon.tab
	 * @param roads
	 *            a File for roadID-roadInfo.tab
	 * @param segments
	 *            a File for roadSeg-roadID-length-nodeID-nodeID-coords.tab
	 * @param polygons
	 *            a File for polygon-shapes.mp
	 */
	protected abstract void onLoad(File nodes, File roads, File segments,
			File polygons);

	// here are some useful methods you'll need.

	/**
	 * @return the JTextArea at the bottom of the screen for output.
	 */
	public JTextArea getTextOutputArea() {
		return textOutputArea;
	}

	/**
	 * @return the JTextField used as a search box in the top-right, which can
	 *         be queried for the string it contains.
	 */
	public JTextField getSearchBox() {
		return search;
	}

	/**
	 * @return the dimensions of the drawing area.
	 */
	public Dimension getDrawingAreaDimension() {
		return drawing.getSize();
	}

	/**
	 * Redraws the window (including drawing pane). This is already done
	 * whenever a button is pressed or the search box is updated, so you
	 * probably won't need to call this.
	 */
	public void redraw() {
		frame.repaint();
	}

	// --------------------------------------------------------------------
	// Everything below here is Swing-related and, while it's worth
	// understanding, you don't need to look any further to finish the
	// assignment up to and including completion.
	// --------------------------------------------------------------------

	private static final boolean UPDATE_ON_EVERY_CHARACTER = false;

	private static final int DEFAULT_DRAWING_HEIGHT = 400;
	private static final int DEFAULT_DRAWING_WIDTH = 400;
	private static final int TEXT_OUTPUT_ROWS = 5;
	private static final int SEARCH_COLS = 15;

	private static final String NODES_FILENAME = "nodeID-lat-lon.tab";
	private static final String ROADS_FILENAME = "roadID-roadInfo.tab";
	private static final String SEGS_FILENAME = "roadSeg-roadID-length-nodeID-nodeID-coords.tab";
	private static final String POLYS_FILENAME = "polygon-shapes.mp";

	/*
	 * In Swing, everything is a component; buttons, graphics panes, tool tips,
	 * and the window frame are all components. This is implemented by
	 * JComponent, which sits at the top of the component inheritance hierarchy.
	 * A JFrame is a component that represents the outer window frame (with the
	 * minimise, maximise, and close buttons) of your program. Every swing
	 * program has to have one somewhere. JFrames can, of course, have other
	 * components inside them. JPanels are your bog-standard container component
	 * (can have other components inside them), that are used for laying out
	 * your UI.
	 */

	private JFrame frame;

	private JPanel controls;
	private JComponent drawing; // we customise this to make it a drawing pane.
	private JTextArea textOutputArea;

	private JTextField search;
	private JFileChooser fileChooser;

	public GUI() {
		initialise();
	}

	@SuppressWarnings("serial")
	private void initialise() {

		/*
		 * first, we make the buttons etc. that go along the top bar.
		 */

		// action listeners give you a hook to perform when the button is
		// pressed. the horrible thing being passed to addActionListener is an
		// anonymous class, covered in SWEN221. these are useful when working
		// with swing. the quit button isn't really necessary, as you can just
		// press the frame's close button, but it serves as a nice example.
		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0); // cleanly end the program.
			}
		});

		fileChooser = new JFileChooser();
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File nodes = null, roads = null, segments = null, polygons = null;

				// set up the file chooser
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select input directory");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// run the file chooser and check the user didn't hit cancel
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					// get the files in the selected directory and match them to
					// the files we need.
					File directory = fileChooser.getSelectedFile();
					File[] files = directory.listFiles();

					for (File f : files) {
						if (f.getName().equals(NODES_FILENAME)) {
							nodes = f;
						} else if (f.getName().equals(ROADS_FILENAME)) {
							roads = f;
						} else if (f.getName().equals(SEGS_FILENAME)) {
							segments = f;
						} else if (f.getName().equals(POLYS_FILENAME)) {
							polygons = f;
						}
					}

					// check none of the files are missing, and call the load
					// method in your code.
					if (nodes == null || roads == null || segments == null) {
						JOptionPane.showMessageDialog(frame,
								"Directory does not contain correct files",
								"Error", JOptionPane.ERROR_MESSAGE);
					} else {
						onLoad(nodes, roads, segments, polygons);
						redraw();
					}
				}
			}
		});

		JButton west = new JButton("\u2190");
		west.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.WEST);
				redraw();
			}
		});

		JButton east = new JButton("\u2192");
		east.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.EAST);
				redraw();
			}
		});

		JButton north = new JButton("\u2191");
		north.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.NORTH);
				redraw();
			}
		});

		JButton south = new JButton("\u2193");
		south.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.SOUTH);
				redraw();
			}
		});

		JButton in = new JButton("+");
		in.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.ZOOM_IN);
				redraw();
			}
		});

		JButton out = new JButton("\u2012");
		out.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onMove(Move.ZOOM_OUT);
				redraw();
			}
		});

		// next, make the search box at the top-right. we manually fix
		// it's size, and add an action listener to call your code when
		// the user presses enter.
		search = new JTextField(SEARCH_COLS);
		search.setMaximumSize(new Dimension(0, 25));
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSearch();
				redraw();
			}
		});

		if (UPDATE_ON_EVERY_CHARACTER) {
			// this forces an action event to fire on every key press, so the
			// user doesn't need to hit enter for results.
			search.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					// don't fire an event on backspace or delete
					if (e.getKeyCode() == 8 || e.getKeyCode() == 127)
						return;
					search.postActionEvent();
				}
			});
		}

		/*
		 * next, make the top bar itself and arrange everything inside of it.
		 */

		// almost any component (JPanel, JFrame, etc.) that contains other
		// components inside it needs a LayoutManager to be useful, these do
		// exactly what you expect. three common LayoutManagers are the BoxLayout,
		// GridLayout, and BorderLayout. BoxLayout, contrary to its name, places
		// components in either a row (LINE_AXIS) or a column (PAGE_AXIS).
		// GridLayout is self-describing. BorderLayout puts a single component
		// on the north, south, east, and west sides of the outer component, as
		// well as one in the centre. google for more information.
		controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));

		// make an empty border so the components aren't right up against the
		// frame edge.
		Border edge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		controls.setBorder(edge);

		JPanel loadquit = new JPanel();
		loadquit.setLayout(new GridLayout(2, 1));
		// manually set a fixed size for the panel containing the load and quit
		// buttons (doesn't change with window resize).
		loadquit.setMaximumSize(new Dimension(50, 100));
		loadquit.add(load);
		loadquit.add(quit);
		controls.add(loadquit);
		// rigid areas are invisible components that can be used to space
		// components out.
		controls.add(Box.createRigidArea(new Dimension(15, 0)));

		JPanel navigation = new JPanel();
		navigation.setMaximumSize(new Dimension(150, 60));
		navigation.setLayout(new GridLayout(2, 3));
		navigation.add(out);
		navigation.add(north);
		navigation.add(in);
		navigation.add(west);
		navigation.add(south);
		navigation.add(east);
		controls.add(navigation);
		controls.add(Box.createRigidArea(new Dimension(15, 0)));
		// glue is another invisible component that grows to take up all the
		// space it can on resize.
		controls.add(Box.createHorizontalGlue());

		controls.add(new JLabel("Search"));
		controls.add(Box.createRigidArea(new Dimension(5, 0)));
		controls.add(search);

		/*
		 * then make the drawing canvas, which is really just a boring old
		 * JComponent with the paintComponent method overridden to paint
		 * whatever we like. this is the easiest way to do drawing.
		 */

		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				redraw(g);
			}
		};
		drawing.setPreferredSize(new Dimension(DEFAULT_DRAWING_WIDTH,
				DEFAULT_DRAWING_HEIGHT));
		// this prevents a bug where the component won't be
		// drawn until it is resized.
		drawing.setVisible(true);

		drawing.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				onClick(e);
				redraw();
			}
		});

		drawing.addMouseWheelListener(new MouseAdapter() {
			public void mouseWheelMoved(MouseWheelEvent e) {
			}
		});

		/*
		 * then make the JTextArea that goes down the bottom. we put this in a
		 * JScrollPane to get scroll bars when necessary.
		 */

		textOutputArea = new JTextArea(TEXT_OUTPUT_ROWS, 0);
		textOutputArea.setLineWrap(true);
		textOutputArea.setWrapStyleWord(true); // pretty line wrap.
		textOutputArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(textOutputArea);
		// these two lines make the JScrollPane always scroll to the bottom when
		// text is appended to the JTextArea.
		DefaultCaret caret = (DefaultCaret) textOutputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		/*
		 * finally, make the outer JFrame and put it all together. this is more
		 * complicated than it could be, as we put the drawing and text output
		 * components inside a JSplitPane so they can be resized by the user.
		 * the JScrollPane and the top bar are then added to the frame.
		 */

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setDividerSize(5); // make the selectable area smaller
		split.setContinuousLayout(true); // make the panes resize nicely
		split.setResizeWeight(1); // always give extra space to drawings
		// JSplitPanes have a default border that makes an ugly row of pixels at
		// the top, remove it.
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setTopComponent(drawing);
		split.setBottomComponent(scroll);

		frame = new JFrame("Mapper");
		// this makes the program actually quit when the frame's close button is
		// pressed.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(controls, BorderLayout.NORTH);
		frame.add(split, BorderLayout.CENTER);

		// always do these two things last, in this order.
		frame.pack();
		frame.setVisible(true);
	}
}

// code for COMP261 assignments
