package com.venkat.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Function;

/*
 * ProcDriver - stands for Process Driver
 * It takes a path and transform
 * and applies the transform on the contents of the file
 * It 
 */
public class ProcDriver implements Runnable {
	// this will stand for the buffer size we read at one time
	public static final int BLOCK_SIZE = 2048;
	public static final char DOT = '.';
	public static final String OUT = "out";
	Path myPath;
	// this is the transform that the user provides 
	// to transform our input files
	Function<String,String> myfn;
	ProcDriver(Path path,Function<String,String> transform){
		this.myPath = path;
		this.myfn = transform;
		
	}
	private String getOutputFilename() {

		String filename = myPath.toAbsolutePath().toString();
		/****
		System.out.println("filename is " + filename);
		System.out.println("separator is " + File.separatorChar);
		***/
		// Find the index of last separator char
		int idx = filename.length() -1;
		while (filename.charAt(idx) != File.separatorChar) {
			// System.out.println("idx = " + idx + "  char is " + filename.charAt(idx));
			idx--;
		}
		// get the directory name
		String dirname = filename.substring(0, idx+1);
		//System.out.println("dirname is " + dirname);
		
		// get file name only
		String fnamonly = filename.substring(idx+1);
		// System.out.println("fnamonly is " + fnamonly);
		idx = fnamonly.length() - 1;
		while (fnamonly.charAt(idx) != DOT) {
			idx--;
		}
		// without extension 
		String fnameWoExt = fnamonly.substring(0, idx);
		//System.out.println("fnamWoExt is " + fnameWoExt);
		// take the fnameWoExt and add DOT and out suffixes
		String outfileonly = fnameWoExt + DOT + OUT;
		//System.out.println("outfileonly is " + outfileonly);
		String outdir = dirname + File.separatorChar + OUT;
		if (new File(outdir).mkdir()) {
			System.out.println("out directory created...");
		}
		String outfile = dirname + File.separatorChar + 
							 OUT + File.separatorChar + 
							 outfileonly;
		return outfile;	
	}
	public void process() {
		String inxfile = myPath.toAbsolutePath().toString();
		String outfile = getOutputFilename();
		System.out.println("inxfile is " + inxfile);
		System.out.println("outfile is " + outfile);
		BufferedReader breader = null;
		BufferedWriter bwriter = null;
		try {
			breader = new BufferedReader(new FileReader(inxfile));
			bwriter = new BufferedWriter(new FileWriter(outfile)); 
	
			char[] buf = new char[BLOCK_SIZE];
			int offset = 0;
			int len = buf.length;
			int readLen = 0;
			while ( (readLen = breader.read(buf, 0, len)) > 0){
				bwriter.write( myfn.apply(new String(buf)).toCharArray(), 0, readLen);
				bwriter.newLine();
			}

			if (breader != null)
				breader.close();
			if (bwriter != null)
				bwriter.close();
		
		} 
		catch (Exception ee) {
			System.out.println("Exception ..." + ee.getMessage());
		} finally {

		}
	}
	public void run() {
		process();
	}
}
