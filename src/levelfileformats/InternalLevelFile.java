package levelfileformats;

import java.io.FileOutputStream;
import levelmodel.DummyObject;
import levelmodel.DummyObjects;
import leveledit.LevelFileInterface;
import leveledit.ResFileReader;
import levelmodel.TileMap;

/**
 *
 *  LevelEdit internal file format.
 */
public class InternalLevelFile 
implements LevelFileInterface {
    
    private String path;
    
    /**
     * Is created for specific file.
     */
    public InternalLevelFile(String path) {
        this.path = path;
    }
    
    /**
     * Write level to file.
     * 
     * @return true if ok.
     */
    @Override
    public boolean write(DummyObjects dummyObjects, TileMap tileMap) {
        try {
	
	    FileOutputStream f = new FileOutputStream(path);

	    // filename
	    f.write(new String("# "+"Level generated by RILedit.").getBytes());
	    f.write(new String("\r\n# "+path+"\r\n").getBytes());
	    f.write(new String("# "+"\r\n\r\n").getBytes());

	    // map array
	    f.write(new String("[map]"+"\r\n").getBytes());
	    f.write(new String(Integer.toString(tileMap.getWidth())).getBytes());
	    f.write(new String(" "+Integer.toString(tileMap.getHeight())+"\r\n").getBytes());
	    String data = new String();
	    int w = tileMap.getWidth();
	    int h = tileMap.getHeight();   
	    for (int j = 0; j < h; j++){
		for (int i = 0; i < w; i++){

		    data += Integer.toString(tileMap.getTileVal(i, j, 0))+" ";
		}
	    }
	    f.write(new String(data+"\r\n\r\n").getBytes());	    

	    // map array 2 (bg)
	    f.write(new String("[map2]"+"\r\n").getBytes());
	    f.write(new String(Integer.toString(tileMap.getWidth())).getBytes());// w
	    f.write(new String(" "+Integer.toString(tileMap.getHeight())+"\r\n").getBytes());// h
	    data = new String();
	    w = tileMap.getWidth();
	    h = tileMap.getHeight();	    
	    for (int j = 0; j < h; j++){
		for (int i = 0; i < w; i++){

		    data += Integer.toString(tileMap.getTileVal(i, j, 1))+" ";
		}
	    }
	    f.write(new String(data+"\r\n\r\n").getBytes());
	    
	    // objects
	    f.write(new String("[init]"+"\r\n").getBytes());
	    DummyObject p;
	    for (int i = 0; i < dummyObjects.size(); i++)
	    {
		p = (DummyObject)dummyObjects.elementAt(i);
				
		String objdata = new String(
					  "new " + p.name + '\t' +
					  p.x + '\t' + p.y + '\t' + p.w + '\t' + p.h + '\t' +
					  p.additionalData + "\r\n"
					  );
		
		f.write(objdata.getBytes());		
	    }	   	    
	    f.write(new String("end"+"\r\n").getBytes());

	    f.close();	    
	} catch (Exception e) {}
        return true;
    }
    
    /**
     * Read a level into memory.
     * @return true if ok.
     */
    @Override
    public boolean read(DummyObjects dummyObjects, DummyObject [] dummyTypes, 
        TileMap tileMap) {

	System.out.println("Opening .level file");
	try 
	{ 
	    ResFileReader r;
	    r = new ResFileReader(path);
	    
	    // load tilemap
	    r.gotoPost("map", false);
	    tileMap.setMap(r.readMapArray(), 0);

	    // load bg layer, support old file format
	    try {
		r.gotoPost("map2", false);
		tileMap.setMap(r.readMapArray(), 1);
	    } catch (Exception e)
	    {
		System.out.println(this.getClass().getName() + "." + "initLevelFromFile(): " 
				   + "Read old format! Converting to new format!");
		// create tilemap
		int x = tileMap.getWidth();
		int y = tileMap.getHeight();
		    tileMap.setMap(new int [y][x], 1);
		for (int i = 0; i < x; i++)
		    for (int j = 0; j < y; j++)
		tileMap.setTileVal(x, y, 1, 0);
	    }

	    // load objects
	    r.gotoPost("init",false);
	    r.nextLine();
	    int loops = 0;
	    while (r.getWord().compareTo("end") != 0 && loops < 1000)
	    {
		loops ++;
		if (r.getWord().compareTo("new") == 0)
		{
		    System.out.println("Adding dummy...");
		    String type = r.getNextWord();
		    int x = r.getNextWordAsInt();
		    int y = r.getNextWordAsInt();
		    int w = r.getNextWordAsInt();
		    int h = r.getNextWordAsInt();
		    String addData = r.getRestOfLine();
		    
		    DummyObject dummy ;
		    boolean found = false;
		    for (int i = 0; i < dummyTypes.length; i++){
			if (dummyTypes[i].name.compareTo(type)==0){
			    found = true;
			    dummy = new DummyObject(dummyTypes[i]);
			    dummy.x = x;
			    dummy.y = y;
			    dummy.w = w;
			    dummy.h = h;
			    dummy.additionalData = addData;
			    dummyObjects.newDummy(dummy);
			}
		    }
		    if (!found){
			System.out.println(
			    "Warning: dummytype "
			    +type+" not defined in def.file!");
		    }
		}
		r.nextLine();
	    }

	}
	catch (Exception e) {
            e.printStackTrace();
	    System.out.println("Error reading *.level file!");
	    return false;
	}

	System.out.println("Init finished");
        return true;
    }   
}