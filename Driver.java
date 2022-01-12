package com.venkat.concurrency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
/*
 * This is the main Driver
 * It spins off a GenerateFiles first to generate input files
 * And quickly spins off the Driver to handle the events
 * Also initializes the processor lambda (transform) and passes 
 * it to procDriver which are invoked in the scheduled executor threads
 * The pool is set to have only three threads 
 * 
 */
public class Driver {
    public static final String pardir = "C:\\tmp";
    public static final String inpdir = "C:\\tmp\\sample";
    
	int numGroups = 0;
    private WatchService watcher;
    private Map<WatchKey,Path> keys;
    private ScheduledExecutorService exesvc = null;
    // define the transform
    Function<String,String> myfn;
    
    private boolean trace = false;
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }	
    /**
     * Register the given directory 
     * with the WatchService
     */
    private void register(Path dir) {
    	WatchKey key = null;
    	try {
    		key = dir.register(watcher, ENTRY_CREATE);
    	} catch (IOException ie) {
    		System.out.println(" Exception .... aborting");
    		System.exit(0);
    	}
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }
    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
    	System.out.println("process Events ..");
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()){
                WatchEvent.Kind kind = event.kind();

                // TBD - overflow events - have a separate thread
                // that goes around and looks for unprocessed files from a
                // given watermark timestamp t1 to current time and save that into t1
                // when done. All those that are found unprocessed - put them in a queue 
                // then kick off those with the process driver with the same constructor/process
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("PE %s: %s\n", event.kind().name(), child);
                System.out.format("child == " + child + " to String " + child.toAbsolutePath().toString());
                if (Group.isValid(child.toString())) {
                	exesvc.schedule(new ProcDriver(child.toAbsolutePath(), myfn), (int)(10*Math.random()), TimeUnit.MILLISECONDS);
                	// exesvc.execute(new ProcDriver( child.toAbsolutePath() ));
                } else {
                	System.out.println("Ignoring... " + child);
                }            
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
	// Driver constructor
	// we could make this into a config file
	public Driver(String path, Function<String,String> transform) {
		this.watcher = null;
        this.keys = new HashMap<WatchKey,Path>();
        this.numGroups = Group.values().length;
        this.myfn = transform;
        
        /************
	    String fileName = path;
	    InputStream is = null;
	    Properties prop = new Properties();
	    try {
	    	is = new FileInputStream(fileName);
			prop.load(is);
	    } catch (FileNotFoundException fnfe) {
	    	System.out.println("file not found...");
	    	System.exit(0);
	    } catch (IOException ioe) {
	    	System.out.println("io exception in property load.");
	    	System.exit(0);
	    }
	   
        for (Object key: prop.keySet()) {
            System.out.println(key + ": " + prop.getProperty(key.toString()));
        }
        ***************/
        try {
        	this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ioe) {
        	System.out.println(" could not get WatchService...");
        }
        
 
        File pardirFile = new File(pardir);
        pardirFile.mkdir();
     
        // for this project we will assume windows and 
        // initialize inp and output directory
 
        File inpdirFile = new File(inpdir);
        inpdirFile.mkdir();
 
        // we are not using configuration file to avoid
        // unnecessary complexity.
        Path dir = Paths.get("\\tmp\\sample");
        register(dir);
        
        //exesvc = Executors.newScheduledThreadPool(numGroups);
        exesvc = Executors.newScheduledThreadPool(numGroups);

	}
	public Driver(Function<String,String> transform) {
		this("C:\\tmp\\processor.cfg", transform);
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("enum size" + Group.values().length);

		// define the transform we are interested in
		Function<String,String> trans = i -> i.toLowerCase();
		// init the driver
		Driver driver = new Driver(trans);
		
		// kick off the threads to generate files or our test processing
		// in its own thread.
		Thread t2 = new Thread(new GenerateFiles());
		t2.start();
		System.out.println(" generate thread invoked.... ");
		
		// now handle events...
		driver.processEvents();
	}

}
