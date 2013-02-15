package leveledit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import levelfileformats.Blocko2LevelFile;
import levelfileformats.InternalLevelFile;
import levelfileformats.MappyLevelFile;
import levelmodel.Level;

/**
 * The GUI class of the LevelEdit application.
 * Sets up all the buttons, menus and stuff in the main window.
 * 
 * <p>TODO: Not the best way to set up a GUI in java.
 * This should be revised.
 * 
 */
public class LevelEdit extends JFrame
{
    // Help text
    private static final String HELP_TEXT = 
	"Editor help                              \n"+
	"-----------                              \n"+
	"                                         \n"+
	"Keys:                                    \n"+
	"A,W,S,D - move camera                    \n"+
	"Tiletool + CTRL + click - erase          \n"+
	"Tiletool + F + click - flood fill        \n"+
	"Q,E - select next/prev dummy             \n"+
	"1,2 - edit FG / BG tile layer            \n"+
	"UP/DOWN/LEFT/RIGHT - move selected dummy.\n"+
	"                                         \n"+
	"                                         \n"+
	""
    ;
   
    
    // <editor-fold desc="GUI elements">

    // menu bar
    private JMenuItem newMapItem;    
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenuItem openItem;
    private JMenuItem importMappyItem;  
    private JMenuItem exportAsBlockoFormatItem;
    private JMenuItem exportAsBlockoFormat2Item;
    private JMenuItem helpItem;

    // object edit btns    
    private JButton moveUp; 
    private JButton moveDown; 
    private JButton moveRight; 
    private JButton moveLeft;
    private JButton nudgeUp;
    private JButton nudgeDown;
    private JButton nudgeRight; 
    private JButton nudgeLeft;
    private JButton nextObjBtn; 
    private JButton prevObjBtn;
    private JPanel  dummyTab;
    public  JSlider scrollbar; 
    
    public ToolSelector toolSelector;
    public TileSelector tileSelector;
    public DummyTypeSelector dummyTypeSelector;
    
    // </editor-fold>
    
    private Config config;
    
    // constants
    public static final String
	LABEL_UP           = "^",
	LABEL_DOWN         = "v",
	LABEL_RIGHT        = ">",
	LABEL_LEFT         = "<";
    
    // initial window props.
    private static final int
	SIZE_X             = 1200,
	SIZE_Y             = 500,
	STARTLOC_X         = 10,
	STARTLOC_Y         = 100;

    /** For showing open file. */
    private File currentFile = null;

    /** Remember pos in file system. */
    private File currentDir;
    
    /** Map view. */
    private MapView mapView; 
    
    /** Level object. */
    private Level level = new Level();

    
    /**
     * Create new blank level. Ask for size.
     */
    public void newLevel() {
        int x = Integer.parseInt(
                JOptionPane.showInputDialog(this, "Map size x?"));
        int y = Integer.parseInt(
                JOptionPane.showInputDialog(this, "Map size y?"));
        level.initBlankMap(x, y);
        currentFile = null;
        setTitle("LevelEdit - untitled");
    }

    /**
     * Show a "save" dialog to get path.
     * @param useCurrent If to use already opened level if any.
     * @return Path to save to.
     */
    public String getSaveLevelPath(boolean useCurrent) {
        if (!useCurrent || currentFile == null) {
            
            JFileChooser fc = new JFileChooser("");
            fc.showSaveDialog(this);
            fc.setCurrentDirectory(currentDir);
            File selFile = fc.getSelectedFile();
            
            if (selFile != null) {                                
                currentFile = selFile;
                setTitle("LevelEdit - " + currentFile.getAbsolutePath());
            }                        

        } else {            
            System.out.println("Wrote file " + currentFile.getAbsolutePath());            
        }
        
        return currentFile.getAbsolutePath();
    }
    
