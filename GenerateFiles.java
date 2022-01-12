package com.venkat.concurrency;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

/*
 * This is a test generator
 * This will keep on generating events / files
 * to be processed into the input directory
 * It automatically generates files with random characters
 * constructed into strings and makes up files.
 * we can make it an infinite loop to test longer timeframes
 * at the risk of filling up disk space.
 */
public class GenerateFiles implements Runnable {
	public static final String outdir = "C:\\tmp\\sample\\";
	public static final String OUT = "out";
	public static final char DOT = '.';
	String[] grps = {"group1","group2","group3"};
	public int numFiles = 0;
	public GenerateFiles() {
		this.numFiles = 250;
		System.out.println("in GenerateFiles..");
	}
	
	public void run() {
		System.out.println(" run in gen files started...");
		for(int i = 1; i <= numFiles ; i++) {
			for(String grp: grps) {
				String name = grp + DOT + "work" + i + DOT + "txt";
				String outfile = outdir + name;
				System.out.println("outfile is " + outfile);
				try {
					BufferedWriter bwriter = new BufferedWriter(new FileWriter(outfile));
					int numlines = (int) (Math.random() * 100);
					for(int j = 0; j < numlines; j++) {
						StringBuilder sb = new StringBuilder();
						for(int z = 1; z <= 250; z++) {
							int x = (int) (177 + Math.random()*26);
							x = x % 26;
							if ((int)(Math.random()*5) == 0)
								sb.append((char)('A' + x));
							else
								sb.append((char)('a' + x));
							if ((int)(Math.random()*5) == 0)
								sb.append(" ");
						}
						//System.out.println("writing... " + sb.toString());
						bwriter.write(sb.toString());
						bwriter.newLine();
					}
					bwriter.close();
					
					try {
						TimeUnit.MILLISECONDS.sleep((int)(1000*Math.random()));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(); 
					}
				} catch (Exception fie) {
					System.out.println("File write exception..." + fie.getMessage());
				} finally {

				}
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
}
