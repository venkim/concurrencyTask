# concurrencyTask
concur task
Driver - is the main entry point. 
The main method initializes a transform - it does toLowerCase() but it can be anything.
  Driver has a file watcher, a functional interface (transform), and a scheduled executor service
    as instance members. It initializes those and uses a enum Groups to keep track of the groups.
Then spins off a thread to test generate a bunch of files.
The driver then kicks off the process Events method which listens to the file watcher method to
  kick off the procDriver which stands for processDriver - in its own thread (from the pool).
The procDriver  -- takes a path (one of the inputs files) and the transform (functional interface)
  and applies the transform to blocks of the files and writes it out.