    /**
     * Show an "open" dialog to to get path.
     * @return Path to open. 
     */
    public String getOpenLevelPath() {
            
        JFileChooser fc = new JFileChooser("");
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
                        
            currentFile = fc.getSelectedFile();
            setTitle("LevelEdit - " + currentFile.getAbsolutePath());
            return currentFile.getAbsolutePath();
        }
        return null;
    }
        
    /**
     * Shows help window.
     */
    public void showHelp() {
        JOptionPane.showMessageDialog(this, HELP_TEXT);
        System.out.println(HELP_TEXT);
    }
    
    /**
     * Handle all actions in main window.
     */
    private class LevelEditHandler implements ActionListener
    {
	LevelEdit parentframe;
	
        @Override
	public void actionPerformed (ActionEvent event)
	{		    	    	       

            if (event.getSource() == nextObjBtn) {
                level.getDummyObjects().selectNextDummy();
            }

            if (event.getSource() == prevObjBtn) {
                level.getDummyObjects().selectPrevDummy();
            }

            // buttons move & nudge
            if (level.getDummyObjects().selected() != null) {
                // move
                if (event.getSource() == moveUp) {
                    level.getDummyObjects().moveSelectedDummy(0, -Config.TILESIZE);
                } else if (event.getSource() == moveDown) {
                    level.getDummyObjects().moveSelectedDummy(0, Config.TILESIZE);
                } else if (event.getSource() == moveLeft) {
                    level.getDummyObjects().moveSelectedDummy(-Config.TILESIZE, 0);
                } else if (event.getSource() == moveRight) {
                    level.getDummyObjects().moveSelectedDummy(Config.TILESIZE, 0);
                } // nudge
                else if (event.getSource() == nudgeUp) {
                    level.getDummyObjects().moveSelectedDummy(0, -1);
                } else if (event.getSource() == nudgeDown) {
                    level.getDummyObjects().moveSelectedDummy(0, 1);
                } else if (event.getSource() == nudgeLeft) {
                    level.getDummyObjects().moveSelectedDummy(-1, 0);
                } else if (event.getSource() == nudgeRight) {
                    level.getDummyObjects().moveSelectedDummy(1, 0);
                }
            }

            // MENU -> "Save" (LevelEdit format)
            if (event.getSource() == saveItem) {
                level.writeToFile(new InternalLevelFile(
                        getSaveLevelPath(true)));
            }

            // MENU -> "Save as" LevelEdit format
            if (event.getSource() == saveAsItem) {
                level.writeToFile(new InternalLevelFile(
                        getSaveLevelPath(false)));
            }

            // MENU -> Export as blocko format 2
            if (event.getSource() == exportAsBlockoFormat2Item) {
                level.writeToFile(new Blocko2LevelFile(
                        getSaveLevelPath(false)));
            }

            // HELP
            if (event.getSource() == helpItem) {
                showHelp();
            }

	    
	    // MENU -> New map
	    if (event.getSource() == newMapItem){
                newLevel();		
	    }

            // MENU -> open
            if (event.getSource() == openItem) {

                level.initFromFile(new InternalLevelFile(getOpenLevelPath()), 
                        config.typeData);
            }
	    
	    // MENU -> import mappy           
            if (event.getSource() == importMappyItem) {

                level.initFromFile(new MappyLevelFile(getOpenLevelPath()),
                        config.typeData);
            }           

	    mapView.requestFocusInWindow();
	    repaint();
	    mapView.repaint();
	}	
    }
    
    
    /**
     * Create the main window GUI.
     * @param container Container object.
     */
    private void createGui(Container container)
    {
	LevelEditHandler handler = new LevelEditHandler(); 	
	handler.parentframe = this;
        
	//------------------------------------------------------
	// menu bar init
	JMenuBar bar = new JMenuBar();
	setJMenuBar(bar);

	JMenu fileMenu = new JMenu ("File");

	JMenu helpMenu = new JMenu ("Help");
	helpItem = new JMenuItem ("Help");
	helpItem.addActionListener(handler);
	helpMenu.add(helpItem);

	newMapItem = new JMenuItem ("New map");
	newMapItem.addActionListener(handler);
	fileMenu.add(newMapItem);

	openItem = new JMenuItem ("Open");
	openItem.addActionListener(handler);
	fileMenu.add(openItem);

	importMappyItem = new JMenuItem ("Import Mappy file");
	importMappyItem.addActionListener(handler);
	fileMenu.add(importMappyItem);

	saveItem = new JMenuItem ("Save");
	saveItem.addActionListener(handler);
	fileMenu.add(saveItem);

	saveAsItem = new JMenuItem ("Save As...");
	saveAsItem.addActionListener(handler);
	fileMenu.add(saveAsItem);

	exportAsBlockoFormatItem = new JMenuItem ("Export to Blocko format");
	exportAsBlockoFormatItem.addActionListener(handler);
	fileMenu.add(exportAsBlockoFormatItem);

	exportAsBlockoFormat2Item = new JMenuItem ("Export to Blocko format 2");
	exportAsBlockoFormat2Item.addActionListener(handler);
	fileMenu.add(exportAsBlockoFormat2Item);

	bar.add(fileMenu);
	bar.add(helpMenu);

	//-----------------------------------------------------
	// DUMMYTAB
	//
	dummyTab  = new JPanel();
	dummyTab.setLayout(new GridLayout(1,9));	
	JPanel moveButtons = new JPanel();
	JPanel nudgeButtons = new JPanel();
  
        toolSelector = new ToolSelector();
	dummyTab.add(toolSelector);
        
        dummyTypeSelector = new DummyTypeSelector();
        dummyTab.add(dummyTypeSelector);


	//------------------------------------------------------
	// add, prev, next buttons
	JPanel middle = new JPanel();
	middle.setLayout(new GridLayout(1,2));

	nextObjBtn = new JButton("Select next");
	nextObjBtn.addActionListener(handler);
	middle.add(nextObjBtn);

	prevObjBtn = new JButton("Select prev");
	prevObjBtn.addActionListener(handler);
	middle.add(prevObjBtn);
	
	dummyTab.add(middle);

	//-----------------------------------------------------------
	// move, nudge direction btns.
	moveButtons.setLayout(new GridLayout(3,3)); 

	// 1 row
	moveButtons.add(new JPanel());
	moveUp = new JButton(LABEL_UP);
	moveButtons.add(moveUp);
	moveUp.addActionListener(handler);
	moveButtons.add(new JPanel());
	
	// 2 row
	moveLeft = new JButton(LABEL_LEFT); 
	moveButtons.add(moveLeft);
	moveLeft.addActionListener(handler);
	moveButtons.add(new JLabel("MOVE", JLabel.CENTER));
	moveRight = new JButton(LABEL_RIGHT); 
	moveButtons.add(moveRight);
	moveRight.addActionListener(handler);

	// 3 row
	moveButtons.add(new JPanel());
	moveDown = new JButton(LABEL_DOWN); 
	moveButtons.add(moveDown);
	moveDown.addActionListener(handler);
	moveButtons.add(new JPanel());
      
	//--------------------------------------------------------

	nudgeButtons.setLayout(new GridLayout(3,3));

	// 1 row
	nudgeButtons.add(new JPanel());
	nudgeUp = new JButton(LABEL_UP);
	nudgeButtons.add(nudgeUp);
	nudgeUp.addActionListener(handler);
	nudgeButtons.add(new JPanel());
	
	// 2 row
	nudgeLeft = new JButton(LABEL_LEFT);
	nudgeButtons.add(nudgeLeft);
	nudgeLeft.addActionListener(handler);
	nudgeButtons.add(new JLabel("NUDGE",JLabel.CENTER));
	nudgeRight = new JButton(LABEL_RIGHT);
	nudgeButtons.add(nudgeRight);
	nudgeRight.addActionListener(handler);
	
	// 3 row
	nudgeButtons.add(new JLabel());
	nudgeDown = new JButton(LABEL_DOWN);
	nudgeButtons.add(nudgeDown);
	nudgeDown.addActionListener(handler);
	nudgeButtons.add(new JLabel());

	dummyTab.add(moveButtons);
	dummyTab.add(nudgeButtons);

        tileSelector = new TileSelector();	
        container.add(tileSelector, BorderLayout.EAST);
        
	container.add(dummyTab, BorderLayout.SOUTH);

	// init main window
       	setSize(SIZE_X,SIZE_Y);
	setLocation(STARTLOC_X,STARTLOC_Y);
	setVisible(true);
	setResizable(true);
        
    }
        
    /**
     * Setup app.
     * @param configfile a config file in the LevelEdit format.
     */
    public LevelEdit(String configFile)
    {
      	super("LevelEdit");
        
        config = new Config(configFile);
        
	Container container = getContentPane();
        createGui(container);
  
        tileSelector.setTiles(config.tiles, Config.NUM_TILES, 
                Config.TILESIZE, Config.TILESIZE);

        dummyTypeSelector.setDummyList(config.typeNames, config.typeData);
        
	// mapview
	mapView = new MapView(config, toolSelector, tileSelector, 
                dummyTypeSelector, level);
        mapView.addKeyListener(mapView);
        mapView.addMouseListener(mapView);
        mapView.addMouseMotionListener(mapView);
	container.add(mapView, BorderLayout.CENTER);
	mapView.setVisible(true);
	repaint();
	mapView.repaint();
	mapView.requestFocusInWindow();
    }

    /**
     * App main entry.
     * 
     * @param args  arg 0 path to a config file.
     */
    public static void main(String[] args) {
        String configFile;
        if (args.length < 1) {
            System.out.println("No config specified, using default.");
            configFile = "src/leveledit/default.config";
        } else {
            configFile = args[0];
        }

        try {
            LevelEdit app = new LevelEdit(configFile);
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        } catch (Exception e) {
            System.out.println("Error while initializing app.");
            e.printStackTrace();
        }
    }
}